package edu.umich.imagician
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64.DEFAULT
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import java.io.ByteArrayOutputStream
import android.util.Base64


class StenoAlgo {
    private val py: Python = Python.getInstance()
    private val pyo: PyObject = py.getModule("lsb")
    private val chunkSize = 1 /*TO BE DETERMINED*/

    fun encode(bitmap: Bitmap, message: String): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val img: String = Base64.encodeToString(stream.toByteArray(),Base64.DEFAULT)
        val obj: PyObject = pyo.callAttr("LSB_encode", img, message,chunkSize)
        val str: String  = obj.toString()
        val res = Base64.decode(str, Base64.DEFAULT)
        val bmp: Bitmap = BitmapFactory.decodeByteArray(res,0,res.size)

        return bmp
    }

    fun decode(bitmap:Bitmap):String {
        val stream2 = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream2)
        val img2: String = Base64.encodeToString(stream2.toByteArray(),Base64.DEFAULT)
        val obj3: PyObject = pyo.callAttr("LSB_decode", img2, chunkSize)
        return obj3.toString()

    }



}