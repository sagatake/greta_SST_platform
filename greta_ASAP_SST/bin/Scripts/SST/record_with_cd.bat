REM D:
REM cd D:\Takeshi\SST\SST_main\greta_python
SET mypath=%~dp0
cd /D %mypath%

ffmpeg.exe -y -f dshow -i video="C505e HD Webcam":audio="Microphone (C505e HD Webcam)" -s 1600x900 -r 30 -b 2M temp.mpg

