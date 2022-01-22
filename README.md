# CBZMaker

## What is it for ?
This project exists to create cbz or pdf files from list of jpg

## How to use ?
### For Windows users
Download bin/CBZMaker.exe and run following command:</br>
CBZMaker path/to/ComicBookName/ </br>
</br>
Optional Arguments:</br>
 - -pdf : generate .pdf instead of .cbz </br>
 - -MaxPages XXX : limit file to XXX pages </br>
 - -GroupVolumes XX : group volumes in one output file
 - -GrayScale : convert input colored jpg into gray scale image. Very usefull for e-reader (approx 25% bigless)

Required structure of file is 
 - ComicBookName
   - VolumeNumber
     - file01.jpg
     - file02.jpg
     - ...
     
### For other users
you will find binary jar inside bin folder. Please use same arguments as above
