import numpy as np
import cv2
from codeFiles.calcArea import calcArea

def parseImage(path, dictionary, filename):
    returningResult = {}

    im = cv2.imread(path)
    img = cv2.cvtColor(im, cv2.COLOR_BGR2RGB)
    hsv_img = cv2.cvtColor(img, cv2.COLOR_RGB2HSV)
    dictionaryHSV = {
        "greenCombo": [[30, 126, 87], [70, 255, 250]],
        'red': [[0, 92, 212], [10, 265, 255]],
        'blue': [[110, 7, 214], [130, 255, 255]],
        'black': [[0, 0, 0], [10, 10, 40]],
        'another1': [[20, 245, 151], [40, 255, 231]],
        'pink': [[140, 126, 215], [160, 146, 255]]
    }

    # gg = {'green': [[39, 245, 0], [70, 265, 61]],
    #       'anotherGreen': [[50, 143, 87], [100, 265, 295]],
    #       }


    counterrr = 0
    for keyy in dictionary.keys():
        lst  = keyy.split("#_#_#_#__")  ## delimeter
        text = lst[0]
        color = lst[1]
        r1, r2 = dictionaryHSV[color]
        lower = np.array(r1)
        upper = np.array(r2)
        mask = cv2.inRange(hsv_img, lower, upper)
        nm = np.ones((img.shape[0], img.shape[1], img.shape[2]), dtype=np.uint8)
        for i in range(nm.shape[0]):
            for j in range(nm.shape[1]):
                nm[i][j] = (255, 255, 255)
        result = cv2.bitwise_and(nm, nm, mask=mask)
        counterrr+=1
        name = "../intermediateImages/"+filename+"_"+ str(counterrr)+".jpg"
        cv2.imwrite(name, result)
        area = calcArea(result)
        returningResult[keyy]= area

    return returningResult

