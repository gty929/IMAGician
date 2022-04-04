from xmlrpc.client import TRANSPORT_ERROR
import cv2
from PIL import Image
import base64
import io
import numpy as np
import hashlib

DELIMITER = "###"

'''
yyzjason: LSB Algorithm (repeating message and split with DELIMITER byte array)
'''
def LSB_encode(data,message,debug=False):
    message += DELIMITER
    binary_message = msgToBinary(message)
    data_len = len(binary_message)
    
    if debug:
        img = data
    else:
        img = get_data(data)

    if LSB_decode(data, check=True):
        return None

    H,W,C = img.shape
    max_bytes = H * W * C / 8
    if len(message) > max_bytes:
        raise RuntimeError(f"message({len(message)}) too large! ({max_bytes})")

    # '''For Method'''
    # data_index = 0
    # for h in range(H):
    #     for w in range(W):
    #         for c in range(C):
    #             img[h,w,c] = int(msgToBinary(img[h,w,c])[:-1]+binary_message[data_index],2)
    #             data_index = (data_index+1)%data_len

    '''Vectorization Method'''
    img = (img >> 1)*2
    bin_msg_vec = np.array([int(b) for b in binary_message], dtype='uint8')
    bin_msg_vec = np.tile(bin_msg_vec,(H*W*C//data_len)+1)[:H*W*C].reshape(H,W,C)
    img += bin_msg_vec

    return wrap_data(img) if not debug else img


def LSB_decode(data, check=False, debug=False):
    if debug or check:
        img = data
    else:
        img = get_data(data)
    message_byte = ""
    flat_img = img.reshape(-1)%2
    message_byte = "".join(map(str, list(flat_img)))

    message_byte = message_byte.split(msgToBinary(DELIMITER))
    if check:
        return len(message_byte) > 256
    result = {}
    for byte in message_byte:
        segment = ""
        for b in [byte[i:i+8] for i in range(0,len(byte),8)]:
            segment += chr(int(b,2))
        if not segment in result:
            result[segment] = 0
        result[segment] += 1
    message = max(result, key=result.get)  
    if len(message) > 20:
        message = ""
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





'''
Old LSB Algorithm (using chunk)
'''
def old_LSB_encode(data, message, chunk_size):
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


def old_LSB_decode(data, chunk_size):
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
    print(result)
    message = max(result, key=result.get)
#     print(message)
    return message



"""
Debug
"""
# import time
# from PIL import Image
# from numpy import asarray


# img = Image.open('org.png')

# img = asarray(img)
# img = np.random.randint(90,255,(100,100,3))

# time_start = time.time()
# embed_img = LSB_encode(img, "abcdefng", debug=True)
# time_end = time.time()
# print(f"embed time: {time_end-time_start}")


# time_start = time.time()
# msg = LSB_decode(embed_img,check=False,debug=True)
# time_end = time.time()
# print(f"decode time: {time_end-time_start}")
# print(f"message: {msg}")


# embed_img = LSB_encode(embed_img, "abcdefng", debug=True)
# assert embed_img is None

