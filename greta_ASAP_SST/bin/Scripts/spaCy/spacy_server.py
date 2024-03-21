# -*- coding: utf-8 -*-
"""
Created on Fri Jun  9 15:58:10 2023

@author: Stagiaire
"""

import spacy
from spacy.matcher import Matcher
from spacy.util import filter_spans
# from spacy.lang.ja.examples import sentences
# from spacy.lang.fr.examples import sentences 
# nlp = spacy.load("fr_core_news_sm")

import textacy

import pprint as pp
import subprocess
import time
import sys
import re


import socket

HOST = "127.0.0.1"  # Standard loopback interface address (localhost)
PORT = 65432  # Port to listen on (non-privileged ports are > 1023)



# this can be overrided through command-line argument
lang = 'EN'

output_path = __file__ + '/../../../parsed_sentences.txt'
# print(output_path)

nlp = None
nlp_textacy = None
INITIALIZED = False

DEBUG = True

def init_server(args, host = "127.0.0.1", port = 65432):
    
    global lang
    
    lang        = args[1]
    
    init()

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((host, port))
        
        while True:
            
            print('### Spacy parser: waiting for connection...')
            s.listen()
            conn, addr = s.accept()
            
            with conn:
                print("### Spacy parser: Connected by", addr)

                data = conn.recv(1024)
                data = data.decode('utf-8')
                
                output = analyze_for_MM(data)
                with open(output_path, 'w', encoding = 'utf-8') as f:
                    f.write(output)                

                conn.sendall("OK".encode('utf-8'))
                
                conn.close()

def main(args):
    
    init_server(args)
    
def test(lang):

    s = time.time()
    init(lang)
    m = time.time()
    
    # print("nlp")
    # doc = nlp(sentences[0])
    # print(doc.text)
    # for token in doc:
    #     print("------------------------")
    #     print("text:", token.text)
    #     print("pos:", token.pos_)
    #     print("dep:", token.dep_)
    #     print("lemma:", token.lemma_)
        
    # print("{:25} {:10} {:10} {:20} {:10} {:10} {:50} {:50}".format('Token','Pos-Tag','Dep','Head','Pos-Head','NER', 'Lefts', 'Rights'))
    # print("_" * 150)
    # for token in doc:
    #     print("{:25} {:10} {:10} {:20} {:10} {:10} {:50} {:50}".format(token.text, token.pos_ , token.dep_, token.head.text, token.head.pos_, token.ent_type_,  "-".join([w.text for w in token.lefts]), " ".join([w.text for w in token.rights])))

    # sentences = 'é ­ã?èµ¤ã?é­ãé£ã?¹ãç«ã?ã?ã??ã?ã?¡ããã«ã?ã?£ã?ã'
    # lang = 'JP'

    sentences = 'He ate a lot of Tofu. That was too much for me.'
    lang = 'EN'

    # sentences = 'Es freut mich, dich kennenzulernen.'
    # lang = 'DE'
    
    # sentences = 'ì?ë¦­ì?´ ì¬ê³¼ë¥¼ ë¨¹ì´ì'
    # lang = 'KO'
    
    output = analyze_for_MM(sentences, lang)
        
    e = time.time()
    
    # print(m-s)
    # print(e-m)
    # print(e-s)

def init():
    
    global lang,nlp,nlp_textacy,INITIALIZED
    
    lang = lang.upper()
    
    if lang == 'EN':
        nlp = _load_model("en_core_web_sm")
        nlp_textacy = textacy.load_spacy_lang("en_core_web_sm")
    elif lang == 'FR':
        nlp = _load_model("fr_core_news_sm")
        nlp_textacy = textacy.load_spacy_lang("fr_core_news_sm")
    elif lang == 'JP':
        nlp = _load_model("ja_core_news_sm")
        nlp_textacy = textacy.load_spacy_lang("ja_core_news_sm")
    else:
        try:
            nlp = _load_model("{}_core_web_sm".format(lang.lower()))
            nlp_textacy = textacy.load_spacy_lang("{}_core_web_sm".format(lang.lower()))
        except:
            nlp = _load_model("{}_core_news_sm".format(lang.lower()))
            nlp_textacy = textacy.load_spacy_lang("{}_core_news_sm".format(lang.lower()))
    
    INITIALIZED = True
            

def analyze_for_MM(sentences):
       
    if not INITIALIZED:
        init()
        
    t1 = time.time()
    
    # _ = ''
    # print('### sentences')
    # print(sentences)
    # for sentence in sentences:
    #     if len(sentence) == 0:
    #         pass
    #     else:
    #         _ += sentence
    #         _ += '\n'
    # sentences = _
    
            
    
    # sentences = sentences.split('\n')
    
    sentences = sentences.replace("\n", " ")
    sentences = sentences.replace("  ", " ")
    pp.pprint(sentences)
    
    doc = nlp(sentences)
    
    token_properties = []
    
    doc_sentences = [x for x in doc.sents]
    
    t2 = time.time()

        
    for sentence in doc_sentences:
        
        token_properties.append([])
                
        if DEBUG:
            print('################################')
            print(sentence)
        
        # output = output + str(sentence).strip("\n") + '#'
        
        for token in sentence:
            if True: #dummy
            # if token.pos_ in ['NOUN', 'VERB', 'ADJ', 'ADV']:
                
                if DEBUG:
                    print('{:5s} {:5s} {:5s} {:5s}'.format(str(token), token.lemma_, token.pos_, token.dep_))
                
                token_properties[-1].append([str(token), token.lemma_, token.pos_, token.dep_, 'O'])
                
                # output = output + '{} {} {} {}'.format(str(token), token.lemma_, token.pos_, token.dep_) + '/'
        
        # output += '\n'
        
        if DEBUG:
            print('################################')

    t3 = time.time()

    
    ########################
    # Noun phrase chunking #
    ########################
    
    noun_chunktag_list = []
    noun_token_chunktag_list = []

    for chunk in doc.noun_chunks:
        noun_chunktag_list.append([])
        noun_chunktag_list[-1].extend([chunk.text, chunk.root.text, chunk.root.dep_, chunk.root.head.text])
    # pp.pprint(noun_chunktag_list)
    
    for chunktag in noun_chunktag_list:
        chunk_text_tokens = [x for x in nlp(chunktag[0])]
        # print(chunk_text_tokens)
        for token in chunk_text_tokens:
            if token == chunk_text_tokens[0]:
                noun_token_chunktag_list.append([str(token), 'B-NP'])
            else:
                noun_token_chunktag_list.append([str(token), 'I-NP'])
    
    # pp.pprint(noun_token_chunktag_list)
    # pp.pprint(token_properties)
    
    t4 = time.time()

    
    token_properties = add_chunktag(token_properties, noun_token_chunktag_list)
    
    t5 = time.time()
    

    if DEBUG:
        print('################################')

    
    ########################
    # Verb phrase chunking #
    ########################
    
    # pattern = [
    #     [{'POS': 'VERB'},{'OP': '?'}],
    #     [{'POS': 'ADV'}, {'OP': '*'}],
    #     [{'POS': 'AUX'}, {'OP': '*'}],
    #     [{'POS': 'VERB'}, {'OP': '+'}]
    #     ]

    # # instantiate a Matcher instance
    # matcher = Matcher(nlp.vocab)
    # matcher.add("Verb phrase", pattern)
    
    # # doc = nlp(sentence) 
    # # call the matcher to find matches
    # for sent in doc.sents:
    #     matches = matcher(sent)
    #     spans = [doc[start:end] for _, start, end in matches]
        
    #     print (filter_spans(spans))
    
    verb_chunktag_list = []
    verb_token_chunktag_list = []
    
    for sentence in doc.sents:
        vp = getVP(sentence, r'Spacy')
        if len(vp) != 0:
            verb_chunktag_list.extend(vp)
        # print(vp)
        
    for chunktag in verb_chunktag_list:
        chunk_text_tokens = [x for x in nlp(chunktag)]
        # print(chunk_text_tokens)
    
        for token in chunk_text_tokens:
            if token == chunk_text_tokens[0]:
                verb_token_chunktag_list.append([str(token), 'B-VP'])
            else:
                verb_token_chunktag_list.append([str(token), 'I-VP'])

    t6 = time.time()
        
    
    token_properties = add_chunktag(token_properties, verb_token_chunktag_list)

    if DEBUG:
        # pp.pprint(verb_token_chunktag_list)
        # pp.pprint(token_properties)
        for sentence in token_properties:
            for token in sentence:
                print(token[-1])
        print('################################')
    
    output = ""
    for i, sentence in enumerate(doc_sentences):
        output = output + str(sentence).strip("\n") + '#'
        for token_property in token_properties[i]:
            output = output + ' '.join(token_property) + '/'
        output += '\n'
    
    # output = output.strip()
    
    # if DEBUG:
    #     print(output)
    
    pp.pprint(token_properties)

    t7 = time.time()
    
    if DEBUG:
        print(t2-t1)
        print(t3-t2)
        print(t4-t3)
        print(t5-t4)
        print(t6-t5)
        print(t7-t6)
        print(t7-t1)
    
    return output
    
    

def _load_model(model_name):
    
    try:
        nlp = spacy.load(model_name)
    except:
        print('#################################################')
        print('#################################################')
        print('Trying to download', model_name)
        print('This process could take several minutes.')
        print('#################################################')
        print('#################################################')
        proc = subprocess.run('python -m spacy download {}'.format(model_name), capture_output=True, text=True)
        print(proc.stdout)
        print(proc.stderr)
        nlp = spacy.load(model_name)
        
    print(model_name, 'loaded')
    
    return nlp


def add_chunktag(token_properties, token_chunktag_list):
    
    # print("@@@@@")
    # pp.pprint(token_properties)
    # pp.pprint(token_chunktag_list)
    
    for i in range(len(token_properties)):
        for j in range(len(token_properties[i])):
            
            for k in range(len(token_chunktag_list)):
                if token_properties[i][j][0] == token_chunktag_list[k][0]:
                    token_properties[i][j][4] = token_chunktag_list[k][1]
                    if len(token_chunktag_list) != 0:
                        token_chunktag_list.pop(0)
                    break
            
            # # if above loop didn't encounter the "break" in the above "if" clause
            # else:
            #     token_properties[i][j][4] = 'O'
    
    return token_properties

def getVP(nlpdoc, mytoken):
    
    mylist = []
    
    patt = re.compile(mytoken)
    
    for token in nlpdoc:
        if token.pos_ == 'VERB' or token.pos_ == 'AUX':
            #print('    ')
            # print(token.text)
            #print('    ')
            #get children on verb/aux
            nodechild = token.children
            getchild1 = []
            getchild2 = []
            #iterate over the children
            for child in nodechild:
                getchild1.append(child)
                #get children of children
                listchild = list(child.children)
                for grandchild in listchild:
                    getchild2.append(grandchild)
            #print('children are ' + str(getchild1)) 
            #print('grandchildren are ' + str(getchild2))
            #check if Spacy is a children or a children of a children
            test1 = [patt.search(tok.lemma_) for tok in getchild1]
            test2 = [patt.search(tok.lemma_) for tok in getchild2]
            #if YES, then parse the VP
            if any(test1) or any(test2):
                
                fulltok = token.text
                myiter = token
                #the VP can actually start a bit before the VERB, so we look for the leftmost AUX/VERBS
                candidates = [lefty for lefty in token.lefts]
                candidates = [lefty for lefty in candidates if lefty.pos_ in ['AUX', 'VERB']]
                #if we find one, then we start concatenating the tokens from there
                if candidates:
                    fulltok = candidates[0].text
                    myiter = candidates[0]

                while myiter.nbor().pos_ in ['VERB','PART','ADV','ADJ','ADP','NUM','DET','NOUN','PROPN','AUX']:
                    fulltok = fulltok + ' '+ myiter.nbor().text
                    myiter = myiter.nbor()
                mylist.append(fulltok)
                
    return mylist  

if __name__ == '__main__':
    args = sys.argv
    
    print(len(args))
    for x in args:
        print(x)
    
    if len(args) != 2:
        args = ['', 'EN']
        
    main(args)

# print("lemmatizer")
# lemmatizer = nlp.add_pipe("lemmatizer")
# doc = lemmatizer(sentences[0])
# print(doc.text)
# for token in doc:
#     print(token.text, token.pos_, token.dep_, token.lemma_)