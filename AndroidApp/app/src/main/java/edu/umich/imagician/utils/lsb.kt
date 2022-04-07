package edu.umich.imagician.utils

import android.graphics.Bitmap
import android.util.Log
import java.lang.StringBuilder
import kotlin.experimental.and


fun ktencode(oldImg: Bitmap, message: String): Bitmap? {
        val msg = "###$message".toByteArray()
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
        val msg = StringBuilder()
        var accByte = 0
        for (w in 0 until W) {
            for (h in 0 until H) {

                val bit = oldImg.getPixel(w, h) and 1
                if (bitPos == 7) {
                    bitPos = 0
                    msg.append(accByte.toChar())
                    accByte = 0
                    if (msg.length >= 3 && msg.substring(msg.length - 3) == "###") {
                        if (msgStartingPos != -1) {
                            val retStr = msg.substring(msgStartingPos, msg.length - 3)
                            if (retStr.length % 2 == 0 && Regex("[0-9a-f]+").matches(retStr)) {
                                return retStr
                            }

                        }
                        msgStartingPos = msg.length

                    }
                } else {
                    accByte += (bit shl bitPos)
                    ++bitPos
                }
            }
        }
        return null
    }
