REM cd C:\Users\Stagiaire\Desktop\5_France_ISIR\test
SET mypath=%~dp0
cd /D %mypath%

call activate py37_wsd

REM call python wordnet_sentence-embed_server.py fr
call python wordnet_sentence-embed_server.py %1