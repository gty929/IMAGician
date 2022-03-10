import cv2
from PIL import Image
import base64
import io
import numpy as np


def LSB_encode(data, message, chunk_size):
    message += "###"
    binary_message = msgToBinary(message)
    data_len = len(binary_message)
    data_index = 0
    img = get_data(data)

    H,W,C = img.shape
    max_bytes = (H//chunk_size) * (W//chunk_size) * 1 / 8
    if len(message) > max_bytes:
        raise RuntimeError(f"message({len(message)}) too large! ({max_bytes})")

    for h in range(H//chunk_size-80//chunk_size-1):
        for w in range(W//chunk_size-80//chunk_size-1):
            for c in [-1]:
               for i in range(80+h*chunk_size,80+h*chunk_size+chunk_size):
                    for j in range(80+w*chunk_size,80+w*chunk_size+chunk_size):
                        if data_index < data_len:
                            img[i][j][c] = int(msgToBinary(img[i][j][c])[:-1]+binary_message[data_index],2)
                        else:
                            break
               if data_index < data_len:
                    data_index += 1
               else:
                    break

    assert data_index == data_len

    return wrap_data(img)





def LSB_decode(data, chunk_size):
    img = get_data(data)
    message_byte = ""
    H,W,C = img.shape

    for h in range(H//chunk_size-80//chunk_size-1):
        for w in range(W//chunk_size-80//chunk_size-1):
            for c in [-1]:
                tmp = {"1":0, "0":0}
                for i in range(80+h*chunk_size,80+h*chunk_size+chunk_size):
                    for j in range(80+w*chunk_size,80+w*chunk_size+chunk_size):
                        tmp[msgToBinary(img[i][j][c])[-1]] += 1
                message_byte += "1" if tmp["1"] > tmp["0"] else "0"
    message_byte = [ message_byte[i:i+8] for i in range(0,len(message_byte),8) ]
    message = ""
    for byte in message_byte:
        message += chr(int(byte,2))
        if message[-3:] == "###":
            break
    return message[:-3]

                    



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
    print(data.shape)
    buff = io.BytesIO()
    pil_im.save(buff,format="PNG")
    img_str = base64.b64encode(buff.getvalue())
    return str(img_str,'utf-8')


