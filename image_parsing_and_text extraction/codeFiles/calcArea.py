import cv2
import numpy as np
import os

# dirr = "./outimages"
#
# for file in os.listdir(dirr):
#     path  = dirr+"/"+file
def calcArea(img):
    # img = cv2.imread(path)
    gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    edges = cv2.Canny(gray, 40, 100, apertureSize=3)
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    h, s, v = cv2.split(hsv)
    # ret, th = cv2.threshold(s, 10, 20, 0)
    lines = cv2.HoughLinesP(edges, 0.2, np.pi / 180, 1, minLineLength=3, maxLineGap=1)
    allCurves = []
    if(lines is None):
        print("curve not found")
        return 0.0
    else:
        local=[]
        for line in lines:
            x1, y1, x2, y2 = line[0]
            # print((x1, -y1),(x2, -y2))
            local.append((x1, y1))
            local.append((x2, y2))
        local.sort(key=lambda tup: tup[0])
        allCurves.append(local)
    xx= []
    yy=[]
    for curve in allCurves:
        for tple in curve:
            xx.append(tple[0])
            yy.append(tple[1])
    if len(xx) is 0:
        return 0.0
    else:
        area1=  np.trapz(yy,xx)
        totalArea= img.shape[0]*img.shape[1]
        ratioArea = (totalArea-area1)/(totalArea)
        return ratioArea

