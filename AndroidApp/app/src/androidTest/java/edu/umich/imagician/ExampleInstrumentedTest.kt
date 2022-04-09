package edu.umich.imagician

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import edu.umich.imagician.utils.ktdecode64
import edu.umich.imagician.utils.ktencode64
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("edu.umich.imagician", appContext.packageName)
    }

    @Test
    fun testEncodeDecode() {
        val W = 500
        val H = 500
        val img: Bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        for (w in 0 until W) {
            for (h in 0 until H) {
                img.setPixel(w, h, -((w + h)%10 + 1))
            }
        }
        val imgArr = bitMap2Array(img)
        val msg = 0x01020304050403
//        val msgByteArray = ("###$msg").toByteArray()
//        var msgBitStr = ""
//        for (b in msgByteArray) {
//            msgBitStr += Integer.toBinaryString(b.toInt()).reversed()
//                .let { it + "0".repeat(8 - it.length) } // little endian
//        }
        val encodeImg = ktencode64(img, msg)

        assert(encodeImg != null)
        val encodeImgArr = bitMap2Array(encodeImg!!)
        val decodeMsg = ktdecode64(encodeImg)
        Log.d("Decode msg: ", "${decodeMsg}$")
        assert(decodeMsg == msg)
        val cropImg: Bitmap = Bitmap.createBitmap(encodeImg, 5, 5, encodeImg.width -9, encodeImg.height - 8)
        val decodeCropMsg = ktdecode64(cropImg)
        assert(decodeCropMsg == msg)

    }
    @Test
    fun testInt64EncodeDecode() {
        val W = 10
        val H = 10
        val img: Bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        for (w in 0 until W) {
            for (h in 0 until H) {
                img.setPixel(w, h, -((w + h) % 10 + 1))
            }
        }
        val encodeImg = ktencode64(img, 0x1234567)
        if (encodeImg != null) {
            val arr = bitMap2Array(encodeImg)
            print(arr)
        }

    }
    fun bitMap2Array(img: Bitmap): Array<Array<IntArray>> {
        val arr = Array(img.width) { Array(img.height) { IntArray(3) } }
        for (w in 0 until img.width) {
            for (h in 0 until img.height) {
                val colorInt = img.getPixel(w, h)
                arr[w][h][0] = (colorInt shr 16) and 0xFF
                arr[w][h][1] = (colorInt shr 8) and 0xFF
                arr[w][h][2] = colorInt and 0xFF
            }
        }
        return arr
    }

    fun bitMapSpeedTest() {
        System.gc()
        var map = createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        val elapsedHfirst = measureTimeMillis {
            for (h in 0 until 2000) {
                for (w in 0 until 2000) {
                    map.setPixel(w, h, map.getPixel(w, h)+1)
                }
            }
        }
        System.gc()
        map = createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        val elapsedWfirst = measureTimeMillis {
            for (w in 0 until 2000) {
                for (h in 0 until 2000) {
                    map.setPixel(w, h, map.getPixel(w, h)+1)
                }
            }
        }

        Log.i("Time: ","W first $elapsedWfirst ms, H first $elapsedHfirst ms")
    }
}