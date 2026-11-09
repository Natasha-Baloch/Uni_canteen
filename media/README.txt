Login screen background video (looping, with sound).
  frames/  - video frames (24 fps), audio.wav - matching sound.
Full video is used (no trimming), 9.4 s.
To re-cut, run (replace input.mp4):
  ffmpeg -i input.mp4 -vf "fps=24,scale=1280:-2" -q:v 4 frames/f%04d.jpg
  ffmpeg -i input.mp4 -vn -ac 2 -ar 44100 -c:a pcm_s16le audio.wav
Run the app from the UniCanteen folder so the media folder is found.
