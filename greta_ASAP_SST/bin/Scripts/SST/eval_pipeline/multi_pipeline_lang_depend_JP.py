#!/usr/bin/env python3
# coding: utf-8
"""
Created on Mon Mar  1 10:15:00 2022
Updated on Fri Apr 21 12:00:00 2023

@author: takeshi-s

It is better to use Windows machine since Openface on docker support multiprocessing only for Windows 

TODO!
    add translation function of component score name (e.g. EN to JP)


"""

import os
import csv
import sys
import cv2
import json
import time
import copy
import math
import shutil
import platform
import datetime
import argparse
import traceback
import threading
import subprocess
import numpy as np
import pandas as pd
import pprint as pp
import pickle as pkl
from tqdm import tqdm
from pydub import AudioSegment

sys.path.append(os.path.dirname(__file__))

import audio_face_util
import reset_util

from openpose_src import util as openpose_util
from openpose_src.body import Body
from openpose_src.hand import Hand
import torch

#dummy
STRATEGY_BEST_WORST = False
STRATEGY_NM = True

# SENTENCE_ENCODING = 'cp932'
SENTENCE_ENCODING = 'shift-jis'

xml_base_dir = '../../Common/Data/FlipperResources/fmltemplates_SST_JP'
xml_feedback_file = 'AgentSpeech_feedback.xml'
# xml_template_path = os.path.join(xml_base_dir, xml_file)

def select_pred_target(predicted_score, task_name):

    elim_tgt_dict ={
        "TELL":{"視線", "体の向きと距離", "明晰さ", "流暢さ"},
        "LISTEN":{"体の向きと距離", "声の変化", "明晰さ", "流暢さ"},
        "FAVOR":{"視線", "流暢さ"},
        "REFUSE":{"表情", "声の変化", "明晰さ", "流暢さ"}
    }
    
    filtered_score = {}
    for key in predicted_score.keys():
        if key in elim_tgt_dict[task_name]:
            pass
        else:
            filtered_score[key] = predicted_score[key]
    
    return filtered_score

def generate_feedbacks(result_dict, task_name, num_positive = 1, num_negative = 1, feedback_csv_path = 'eval_pipeline/feedback_sentences_JP.csv'):

    # Dummy function
    feedbacks = {'PositiveComment':'ぽじてぃぶふぃーどばっく', 'NegativeComment':'ねがてぃぶふぃーどばっく'}
    # feedbacks = {'PositiveComment':'positive dummy', 'NegativeComment':'negative dummy'}
    
    # try:
    #     feedback_list = _load_csv(feedback_csv_path, encoding='utf-8')
    # except Exception:
    #     print(traceback.format_exc())
    try:
        # feedback_list = _load_csv(feedback_csv_path, encoding='shift-jis')
        feedback_list = _load_csv(feedback_csv_path, encoding=SENTENCE_ENCODING)
    except Exception:
        print(traceback.format_exc())    
    
    
    feedback_list = feedback_list[1:]
    
    feedback_dict = {}
    for row in feedback_list:
        feedback_dict[row[0]] = {}
    for row in feedback_list:
        feedback_dict[row[0]][row[1]] = [row[2], row[3]]
    
    #pp.pprint(feedback_dict)
    
    if STRATEGY_BEST_WORST:
        pos_sent, neg_sent = strategy_best_worst(result_dict, feedback_dict, task_name)
    if STRATEGY_NM:
        pos_sent, neg_sent = strategy_good_n_bad_m(result_dict, feedback_dict, task_name, num_positive, num_negative)

    if "social-appropriateness" in result_dict.keys():
        if task_name == "TELL":
            task_specific_score = "Attention to response"
            pos_sent = pos_sent.replace('social-appropriateness', "Attention to response")
            neg_sent = neg_sent.replace('social-appropriateness', "Attention to response")
        if task_name == "LISTEN":
            task_specific_score = "Attentive listening"
            pos_sent = pos_sent.replace('social-appropriateness', "Attentive listening")
            neg_sent = neg_sent.replace('social-appropriateness', "Attentive listening")
        if task_name == "FAVOR":
            task_specific_score = "Clear and emotional request"
            pos_sent = pos_sent.replace('social-appropriateness', "Clear and emotional request")
            neg_sent = neg_sent.replace('social-appropriateness', "Clear and emotional request")
        if task_name == "REFUSE":
            task_specific_score = "Decline with sincerity"
            pos_sent = pos_sent.replace('social-appropriateness', "Decline with sincerity")
            neg_sent = neg_sent.replace('social-appropriateness', "Decline with sincerity")
        result_dict[task_specific_score] = result_dict["social-appropriateness"]
        result_dict.pop("social-appropriateness")

    feedbacks = {'PositiveComment':pos_sent, 'NegativeComment':neg_sent}
    
    return feedbacks


def _generate_sentence(pairs, feedback_dict, task_name, positive=True):
    
    num_component = len(pairs)
    sent = ''
    
    if positive:

        if num_component == 0:
            sent = ''
        elif num_component == 1:
            #print(pairs[0])
            #print(feedback_dict[task_name][pairs[0][0]][0])
            sent = '{0:}がよかったと思います。{0:}をうまく扱うことができると、{1:}ので良いコミュニケーションができそうです！'.format(pairs[0][0], feedback_dict[task_name][pairs[0][0]][0])
        elif num_component == 2:
            sent = '{0:}と{1:}がよかったと思います。'.format(pairs[0][0], pairs[1][0])
            sent += '{0:}をうまく扱うことができると、{1:}と思いますし、'.format(pairs[0][0], feedback_dict[task_name][pairs[0][0]][0])
            sent += '{0:}をうまく使えると、{1:}ので、良いコミュニケーションができそうです！'.format(pairs[1][0], feedback_dict[task_name][pairs[1][0]][0])
        else:
            sent = 'すみません、エンジニアさん。フィードバック項目は0個か1個か2個にしてもらえますか？それ以外は私にはちょっと・・・ごめんなさい'
    
    else:
        
        if num_component == 0:
            sent = ''
        elif num_component == 1:
            sent = '{0:}についてはもう少し意識できると、よりよいコミュニケーションがとれそうですね。'.format(pairs[0][0])
            sent += '{0:}をうまく扱えると、{1:}ので、お互いに気持ちのいいコミュニケーションの実現が期待できます。'.format(pairs[0][0], feedback_dict[task_name][pairs[0][0]][0])
            sent += 'もし{0:}の改善が難しいようであれば、{1:}と、いいかもしれません！'.format(pairs[0][0], feedback_dict[task_name][pairs[0][0]][1])
        elif num_component == 2:
            sent = '{0:}と{1:}についてはもう少し意識できると、よりよいコミュニケーションがとれそうですね。'.format(pairs[0][0], pairs[1][0])
            sent += '{0:}をうまく扱えると、{1:}ので、お互いに気持ちのいいコミュニケーションの実現が期待できます。'.format(pairs[0][0], feedback_dict[task_name][pairs[0][0]][0])
            sent += 'また、{0:}をうまく扱えると、{1:}ので、お互いに気持ちのいいコミュニケーションの実現が期待できます。'.format(pairs[1][0], feedback_dict[task_name][pairs[1][0]][0])
            sent += 'もし{0:}の改善が難しいようであれば、{1:}と、いいかもしれません！'.format(pairs[0][0], feedback_dict[task_name][pairs[0][0]][1])
            sent += '{0:}については{1:}と改善できそうです！'.format(pairs[1][0], feedback_dict[task_name][pairs[1][0]][1])
        else:
            sent = 'すみません、エンジニアさん。フィードバック項目は0個か1個か2個にしてもらえますか？それ以外は私にはちょっと・・・ごめんなさい'


    return sent

def replace_component_name(task_name, predicted_score):
    
    pp.pprint(predicted_score)

    component_name_file = 'eval_pipeline/component_name_JP.csv'
    
    with open(component_name_file, 'r', encoding = 'utf-8') as f:
        reader = csv.reader(f)
        lookup = [x for x in reader]
        
    replaced_predicted_score = {}
    
    for key in predicted_score.keys():
        for i in range(len(lookup)):
            if (lookup[i][0] == task_name) and (lookup[i][1] == key):
                replaced_predicted_score[lookup[i][2]] = predicted_score[key]
    
    pp.pprint(replaced_predicted_score)
    
    return replaced_predicted_score
        
def strategy_best_worst(result_dict, feedback_dict, task_name):
    
    min_pair = ['', 999]
    max_pair = ['', -999]
    
    for component in result_dict.keys():
        
        tmp_score = result_dict[component]
        
        if tmp_score < min_pair[1]:
            min_pair = [component, tmp_score]
        
        if max_pair[1] < tmp_score:
            max_pair = [component, tmp_score]
    
    pos_sent = _generate_sentence([max_pair], feedback_dict, task_name, positive=True)
    neg_sent = _generate_sentence([min_pair], feedback_dict, task_name, positive=False)
    
    return pos_sent, neg_sent

def strategy_good_n_bad_m(result_dict, feedback_dict, task_name, n, m):
    
    component_list= []
    for component in result_dict.keys():
        component_list.append([component, result_dict[component]])
    score_sorted_list = sorted(component_list, key = lambda x:x[1], reverse = True)
    
    print(score_sorted_list)
    
    pos_pair_list = []
    neg_pair_list = []
    for i in range(n):
        pos_pair_list.append(score_sorted_list[0])
        score_sorted_list.pop(0)
    for i in range(m):
        neg_pair_list.append(score_sorted_list[-1])
        score_sorted_list.pop(-1)

    pos_sent = _generate_sentence(pos_pair_list, feedback_dict, task_name, positive=True)
    neg_sent = _generate_sentence(neg_pair_list, feedback_dict, task_name, positive=False)
    
    return pos_sent, neg_sent

def _write_csv(file_path, data):
    
    with open(file_path, 'w', newline='') as f:
        writer = csv.writer(f, lineterminator='\n')
        writer.writerows(data)

def _load_csv(input_file, delimiter = ',', encoding = 'utf-8', cast = None):
    
    with open(input_file, encoding = encoding) as f:
        reader = csv.reader(f, delimiter = delimiter)
        if cast != None:
            data = []
            for row in reader:
                temp = []
                for x in row:
                    temp.append(cast(x))
                data.append(temp)
        else:
            data = [x for x in reader]
    
    return data