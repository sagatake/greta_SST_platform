# -*- coding: utf-8 -*-
"""
Created on Fri Apr 28 12:00:24 2023

@author: Stagiaire
"""

import pprint as pp
import numpy as np
import csv
import os
import re

def main():
    
    src_dir     = 'csv_scenario_EN'
    tgt_dir_base= 'fmltemplates_SST'
    lang        = 'english'
    encoding    = 'utf-8'
    
    src_files = sorted(os.listdir(src_dir))
    
    for src_file in src_files:
    
        src_path = os.path.join(src_dir, src_file)
        src_data = _load_csv(src_path)
        # pp.pprint(src_data)
        
        # print(np.shape(src_data))
        
        # for i in range(len(src_data[0])):
        #     print(i)
        #     print('row1', src_data[1][i])
        #     print('row2', src_data[2][i])

        _, ext          = src_file.split('.')
        task            = re.sub(r"\d", "", _)
        scenario_idx    = re.sub(r"\D", "", _)
        
        tgt_dir = '{}/{}_{:02d}'.format(tgt_dir_base, task, int(scenario_idx))
        os.makedirs(tgt_dir, exist_ok = True)
            
        for i in range(len(src_data[0])):
            
            
            turn_idx        = int(re.sub(r"\D", "", src_data[0][i])) - 1
            src_text        = src_data[1][i]

            # if 'LISTEN' == task:
            #     pass
            # else:
            #     continue
            
            # print(src_file)
            # print(task)
            # print(scenario_idx)
            # print(turn_idx)
            # continue
            
            if i % 2 == 0:
                Agent_xml = generate_agent_xml(src_text, lang)
                # print(Agent_xml)
                tgt_file = os.path.join(tgt_dir, 'AgentSpeech_{}_{}.xml'.format(turn_idx, 0))
                # print(tgt_file)
                with open(tgt_file, 'w', encoding = encoding) as f:
                    f.write(Agent_xml)            
            else:
                keyword_csv_yes, keyword_csv_no = generate_keyword_csv(src_data[1][i], src_data[2][i], lang)
                # print('yes : ', keyword_csv_yes)
                # print('no : ', keyword_csv_no)
                
                tgt_file = os.path.join(tgt_dir, 'Keyword_{}_{}.csv'.format(turn_idx, 0))
                # print(tgt_file)
                with open(tgt_file, 'w', encoding = encoding) as f:
                    f.write(keyword_csv_yes)
    
                tgt_file = os.path.join(tgt_dir, 'Keyword_{}_{}.csv'.format(turn_idx, 1))
                # print(tgt_file)
                with open(tgt_file, 'w', encoding = encoding) as f:
                    f.write(keyword_csv_no)

    
def generate_agent_xml(src_text, lang, encoding = 'utf-8'):
    
    text = """<?xml version="1.0" encoding="{}" ?>
<fml-apml>
	<bml>
		<speech id="s1" start="0.0" language="{}" voice="marytts" type="SAPI4" text="">
			<description level="1" type="gretabml">
				<reference>tmp/from-fml-apml.pho</reference>
			</description>
    """.format(encoding, lang)
    
    print(src_text)
    # return src_text
    
    sentence_list = []
    try:
        sentences = src_text.split('.')
    except:
        sentences = [src_text]
        
    for sentence in sentences:
        sentence = sentence + '.'
        try:
            questions = sentence.split('?')
            for question in questions:
                if (question[-1] == '.') or (len(question) == 0):
                    pass
                else:
                    question = question + '?'
                    sentence_list.append(question)
        except:
            sentence = sentence + '.'
            sentence_list.append(sentence)
    
    if len(sentence_list) == 0:
        src_text = src_text + '.'
        sentence_list.append(src_text)
    
    print(sentence_list)
    
    for i, sentence in enumerate(sentence_list):
        
        text += """
            \n 
            <tm id="tm{}"/>
                {}
        """.format(i,sentence)
        
    text += """
            \n
            <tm id="tm{}"/>
    """.format(i+1)

    for i, sentence in enumerate(sentences):
        
        text += """
            \n 
            <boundary type="LL" id="b1" start="s1:tm{}" end="s1:tm{}+0.75"/>
        """.format(i+1, i+1)
        
        text += """
		</speech>
	</bml>
</fml-apml>
        """
    
    return text

def generate_keyword_csv(src_text_yes, src_text_no, lang):
    
    src_text_yes = ','.join(src_text_yes.split('/'))
    # src_text_yes = src_text_yes.replace(' ', '')
    
    src_text_no = ','.join(src_text_no.split('/'))
    # src_text_no = src_text_yes.replace(' ', '')
    
    return src_text_yes, src_text_no
    
def _load_csv(file_name, encoding = 'utf-8'):
    with open(file_name, encoding = encoding) as f:
        reader = csv.reader(f)
        data = [x for x in reader]
    return data

def _write_csv(file_name, data, encoding = 'utf-8'):
    with open(file_name, 'w', encoding = encoding) as f:
        writer = csv.writer(f)
        writer.writerows(data)

if __name__ == '__main__':
    main()