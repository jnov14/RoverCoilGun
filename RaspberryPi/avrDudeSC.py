#  AVRDude autoloader module  
import subprocess
import sys
# variables that we might change
# # chipset options are atmega328p for the uno and nano, and atmega2560 for the mega 

folder = "/home/pi/outputfiles/"  
extension = ".cpp.hex"  
uploader = "linuxgpio"  
chipset = "atmega328p"  

#functions that we can call 

def uploadtoBoard(filename, chipset): fileparam = "flash:w:" + folder + filename + extension + ":i"  
    subprocess.call(["sudo", "avrdude", "-c", uploader, "-p", chipset, "-v", "-U", fileparam]) 
def changeUploader(s): uploader = s 
def changeChipset(s): chipset = s 
def changeFolder(s): folder = s 
def changeExtension(s): extension = s

# this is  for if we want to make it into its own callable program#  
if __name__ == __main__: 
    filename = raw_input("What file would you like to upload to the board?")
    uploadtoBoard(filename)  
