REM cd C:\Users\Stagiaire\Desktop\5_France_ISIR\test
SET mypath=%~dp0
cd /D %mypath%

call activate py37_spacy

call python spacy_server.py %1