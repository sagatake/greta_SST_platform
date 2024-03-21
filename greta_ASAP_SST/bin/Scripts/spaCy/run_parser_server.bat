SET mypath=%~dp0
cd /D %mypath%

REM put conda activation here
REM call activate CONDA_ENV_NAME
call activate py37_spacy

call python spacy_client.py %1