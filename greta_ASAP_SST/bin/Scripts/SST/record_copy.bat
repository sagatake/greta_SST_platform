set time2=%time: =0%


rem copy C:\Users\hirok\OneDrive\Desktop\SST_v2\SST2020\Roleplay\src_video\temp.mpg C:\Users\hirok\OneDrive\Desktop\SST_v2\SST2020\Roleplay\record_video\%date:~-10,4%%date:~-5,2%%date:~-2,2%_%time2:~0,2%%time2:~3,2%%time2:~6,2%.mpg 


ffmpeg -y -r 30 -i temp.mpg -r 3 -s 320x180 -b 1M src_video\temp_user.avi

ffmpeg -y -i temp.mpg -acodec pcm_s16le -ac 1 -ar 16000 -f wav src_audio\temp_user.wav

echo Copy recorded files finished!!