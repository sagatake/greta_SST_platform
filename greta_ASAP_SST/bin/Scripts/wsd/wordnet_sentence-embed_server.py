
"""
WordNet python API: https://github.com/goodmami/wn
Multilingual sentence encoder: https://www.sbert.net/examples/training/multilingual/README.html

This method only works with WordNet in some languages which has example sentences for each synset (e.g. english, japanese)

"""
import wn
from sentence_transformers import SentenceTransformer,util
from googletrans import Translator

import pprint as pp
import socket
import sys

DEBUG = True
lang = 'ja'

wn_obj = None
wn_obj_en = None
encoder = None
translator = None

# embeddings = model.encode(['Hello World', 'Hallo Welt', 'Hola mundo'])
# print(embeddings)

#obj = wn.Wordnet('omw-en:1.4')        # Create Wordnet object to query
#ss = obj.synsets('win', pos='v')[0]  # Get the first synset for 'win'

# context = '彼は熱い男だ'
# word = '熱い'

# context = 'もう少しだけ待っていてくれませんか？'
# word = '少し'

# context = '昨日買った宝くじが当たった'
# word = '当たる'

def init_server(args, host = "127.0.0.1", port = 55556):
    
    global lang
    
    lang = args[1]
    
    init(lang)

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((host, port))
        
        while True:
            
            print('### WSD server: waiting for connection...')
            s.listen()
            conn, addr = s.accept()
            
            with conn:
                print("### WSD server: Connected by", addr)

                data = conn.recv(1024)
                data = data.decode('utf-8')
                
                context,word_list,pos_list = data.split('@')
                
                word_list = word_list.strip('[')
                word_list = word_list.strip(']')
                pos_list = pos_list.strip('[')
                pos_list = pos_list.strip(']')
                
                word_list = word_list.split(', ')
                pos_list = pos_list.split(', ')
                
                print('context:', context)
                print('word:', word_list)
                print('pos:', pos_list)
                
                output = WSD(context,word_list,pos_list)
                
                output = str(output)
                
                conn.sendall(output.encode('utf-8'))
                
                conn.close()

def init(lang = 'en'):
    
    global wn_obj,wn_obj_en,encoder,translator
    
    lang = lang.lower()
    
    wn_model_name = 'omw-{}:1.4'.format(lang)
    wn.download(wn_model_name)
    wn_obj = wn.Wordnet(wn_model_name)        # Create Wordnet object to query
    
    wn_model_name = 'omw-en:1.4'
    wn.download(wn_model_name)
    wn_obj_en = wn.Wordnet(wn_model_name)
    
    encoder = SentenceTransformer('distiluse-base-multilingual-cased-v2')
    
    translator = Translator()

def main(args):
    
    init_server(args)

def WSD(context, word_list, pos_list = []):
    
    context_embed = encoder.encode([context])
    
    synset_id_list = []
    
    for word,pos in zip(word_list, pos_list):
        
        print("word:", word)
    
        if pos == '':
            pos = None
        
        # skip dummy words. Those original words were skipped based on MeaningMiner rule 
        # (only use image schemas of words right after verb in verb phrase)
        if word == '_':
            synset_id_list.append('_')
            continue
        
        try:
        # if True:
            
            ss = wn_obj.synsets(word,pos)  # Get the first synset for 'win'
            
            
            result_list = []
            
            if DEBUG:
                print('context:', context)
                print('word:',word)
                print('pos:',pos)
                print('synsets:',ss)
            
            for s in ss:
                
                examples = s.examples()
                
                if DEBUG:
                    
                    print(examples)
                    # print(s)
                    print(s.definition())                
                    print(len(examples))
                    
                if len(examples) == 0:
                    # try:
                        src_id = s.id
                        tmp = src_id.split('-')
                        tgt_id = '{}-{}-{}-{}'.format(tmp[0], 'en', tmp[2], tmp[3])
                        tgt_synset = wn_obj_en.synset(tgt_id)
                        en_examples = tgt_synset.examples()
                        print(en_examples)
                        examples = [translator.translate(x, dest=lang).text for x in en_examples]
                        if DEBUG:
                            print('Translated English examples of {} to {}'.format(tgt_id, lang))
                            print(tgt_synset)
                            print(tgt_synset.lemmas())
                            print(en_examples)
                            print(examples)
                    # except Exception as e:
                    #     print(e)
                
                for example in examples:
                    example_embed = encoder.encode([example])
                    similarity = util.dot_score(context_embed, example_embed)
                    similarity = float(similarity[0][0])
                    
                    if DEBUG:
                        print('{:5.2f} - {:}'.format(similarity, example))
                    
                    result_list.append([s, example, similarity])
            
            result_list = sorted(result_list, key=lambda x:x[2], reverse=True)
            
            if DEBUG:
                print('##################################')
                print('context:', context)
                print('word:', word)
                pp.pprint(result_list[:5])
            
            synset_id = result_list[0][0].id
            
            _ = synset_id.split('-')
            synset_id = 'SID-{}-{}'.format(_[2], _[3].upper())
            synset_id_list.append(synset_id)
            
        except Exception as e:
            import traceback
            print(traceback.format_exc())
            print(e)
            synset_id = '_'
            synset_id_list.append(synset_id)
    
    return synset_id_list

if __name__ == '__main__':
    
    args = sys.argv
    
    if len(args) != 2:
        args = ['', 'en']
        # args = ['', 'fr']
    
    main(args)


# context: 彼は熱い男だ
# word: 熱い
# [[Synset('omw-ja-01247240-a'), '彼女は熱があって、疲れている', 0.41665059328079224],
#  [Synset('omw-ja-01726235-a'), '熱い雄弁', 0.30537497997283936],
#  [Synset('omw-ja-01247240-a'), '熱い額', 0.30137497186660767],
#  [Synset('omw-ja-01247240-a'), '熱風炉', 0.2704736590385437],
#  [Synset('omw-ja-00886117-a'), '暖かなサポート', 0.26729950308799744]]

# context: 殴ったら鈍い音がした
# word: 鈍い
# [[Synset('omw-ja-01454985-a'), '通りのはっきり聞こえない雑音', 0.14021816849708557],
#  [Synset('omw-ja-02011622-a'), '鈍いドサッという音', 0.12953317165374756],
#  [Synset('omw-ja-00980527-a'), '彼はニュースに反応するのが遅かった', 0.12180155515670776],
#  [Synset('omw-ja-00440579-a'),
#   '彼は、本当に馬鹿なのか、それともわざと鈍感に振舞っていた',
#   0.12174380570650101],
#  [Synset('omw-ja-02011622-a'), '弾丸をドンと打つこと', 0.12162065505981445]]

# context: もう少しだけ待っていてくれませんか？
# word: 少し
# [[Synset('omw-ja-00033663-r'), '少し小さい', 0.20356231927871704],
#  [Synset('omw-ja-00033663-r'), '少し気分が良くなった', 0.1420939862728119],
#  [Synset('omw-ja-00033663-r'), '少し暖かい', 0.10945092141628265],
#  [Synset('omw-ja-00033562-r'), '彼は、少し興味を持っていた', 0.08643881976604462],
#  [Synset('omw-ja-07309457-n'), '彼はちょっと運が良かった', 0.07824267446994781]]

# context: 昨日買った宝くじが当たった
# word: 当たる
# [[Synset('omw-ja-01240308-v'), '私は、電柱にぶつかった', 0.10557537525892258],
#  [Synset('omw-ja-02611976-v'), '光は、金色のネックレスに当たった', 0.06909787654876709],
#  [Synset('omw-ja-02611976-v'), '奇妙な音が私の耳にとどいた', 0.062229037284851074],
#  [Synset('omw-ja-02739480-v'), '私の提案は多くの反対を受けた', 0.059219710528850555],
#  [Synset('omw-ja-01205696-v'), '彼らの手が触れた', 0.05843706056475639]]