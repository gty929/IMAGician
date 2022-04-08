package edu.umich.imagician.utils

import android.graphics.Bitmap
import android.util.Log


/**
 *  Here's how data is stored
 *  Block size width = 5, height = 8
 *
 LocBits        MsgBits
 (one col)     (five cols)

  111   |--------------------|      each pixel stores two bits
  001   |                    |      r -> bit 1   g -> bit 2   b -> bit 1 xor bit 2
  001   |     Tag bits       |
  001   |     28 pixels      |
  001   |     Checksum       |
  001   |     4 pixels       |
  001   |                    |
  001   |--------------------|
 * */


fun ktencode64(oldImg: Bitmap, tag: Long): Bitmap? {

    val msg = tag shl 8 or calcHash(tag)   // 56 bit + 8 bit
    val H = oldImg.height
    val W = oldImg.width
    val newImg = oldImg.copy( Bitmap.Config.ARGB_8888 , true)
    val locBufArr = IntArray(8)
    val msgBufArr = IntArray(32)
    for (w in 0 .. W-5 step 5) {
        for (h in 0 .. H-8 step 8) {
            oldImg.getPixels(locBufArr, 0, 1, w, h, 1, 8)
            locBufArr[0] = locBufArr[0] or 0x10101 // set the last bit of all channels to 1
            for (i in 1 until 8) {
                locBufArr[i] = (locBufArr[i] and -65794) or 1 // set the last bits to 0,0,1
            // we don't use 0, 0, 0 here to avoid confusion with regular bits (which might follow 0 xor 0 == 0)
            }
            oldImg.getPixels(msgBufArr, 0, 4, w+1, h, 4, 8)
            var runningMsg = msg
            for (i in 0 until 32) {
                val b1 = (runningMsg and 1).toInt()
                val b2 = ((runningMsg and 2)).toInt() shr 1
                val b3 = b1 xor b2 // for checksum
                msgBufArr[i] = (msgBufArr[i] and -65794) or (b1 shl 16) or (b2 shl 8) or b3
                runningMsg = runningMsg shr 2
            }
            newImg.setPixels(locBufArr, 0, 1, w, h, 1, 8)
            newImg.setPixels(msgBufArr, 0, 4, w+1, h, 4, 8)
        }
    }
    return newImg
}

fun ktdecode64(img: Bitmap): Long? {
    val H = img.height
    val W = img.width
    var zzoCount = -1 //count of 0, 0, 1 pixel after a 0, 0, 0 pixel is found, initialized to -1 if no 0, 0, 0 found
    val msgBufArr = IntArray(32)
    for (w in 0 .. W-5) {
        for (h in 0 until H) {
            val pixel = img.getPixel(w, h)
            val b1 = (pixel shr 16) and 1
            val b2 = (pixel shr 8) and 1
            val b3 = pixel and 1
            if (b1 and b2 and b3 == 1) {
                zzoCount = 0
            } else if (b1 or b2 == 0 && b3 == 1) {
                if (zzoCount != -1) {
                    ++zzoCount
                    if (zzoCount == 7) { // found all loc bits
                        img.getPixels(msgBufArr, 0, 4, w + 1, h - 7, 4, 8)
                        var msg: Long = 0
                        for (i in 0 until 32) {
                            val bit1 = (msgBufArr[i] shr 16) and 1
                            val bit2 = (msgBufArr[i] shr 8) and 1
                            val bit3 = msgBufArr[i] and 1
                            if (bit1 xor bit2 != bit3) {
                                // check failed, clean up
                                zzoCount = -1
                                break
                            }
                            msg = msg or (((bit2 shl 1) + bit1).toLong() shl (i * 2))
                        }
                        if (zzoCount != -1) { // succeed
                            val tag = msg ushr 8
                            if (calcHash(tag) == msg and 0xFF) {
                                Log.d("Retrieved tag", tag.toString())
                                return tag
                            } else {
                                Log.e("Decode", "hash don't match")
                            }
                            zzoCount = -1
                        }
                    }
                }
            }
        }
        zzoCount = -1
    }
    return null
}

fun calcHash(tag: Long): Long {
    val nuance = 0xB5  // add nuance to avoid false positive for pure black image
    val FF = 0xFF.toLong()
    var hash = nuance.toLong()
    var runningBytes = tag
    for (i in 0..6) {
        hash = hash xor (runningBytes and FF)
        runningBytes = runningBytes shr 8
    }
    return hash
}
