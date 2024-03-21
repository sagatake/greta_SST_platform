
"""
WordNet python API: https://github.com/goodmami/wn
Multilingual sentence encoder: https://www.sbert.net/examples/training/multilingual/README.html

This method only works with WordNet in some languages which has example sentences for each synset (e.g. english, japanese)

"""
# import wn
# from sentence_transformers import SentenceTransformer,util

import socket
import sys

import pprint as pp
import time

DEBUG = False
lang = 'ja'

wn_obj = None
encoder = None

output_path = 'wsd_output.txt'

def main(args):
    
    global lang
    
    t1 = time.time()
    
    sock = establish_connection()
    
    t2 = time.time()
    
    lang        = args[1] # Never used
    context     = args[2]
    word        = args[3]
    pos         = args[4]
    
    # init()
    result = request_analysis(sock, context, word, pos)
    
    print('synset id:', result)
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(result)

    t3 = time.time()
    
    print('Duration - connection establishment:', t2-t1)
    print('Duration - process request:', t3-t2)
    print('Duration - total:', t3-t1)

def establish_connection(host = "127.0.0.1", port = 55555):

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((host, port))
    
    print("Connection established (host {}, port {})".format(host, port))
    
    return sock

def request_analysis(sock, context, word, pos):
    
    sentence = '@'.join([context, word, pos])
    
    sock.sendall(sentence.encode('utf-8'))
    result = sock.recv(1024)
    result = result.decode('utf-8')
    
    return result



if __name__ == '__main__':
    args = sys.argv
    
    print("### wsd_client.py:")
    # print(len(args))
    # for x in args:
    #     print(x)
    
    if len(args) != 5:
        # args = ['', 'en', 'It is really cold today.', 'cold', 'a']
        args = ['', 'fr', "Il fait très chaud aujourd'hui", 'fait', '']
        # args = ['', 'fr', "Il fait très chaud aujourd'hui", 'Il', '']
        # args = ['', 'ja', "今日はなぜか強烈だ", '強烈', '']

    # tmp = ['', 'en', 'It is really cold today.', 'it,is,really,cold,today', 'n,v,r,a,r']
    # word_list = tmp[3].split(",")
    # pos_list = tmp[4].split(",")
    
    # t1 = time.time()
    # for word,pos in zip(word_list, pos_list):
    #     args = ['', 'en', 'It is really cold today', word, pos]
    #     main(args)
    # t2 = time.time()
    # print(t2-t1)
    
    main(args)

# print("lemmatizer")
# lemmatizer = nlp.add_pipe("lemmatizer")
# doc = lemmatizer(sentences[0])
# print(doc.text)
# for token in doc:
#     print(token.text, token.pos_, token.dep_, token.lemma_)