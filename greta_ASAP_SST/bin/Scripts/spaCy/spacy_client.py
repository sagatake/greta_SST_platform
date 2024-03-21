# -*- coding: utf-8 -*-
"""
Created on Fri Jun  9 15:58:10 2023

@author: Stagiaire
"""

# import spacy
# from spacy.matcher import Matcher
# from spacy.util import filter_spans
# # from spacy.lang.ja.examples import sentences
# # from spacy.lang.fr.examples import sentences 
# # nlp = spacy.load("fr_core_news_sm")

# import textacy

import pprint as pp
import subprocess
import time
import sys
import re

import socket


# this can be overrided through command-line argument
lang = 'EN'

output_path = __file__ + '/../../../parsed_sentences.txt'
# print(output_path)

nlp = None
nlp_textacy = None
INITIALIZED = False

DEBUG = False

def main(args):
    
    global lang
    
    t1 = time.time()
    
    sock = establish_connection()
    
    t2 = time.time()
    
    lang        = args[1] # Never used
    sentences   = args[2]
    
    # init()
    status = request_analysis(sock, sentences)
    print('spacy client request, returned status:', status)

    t3 = time.time()
    
    print('Duration - connection establishment:', t2-t1)
    print('Duration - process request:', t3-t2)
    print('Duration - total:', t3-t1)

def establish_connection(host = "127.0.0.1", port = 65432):

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((host, port))
    
    print("Connection established (host {}, port {})".format(host, port))
    
    return sock

def request_analysis(sock, sentences):
    
    sock.sendall(sentences.encode('utf-8'))
    status = sock.recv(1024).decode('utf-8')
    
    return status



if __name__ == '__main__':
    args = sys.argv
    
    print("### spacy_client.py:")
    # print(len(args))
    # for x in args:
        # print(x)
    
    if len(args) != 3:
        args = ['', 'EN', 'This is short dummy sentence.\nVery short sentence.\nAfter many years Spacy has suddently become a monster-package in the NLP world.']
        
    main(args)

# print("lemmatizer")
# lemmatizer = nlp.add_pipe("lemmatizer")
# doc = lemmatizer(sentences[0])
# print(doc.text)
# for token in doc:
#     print(token.text, token.pos_, token.dep_, token.lemma_)