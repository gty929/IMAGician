import cv2
from PIL import Image
import base64
import io
import numpy as np
import hashlib

DELIMITER = "###"


def LSB_encode(data, message, chunk_size):
    message += DELIMITER
    binary_message = msgToBinary(message)
    data_len = len(binary_message)
    data_index = 0
    img = get_data(data)

    H,W,C = img.shape
    max_bytes = (H//chunk_size) * (W//chunk_size) * 1 / 8
    if len(message) > max_bytes:
        raise RuntimeError(f"message({len(message)}) too large! ({max_bytes})")

    for h in range(H//chunk_size):
        for w in range(W//chunk_size):
            for c in range(C):
               for i in range(h*chunk_size,h*chunk_size+chunk_size):
                    for j in range(w*chunk_size,w*chunk_size+chunk_size):
                        img[i][j][c] = int(msgToBinary(img[i][j][c])[:-1]+binary_message[data_index],2)
               data_index = (data_index+1)%data_len


    return wrap_data(img)





def LSB_decode(data, chunk_size):
    img = get_data(data)
    message_byte = ""
    H,W,C = img.shape

    for h in range(H//chunk_size):
        for w in range(W//chunk_size):
            for c in range(C):
                tmp = {"1":0, "0":0}
                for i in range(h*chunk_size,h*chunk_size+chunk_size):
                    for j in range(w*chunk_size,w*chunk_size+chunk_size):
                        tmp[msgToBinary(img[i][j][c])[-1]] += 1
                message_byte += "1" if tmp["1"] > tmp["0"] else "0"
    message_byte = [ message_byte[i:i+8] for i in range(0,len(message_byte),8) ]
    message_whole = ""
    for byte in message_byte:
        message_whole += chr(int(byte,2))
    result = {}
    message_whole = message_whole.split(DELIMITER)
    for msg in message_whole:
        if not msg in result:
            result[msg] = 0
        result[msg] += 1
    message = max(result, key=result.get)
#     print(message)
    return message



def get_checkSum(data):
    img = get_data(data)
    res = str(np.sum(img)).encode()
    return hashlib.sha256(res).hexdigest()



def check_modified(data,checksum):
    checksum1 = get_checkSum(data)
    return checksum1 == checksum



def msgToBinary(message):
    if type(message) == str:
        return ''.join([format(ord(i),"08b") for i in message])
    elif type(message) == int or type(message) == np.uint8:
        return format(message,"08b")
    else:
        raise RuntimeError("wrong type of message")



def get_data(data):
    data = base64.b64decode(data)
    np_data = np.frombuffer(data,np.uint8)
    img = cv2.imdecode(np_data,cv2.IMREAD_UNCHANGED)
    # Difference Channel order in PIL and CV2
    # Swap the image channel 0 and 2
    tmp = img[:,:,0].copy()
    img[:,:,0] = img[:,:,2]
    img[:,:,2] = tmp
    return img


def wrap_data(data):
    pil_im = Image.fromarray(data)
    buff = io.BytesIO()
    pil_im.save(buff,format="PNG")
    img_str = base64.b64encode(buff.getvalue())
    return str(img_str,'utf-8')