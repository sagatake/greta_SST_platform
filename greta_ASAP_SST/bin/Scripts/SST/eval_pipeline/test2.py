# -*- coding: utf-8 -*-
"""
Created on Fri Apr 21 11:08:16 2023

@author: Stagiaire
"""

import pprint as pp
import pickle as pkl
import os

from test_module import test_file

GLOBAL_VAL = test_file.GLOBAL_VAL

def main():
    
    # print(dir(test_file))
    
    test_file.main()
    
    # print(test_file.GLOBAL_VAL)
    # global GLOBAL_VAL
    
    print(GLOBAL_VAL)
    
if __name__ == '__main__':
    main()