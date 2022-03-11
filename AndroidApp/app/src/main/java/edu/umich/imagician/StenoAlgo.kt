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
    private val chunkSize = 5 /*TO BE DETERMINED*/

    fun encode(bitmap: Bitmap, message: String): Bitmap {
        val img: String = BM2Str(bitmap)
        val obj: PyObject = pyo.callAttr("LSB_encode", img, message,chunkSize)
        val str: String  = obj.toString()
        val res = Base64.decode(str, Base64.DEFAULT)
        val bmp: Bitmap = BitmapFactory.decodeByteArray(res,0,res.size)

        return bmp
    }


    fun getChecksum(bitmap:Bitmap): String {
        val img: String = BM2Str(bitmap)
        val obj: PyObject = pyo.callAttr("get_checkSum", img)
        return obj.toString()
    }

    fun decode(bitmap:Bitmap):String {
        val img: String = BM2Str(bitmap)
        val obj: PyObject = pyo.callAttr("LSB_decode", img, chunkSize)
        return obj.toString()

    }

    fun checkModified(bitmap:Bitmap, checksum:String):Boolean {
        val img: String = BM2Str(bitmap)
        val obj: PyObject = pyo.callAttr("check_modified", img, checksum)
        return obj.toBoolean()
    }

    fun BM2Str(bitmap:Bitmap):String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return Base64.encodeToString(stream.toByteArray(),Base64.DEFAULT)
    }



}