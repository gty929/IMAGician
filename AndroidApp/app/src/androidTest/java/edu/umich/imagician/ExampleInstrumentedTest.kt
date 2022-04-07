package edu.umich.imagician

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.umich.imagician.utils.ktdecode
import edu.umich.imagician.utils.ktencode

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

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
        val W = 10
        val H = 12
        val img: Bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        for (w in 0 until W) {
            for (h in 0 until H) {
                img.setPixel(w, h, -(w+h+1) * 0xFEFEFF)
            }
        }
        val imgArr = bitMap2Array(img)
        val msg = "a1bf"
        val msgByteArray = ("###$msg").toByteArray()
        var msgBitStr = ""
        for (b in msgByteArray) {
            msgBitStr += Integer.toBinaryString(b.toInt()).reversed().let { it + "0".repeat(8 - it.length)} // little endian
        }
        val encodeImg = ktencode(img, msg)

        if (encodeImg != null) {
            val encodeImgArr = bitMap2Array(encodeImg)
            val decodeMsg = ktdecode(encodeImg)
            assert(decodeMsg == msg)
        }
    }

    fun bitMap2Array(img: Bitmap): Array<Array<IntArray>> {
        val arr = Array(img.width) { Array(img.height) {IntArray(3)} }
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
}