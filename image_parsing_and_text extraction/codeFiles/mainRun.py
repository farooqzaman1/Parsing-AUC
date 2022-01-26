import pytesseract
from pytesseract import Output
import cv2
from codeFiles.segmentation import parseImage
import os
import math
import json
import numpy as np

dictionaryHSV = {
    "greenCombo": [[30, 126, 87], [70, 255, 250]],
    'red': [[0, 92, 212], [10, 255, 255]],
    'blue': [[110, 7, 214], [130, 255, 255]],
    'black': [[0, 0, 0], [10, 10, 40]],
    'another1': [[20, 245, 151], [40, 255, 231]],
    'pink': [[140, 126, 215], [160, 146, 255]]
}


config = '-l eng --oem 1 --psm 3'
outCSV = "../outPutCSV/result.csv"
fout  =  open(outCSV, "w" )
fout.write("FigureName,  AreaUnderCurve, textualDescription \n")
for file in os.listdir("../inputImages"):
    path  = "../inputImages/"+file
    print("Processing:-   ",path)
    dt = {}
    img = cv2.imread(path)
    gray  =cv2.cvtColor(img,  cv2.COLOR_BGR2GRAY)
    hsv_img = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    data = pytesseract.image_to_data(gray, output_type=Output.DICT, config=config)
    colorAssociation = {}
    n_boxes = len(data['level'])
    for i in range(n_boxes):
        word= data['text'][i]
        cnf = data['conf'][i]
        if(word =='' or word ==' '):
            continue
        else:
            word =  word.replace("—", "")
            word =  word.replace("=", "")
            word =  word.replace('“', "")
            (x, y, w, h) = (data['left'][i], data['top'][i], data['width'][i], data['height'][i])
            diction = []
            if(x-35>0 and word!=""):
                roii = hsv_img[y: y+h ,x-35: x+w+20]
                for i in range((roii.shape[0])):
                    for j in range((roii.shape[1])):
                        lst = list(roii[i,j])
                        if(lst[0]==lst[1] and lst[0]==lst[2]):
                            continue
                        if(lst not in diction):
                            diction.append(lst)

                if(len(diction)>0):
                    for k, v in dictionaryHSV.items():
                        r1,r2 = v
                        r1B,  r1G,  r1R  =r1[0] ,  r1[1],  r1[2]
                        r2B , r2G,  r2R  =r2[0] ,  r2[1],  r2[2]
                        for lst in diction:
                            B =  lst[0]
                            G = lst[1]
                            R = lst[2]
                            if( (B<=r2B and B>=r1B)  and (G<=r2G  and G>=r1G) and (R<=r2R and R>=r1R)):
                                if(k!="black"):
                                    keyy =  word+"#_#_#_#__"+k
                                    colorAssociation[keyy] = lst
            theta = math.atan2(h, w)
            deg = math.degrees(theta)
            if (deg <= 44):
                deg = 0
            elif (deg > 44 and deg <= 90):
                deg = 90
            elif (deg > 90 and deg <= 180):
                deg = 180
            elif (deg > 180 and deg <= 270):
                deg = 270
            elif (deg > 270 and deg <= 360):
                deg = 0
            localDictionary = {}
            localDictionary['text'] = word
            localDictionary['box'] = [x, y, w, h]
            localDictionary['rotation'] = deg
            dt[len(dt)] = localDictionary
            jsonn = json.dumps(dt)
            fileName =  os.path.splitext(file)[0]
            outpath  = "../outputJSON/"+fileName+".json"
            fileeee = open(outpath, "w")
            fileeee.write(jsonn)

    areaDictionary  = parseImage(path, colorAssociation, fileName)
    strrout = ""
    counter = 0
    best = ""
    roi=""
    if(areaDictionary is None):
        continue
    else:
        sortedd = [(k, areaDictionary[k]) for k in sorted(areaDictionary, key=areaDictionary.get, reverse=True)]
        for k ,area in sortedd:
            counter+=1
            listt   = k.split("#_#_#_#__")  ## delimeter
            if(counter==1):
                best=listt[0]+" "+listt[1]
                roi = str(np.round(area,2))
                # print("best = ",best)
            strrout+=str(counter)+"_"
            curveInfo = listt[0]+"_"+listt[1]
            strrout+=curveInfo + " area =" +str(np.round(area, 2))+"\t"
        totalString =  file + ","+strrout
        textualDescrip ="precision versus recall area under the curve "+best+" perform better "+ "roi ="+roi
        out = totalString+","+textualDescrip+"\n"
        fout.write(out)

fout.close()


