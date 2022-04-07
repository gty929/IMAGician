package edu.umich.imagician

import android.graphics.Bitmap
import edu.umich.imagician.utils.ktdecode
import edu.umich.imagician.utils.ktencode
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testEncodeDecode() {
        val W = 5
        val H = 4
        val img:Bitmap = Bitmap.createBitmap(5, 4, Bitmap.Config.ARGB_8888)
        for (w in 0 until W) {
            for (h in 0 until H) {
                img.setPixel(w, h, (w+h) * 0x10101)
            }
        }
        val encodeImg = ktencode(img, "a")
        if (encodeImg != null) {
            val decodeMsg = ktdecode(encodeImg)
            print(decodeMsg)
        }
    }
}