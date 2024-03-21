# coding: UTF-8
#from bert_juman import BertWithJumanModel
import numpy as np
import os
import sys
import MeCab
import time
import pandas as pd
import json
import math
import subprocess
import pprint as pp
from threading import Thread
from queue import Queue, Empty

import shutil
import datetime

import torch
from transformers import BertModel, BertForNextSentencePrediction
#from transformers.tokenization_bert_japanese import BertJapaneseTokenizer as BertTokenizer
from transformers import BertJapaneseTokenizer as BertTokenizer
print('Loading BERT ...')
bert_tokenizer = BertTokenizer.from_pretrained('cl-tohoku/bert-base-japanese-whole-word-masking')
bert_model = BertModel.from_pretrained('cl-tohoku/bert-base-japanese-whole-word-masking')
print('Done')

import dialogue_util

os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "sample.json"

instruction_file = 'inst_EN.json'

ASR_BACKGROUND = True
ASR_MAIN = False
ASR_GRETA_WRAPPER = True
ASR_LANG_FOR_SUB = 'en-US'
# ASR_LANG_FOR_SUB = 'ja-JP'

TTS_GRETA = True
TTS_MAIN = False

TEXT_INPUT = False

DISABLE_BLOCK = False

def main():
    
    #task_name=sys.argv[1]
    # task_name = 'TELL'
    task_name = 'REFUSE' #select from LISTEN, TELL, FAVOR, REFUSE

    # m = MeCab.Tagger("-d C:/PROGRA~1/MeCab/dic/ipadic -Ochasen")
            
    
    ####### Scenario file #######
    Sce_dir = 'Scenario_EN'
    #args = sys.argv[1]
    #Sce_file="convey_feeling1.csv"
    #Sce_file_num=sys.argv[2]
    #Sce_file_num = '10'
    Sce_file_num = '1'
    #############################

    num_positive1 = '1'
    num_negative1 = '1'
    
    # path_w = r'C:\Users\Stagiaire\Documents\NetBeansProjects\Greta_SVN\bin\Examples\DemoEN\response\generate.xml'
    # path_w = r'C:\Users\Stagiaire\Documents\NetBeansProjects\greta-gpl-grimaldi_ASAPnASR\bin\Examples\DemoEN\response\generate.xml'
    # path_w = r'D:/Takeshi/SST/NetBeansProjects/greta-gpl-grimaldi_ASAPnASR/bin/Examples/DemoEN/response/generate.xml'
    path_w = r'../../Examples/DemoEN/response/generate.xml'
    
    duration_path = 'audio_duration.txt'
    
    if ASR_BACKGROUND:
        # speak_greta('音声認識が起動するまでお待ちください \n', path_w, duration_path)   
        speak_greta('Activating audio recognition\n', path_w, duration_path)   
        if ASR_MAIN:
            asr_client = dialogue_util.ASR_client()
            asr_client.start()
            time.sleep(10)
        elif ASR_GRETA_WRAPPER:
            asr_client = dialogue_util.ASR_greta_wrapper_client()
            asr_client.start()
        else:
            asr_client = dialogue_util.ASR_sub_client(ASR_LANG_FOR_SUB)
            asr_client.start()            
        # speak_greta('音声認識の起動が完了しました\n', path_w, duration_path)
        speak_greta('Activation completed\n', path_w, duration_path)
    
    Correct_res=0
    Correct_asr=0
    
    #sys_ini=True
    
    if task_name == "TELL": #select from LISTEN, TELL, FAVOR, REFUSE
        task_name_scenario="convey_feeling"
    elif task_name == "LISTEN":
        task_name_scenario="listening"
    elif task_name == "FAVOR":
        task_name_scenario="making_a_request"
    elif task_name == "REFUSE":
        task_name_scenario="declining"
      
    Sce_file = Sce_dir + "\\"+task_name_scenario+Sce_file_num+".csv"
    scenario=pd.read_csv(filepath_or_buffer=Sce_file, encoding="sjis", sep=",")
    print(math.ceil(len(scenario.columns)/2))
    
    #Max_turn=len(scenario.columns)/2
    Max_turn=len(scenario.columns)/2
    
    ## Introduction ##
    if ASR_BACKGROUND:
        self_intro(path_w, task_name, duration_path, asr_client)
    else:
        self_intro(path_w, task_name, duration_path)
    
    ## Role-play ##
    
    turn=1
    val=0
    record_flag=0
    
    flog = open('src_text/temp.txt', 'w')
    while True:
        
        if turn == 2 and record_flag == 0:
            ## recording
                      
            #video_rec1 = Recoder("record_wireless.bat")
            video_rec1 = Recoder("record.bat")
            # video_rec1 = Recoder("record_test.bat")
            video_rec1.start()
            
            ##
            record_flag = 1
            
        print("Trainer:",scenario["Sys"+str(turn)][val])
        print(turn)
    
        utter=scenario["Sys"+str(turn)][val]
        s5=utter+"\n"
        
        speak_greta(s5, path_w, duration_path)
              
        print("Turn:",turn,"Total:",Max_turn)
        if (Max_turn <= turn):
            break
      
        ### nogo
        #if (turn == 2):
          # subprocess.call("nogo.bat", shell=True)
          # subprocess.call("nogo.bat", shell=True)
        ###
          
        print("Waiting for your input...\n")
      
        ####
        if TEXT_INPUT:
            User = input('Input: ')
        else:
            if ASR_BACKGROUND:
                User = asr_client.get_all_q()
            else:
                subprocess.call("recognize.bat", shell=True)
                recognized=open('out.txt')
                User=recognized.readlines()[0] #or User2
    
        
        print("Recognized User utterance:")
        print(User)
        ####
      
        # Log_output #
        flog.write('Agent: ')
        flog.write(utter)
        flog.write('\n')
        flog.write('User: ')
        flog.write(User)
        flog.write('\n')
        
        
        ####  
        #User2=input("Waiting for your input...\n") # refere to the recognized one
        ####
        
        a=get_BERT_embed(User)[0]
        
        Yes=0
        No=1
        Misc=2
        
        Yes=scenario["Usr"+str(turn)][Yes].split("/")
        
        Yes = [word.upper() for word in Yes]
        
        if scenario["Usr"+str(turn)].count() == 1:
            best_cat="Yes"
        elif scenario["Usr"+str(turn)].count() == 2:
            No=scenario["Usr"+str(turn)][No].split("/")
            Yes_len,No_len=len(Yes),len(No)
          
            best=0
            rule=0
            for i in range(Yes_len):
                if Yes[i] in User:
                    best_cat = "Yes"
                    print("Yes rules")
                    rule=1
            for j in range(No_len):
                if No[j] in User:
                    best_cat = "No"
                    print("No rules")
                    rule=1
            if rule == 0:
            
                for i in range(Yes_len):
                    b=get_BERT_embed(Yes[i])[0]
                    #b=bert.get_sentence_embedding(Yes[i],pooling_layer=-1,pooling_strategy="REDUCE_MAX")
                    COS=cos_sim(a, b)
                    print(Yes[i],COS)
                    if COS >= best:
                        best=COS
                        best_cat="Yes"
                for j in range(No_len):
                    b=get_BERT_embed(No[j])[0]
                    #b=bert.get_sentence_embedding(No[j],pooling_layer=-1,pooling_strategy="REDUCE_MAX")
                    COS=cos_sim(a, b)
                    print(No[j],COS)
                    if COS >= best:
                        best=COS
                        best_cat="No"
                ##if best <= 0.7:
                      #best_cat=""
      
        elif scenario["Usr"+str(turn)].count() == 3: 
            
            No=scenario["Usr"+str(turn)][No].split("/")
            Misc=scenario["Usr"+str(turn)][Misc].split("/")
            Yes_len,No_len,Misc_len=len(Yes),len(No),len(Misc)
          
            best=0
            rule=0
            for i in range(Yes_len):
                if Yes[i] in User:
                    best_cat = "Yes"
                    print("Yes rules")
                    rule=1
            for j in range(No_len):
                if No[j] in User:
                    best_cat = "No"
                    print("No rules")
                    rule=1
            for k in range(Misc_len):
                if Misc[k] in User:
                    best_cat = "Misc"
                    print("Misc rules")
                    rule=1
                  
            if rule == 0:
            
                for i in range(Yes_len):
                    b=get_BERT_embed(Yes[i])[0]
                    #b=bert.get_sentence_embedding(Yes[i],pooling_layer=-1,pooling_strategy="REDUCE_MAX")
                    COS=cos_sim(a, b)
                    print(Yes[i],COS)
                    if COS >= best:
                        best=COS
                        best_cat="Yes"
                for j in range(No_len):
                    b=get_BERT_embed(No[j])[0]
                    #b=bert.get_sentence_embedding(No[j],pooling_layer=-1,pooling_strategy="REDUCE_MAX")
                    COS=cos_sim(a, b)
                    print(No[j],COS)
                    if COS >= best:
                        best=COS
                        best_cat="No"
                for k in range(Misc_len):
                    b=get_BERT_embed(Misc[k])[0]
                    #b=bert.get_sentence_embedding(Misc[k],pooling_layer=-1,pooling_strategy="REDUCE_MAX")
                    COS=cos_sim(a, b)
                    print(Misc[k],COS)
                    if COS >= best:
                        best=COS
                        best_cat="Misc"
        
        
        Num_candi=scenario["Sys"+str(turn+1)].count()
        print("Num of next system candidates: ",Num_candi)
      
        Num_candi2=scenario["Usr"+str(turn)].count()
        print("Num of current usr candidates: ",Num_candi2)
      
        ##### check scenario path #####
        turn, val = check_scenario_decision(Num_candi, Num_candi2, turn, val, best_cat)
    
    #############
      
    
    flog.close()
    print(Correct_res,turn-1)
    
    ## finish record 
    video_rec1.stop()
    
    if ASR_BACKGROUND:
        asr_client.stop()
    
    time.sleep(1)
        
    # utter="以上でロールプレイは終了です。ありがとうございました。結果を計算しますのでもう少々お待ちください。\n"
    utter="The training is over. Thank you for joining. Now, let me calculate the result please. Please wait for a while. \n"
    speak_greta(utter, path_w, duration_path, block = True)

    subprocess.call("record_copy.bat", shell=True)

    subprocess.call("python eval_pipeline\multi_pipeline.py {} {} {}".format(task_name, num_positive1, num_negative1), shell=True)

    json_open_feedback = open('result_for_viewer/result.json', 'r')
    json_load_feedback = json.load(json_open_feedback)
    
    feedback_pos=json_load_feedback["PositiveComment"]
    feedback_neg=json_load_feedback["NegativeComment"]

    # utter="ではフィードバックです。"+feedback_pos+feedback_neg
    utter="Here's your feedback. "+feedback_pos+feedback_neg
    speak_greta(utter, path_w, duration_path, block = False)

    subprocess.call("viewer_primary.bat", shell=True)
    
def check_scenario_decision(Num_candi, Num_candi2, turn, val, best_cat):
    
    if (Num_candi == 1):
        if best_cat == "Yes":
            print("Decision: ",best_cat)
            turn=turn+1
            val=0
        elif  (Num_candi2 == 1):
            turn=turn+1
            val=0
        else:
            print("Decision: No Go")
        
    elif (Num_candi == 2):
        if best_cat == "Yes":
            val=0
            turn=turn+1
        elif  best_cat == "No":
            val=1
            turn=turn+1
        else:
            val=2
            turn=turn+1
        
    elif (Num_candi == 3):
        if best_cat == "Yes":
            val=0
            turn=turn+1
        elif  best_cat == "No":
            val=1
            turn=turn+1
        elif best_cat == "Misc":
            val=2
            turn=turn+1
        
    return turn, val

    
def self_intro(path_w, task_name, duration_path, asr_client = None):
    
    # utter="こんにちは。レイと申します。お名前お伺いしてもよろしいですか？\n"
    utter = "Hellp, I'm Greta. May I have you name please?\n"
    speak_greta(utter, path_w, duration_path)
    
    if TEXT_INPUT:
        input_sent = input('Input: ')
    else:
        if asr_client != None:
            input_sent = asr_client.get_all_q()
        else:
            subprocess.call("recognize.bat", shell = True)
            recognized=open('out.txt')
            input_sent=recognized.readlines()[0]
    
    # utter = "こんにちは。本日はよろしくお願いいたします。\n"
    utter = "Hellp, let's enjoy the training together\n"
    speak_greta(utter, path_w, duration_path)
    
    json_open = open(instruction_file, 'r')
    json_load = json.load(json_open)
    
    if task_name == "TELL": #select from LISTEN, TELL, FAVOR, REFUSE
        inst=json_load["TELL"]
        # task_name_agent="気持ちを伝えるスキル"
        task_name_agent="expressing skill of positive feeling"
    elif task_name == "LISTEN":
        inst=json_load["LISTEN"]
        # task_name_agent="耳を傾けるスキル"
        task_name_agent="listening skill"
    elif task_name == "FAVOR":
        inst=json_load["FAVOR"]
        # task_name_agent="頼みごとをするスキル"
        task_name_agent="request making skill"
    elif task_name == "REFUSE":
        inst=json_load["REFUSE"]
        # task_name_agent="頼みをことわるスキル"
        task_name_agent="declining skill"
    
    # utter="これから、"+task_name_agent+"を一緒に学びます。"+inst+"わかりましたか？\n"
    utter = "Today, we are goin to do training on " + task_name_agent + ". " + inst + '\n'
    speak_greta(utter, path_w, duration_path)
    
    time.sleep(0.75)
    utter = "Did you get it?\n"
    speak_greta(utter, path_w, duration_path)
    
    if TEXT_INPUT:
        input_sent = input('Input: ')
    else:
        if asr_client != None:
            input_sent = asr_client.get_all_q()
        else:
            subprocess.call("recognize.bat", shell = True)
            recognized=open('out.txt')
            input_sent=recognized.readlines()[0]
    
    print("User utterance:")
    print(input_sent)
    

    
def get_BERT_embed(text, ALIGN=False, tgt_pos=['代名詞', '副詞', '助動詞', '助詞',
                                                '動詞', '名詞', '形容詞', '感動詞',
                                                '接尾辞', '接続詞', '接頭辞', '空白',
                                                '補助記号', '記号', '連帯詞',
                                                'フィラー']):
    
    bert_tokens, token_ids = tokenize_BERT(text) 
    outputs = calc_BERT_embed(token_ids)
    return outputs

def tokenize_BERT(sentence):
    #bert_tokens = bert_tokenizer.tokenize(" ".join(["[CLS]"] + output + ["[SEP]"]))
    input_ids = bert_tokenizer.encode(sentence, return_tensors='pt')
    bert_tokens = bert_tokenizer.convert_ids_to_tokens(input_ids[0].tolist())
    #print("BERT tokens: ")
    #for i in range(len(bert_tokens)):
    #    print(str(i) + bert_tokens[i])
    token_ids = bert_tokenizer.convert_tokens_to_ids(bert_tokens)
    #print("BERT token IDs: ", token_ids)
    return bert_tokens, token_ids

def calc_BERT_embed(token_ids):
    #ベクトル取得
    #print("\n *** to Vector ***")
    #print(token_ids)
    tokens_tensor = torch.tensor(token_ids).unsqueeze(0)
    #print(np.shape(tokens_tensor))
    #outputs, _ = bert_model(tokens_tensor)
    outputs = bert_model(tokens_tensor).last_hidden_state
    #print(outputs)
    #print(type(outputs))
    #print(np.shape(outputs))
    #print(outputs[0], "\n (size: ", outputs[0].size(), ")")
    outputs = outputs.detach().numpy().copy()
    return outputs[0]

def get_duration(path, subtract_sec = 0.5):
    
    #to wait writing duration time to audio_duration.txt by java
    time.sleep(1)
    
    with open(path) as f:
        
        duration_str = f.read()
        print(duration_str)
        duration_float = float(duration_str)

    print('Duration_original:', str(duration_float))
    
    duration_float = duration_float - subtract_sec -1
    
    if duration_float < 0:
        duration_float = 0.1
        
    print('Duration_subtracted:', str(duration_float))
    
    return duration_float
        
def cos_sim(v1, v2):
    
    return np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2))

def write_xml(s5, path_w):
    
    s1 = "<?xml version=\"1.0\" encoding=\"shift_jis\" ?>\n"
    s2 = "<fml-apml>\n"
    s3="<bml>\n"
    s4="<speech id=\"s1\" language=\"ja-JP\" start=\"0.0\" text=\"\" type=\"SAPI4\" voice=\"cerevoice\" xmlns=\"\">\n"
    #s5="\n"
    s6="</speech>\n"
    s7="</bml>\n"
    s8="</fml-apml>"

    with open(path_w, mode="w") as f:
        f.write(s1)
        f.write(s2)
        f.write(s3)
        f.write(s4)
        f.write(s5)
        f.write(s6)
        f.write(s7)
        f.write(s8)

class Recoder():
    
    def __init__(self, bat_file_name):
        #video_rec=subprocess.Popen("record_wireless.bat", stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True, universal_newlines=True)
        #video_rec=subprocess.Popen("record.bat", stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True, universal_newlines=True)
        self.bat_file_name = bat_file_name
    
    def start(self):
        self.video_rec=subprocess.Popen(self.bat_file_name, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True, universal_newlines=True)
        self.outQueue = Queue()
        self.errQueue = Queue()
        self.outThread = Thread(target=enqueue_output, args=(self.video_rec.stdout, self.outQueue))
        self.errThread = Thread(target=enqueue_output, args=(self.video_rec.stderr, self.errQueue))
        self.outThread.daemon = True
        self.errThread.daemon = True
        self.outThread.start()
        self.errThread.start()
        
    def stop(self):
        ## finish record 
        self.pid = self.video_rec.pid
        subprocess.Popen(['taskkill', '/F', '/T', '/PID', str(self.pid)])
        self.errors = getOutput(self.errQueue)
        self.output = getOutput(self.outQueue)


def enqueue_output(out, queue):
    for line in iter(out.readline, b''):
        queue.put(line)
    out.close()

def getOutput(outQueue):
    outStr = ''
    try:
        while True:
            outStr += outQueue.get_nowait()
    except Empty:
        return outStr

def speak_greta(utter, path_w, duration_path, block = True):
    
    if TTS_GRETA:
        write_xml(utter, path_w)
        subprocess.call("dummy.bat", shell=True)
        subprocess.call("dummy.bat", shell=True)
        
        ## block while speaking ##
        if (block == True) and not(DISABLE_BLOCK):
            delay=get_duration(duration_path)
            print(delay)
            time.sleep(delay)
    else:
        if TTS_MAIN:
            tts_client = dialogue_util.TTS_client()
            tts_client.speak(utter, block)
        else:
            tts_client = dialogue_util.TTS_sub_client()
            tts_client.speak(utter, 'en', block)



if __name__ == "__main__":
    main()
