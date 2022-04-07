package edu.umich.imagician.utils

import android.graphics.Bitmap
import android.util.Log
import kotlin.experimental.and


fun ktencode(oldImg: Bitmap, message: String): Bitmap? {
        val msg = "$message###".toByteArray()
        val msgLen = msg.size
        val H = oldImg.height
        val W = oldImg.width
        val newBitmap = oldImg.copy( Bitmap.Config.ARGB_8888 , true)
        var msgPos = 0
        var bitPos = 0
        for (w in 0 until W) {
            for (h in 0 until H) {
                val newColor = oldImg.getPixel(w, h).let {
                    if (msg[msgPos] and (1 shl bitPos).toByte() == 0.toByte()) {
//                        it
                        it and -65794
                    } else {
//                        it
                        it or 65793
                    }
                }
                newBitmap.setPixel(w, h, newColor)
                if (bitPos == 7) {
                    bitPos = 0
                    ++msgPos
                    if (msgPos == msgLen) {
                        msgPos = 0
                    }
                } else {
                    ++bitPos
                }
            }
        }
        return newBitmap
    }

    fun ktdecode(oldImg: Bitmap): String? {
        val H = oldImg.height
        val W = oldImg.width
        var msgStartingPos = -1
        var bitPos = 0
        var msg = ""
        var accByte = 0
        for (w in 0 until W) {
            for (h in 0 until H) {

                val bit = oldImg.getPixel(w, h) and 1
                if (bitPos == 7) {
                    bitPos = 0
                    msg += accByte.toChar()
                    if (msg.length >= 3 && msg.substring(msg.length - 3) == "###") {
                        if (msgStartingPos == -1) {
                            msgStartingPos = msg.length
                        } else {
                            return msg.substring(msgStartingPos, msg.length - 3)
                        }
                    }
                } else {
                    accByte += (bit shl bitPos)
                    ++bitPos
                }
            }
            if (w % 50 == 0){
                Log.d("Progress: line = ", "$w")
            }
        }
        return null
    }
