REM D:
REM cd D:\Takeshi\SST\SST_main\greta_python
SET mypath=%~dp0
cd /D %mypath%

call activate pytorch-SST
python record_kill.py