REM D:
REM cd D:\Takeshi\SST\SST_main\greta_python
SET mypath=%~dp0
cd /D %mypath%

REM SET arg1 = %1
REM echo %arg1%

call activate pytorch-SST
python eval_pipeline\multi_pipeline.py %1 %2 %3 1 1 