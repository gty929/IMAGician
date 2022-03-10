package edu.umich.imagician

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPython()

        val btn:Button = findViewById(R.id.submit)
        val iv:ImageView = findViewById(R.id.image_view)
        val iv2:ImageView = findViewById(R.id.image_view2)
        val txt:TextView = findViewById(R.id.textView4)
        val btn2:Button = findViewById(R.id.button2)

        btn.setOnClickListener { v:View ->
            run{
                val bitmap : Bitmap = iv.drawable.toBitmap()
                val res: Bitmap = StenoAlgo().encode(bitmap,"yyzjason")
                iv2.setImageBitmap(res)
            }


        }


        btn2.setOnClickListener { v:View ->
            run{
                val bitmap2 : Bitmap = iv2.drawable.toBitmap()
                val message: String = StenoAlgo().decode(bitmap2)
                txt.text = message

            }


        }

    }
    /**
     * Jump to the ImportImageActivity
     *
     * @param view
     */
    fun startImportImage(view: View?) = startActivity(Intent(this, ImportImageActivity::class.java))


    private fun initPython(){
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }
    }
}