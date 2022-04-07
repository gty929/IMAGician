package edu.umich.imagician

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import androidx.core.view.isVisible
import com.google.gson.Gson
import edu.umich.imagician.databinding.ActivityDisplayInfoBinding
import edu.umich.imagician.utils.decryptMSG
import edu.umich.imagician.utils.editToStr
import edu.umich.imagician.utils.toast
import java.lang.Exception
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

// TODO 3/26: create and link this to a ContactActivity for contacting the author
class DisplayInfoActivity : AppCompatActivity() {
    private lateinit var view: ActivityDisplayInfoBinding
    private lateinit var watermarkPost: WatermarkPost
    private var isModified = false
    private var imageUri: Uri?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityDisplayInfoBinding.inflate(layoutInflater)
        setContentView(view.root)
        watermarkPost = WatermarkPost.post
        isModified = watermarkPost.isModified ?: intent.getBooleanExtra("isModified", false)
        imageUri = watermarkPost.img_uri ?: intent.getParcelableExtra("IMAGE_URI")
        view.imageShow.setImageURI(imageUri)
        view.chipEnter.text = "Encrypted, click to enter the password"
        view.chipDl.text = "Download"

        showEmbeddedInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (LoginManager.isLoggedIn.value == true) {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.displayinfo_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // edit request to author
        return when (item.itemId) {
            R.id.contactMenuText -> {
                val intent = Intent(this, SendRequestActivity::class.java)
                intent.putExtra("IMAGE_URI", imageUri)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onClickEnter(chip: View?) {
        toast("Please enter the password")
        view.chipEnter.isVisible = false
        view.editTextTextPassword.isVisible = true
        val password_button = findViewById<Button>(R.id.confirm_button_field)
        password_button.isVisible = true
//        view.editTextTextPassword.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
//            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
//                // call crypto lib to decrypt
//                //
//                // skip this process
//                toast("Password correct!")
//                view.editTextTextPassword.isVisible = false
//                view.msg.text = "Never gonna give you up"
//                view.msg.isVisible = true
//                view.locked.isVisible = false
//                view.unlocked.isVisible = true
//                return@OnKeyListener true
//            }
//            false
//        })
    }

    fun onClickPasswordConfirm(chip: View?) {
        val pwd = editToStr(view.editTextTextPassword.text)
        val msg = decryptMSG(watermarkPost.message, pwd)
        if (msg == null) {
            view.editTextTextPassword.text.clear()
            view.editTextTextPassword.hint = "Wrong Password"
        } else {
            findViewById<Button>(R.id.confirm_button_field).isVisible = false
            view.editTextTextPassword.isVisible = false
            view.msg.text = msg
            view.msg.isVisible = true
            view.locked.isVisible = false
            view.unlocked.isVisible = true
        }
    }


    fun onClickDownload(chip: View?) {
        // http.GET
        Thread {
            downloadFile(
                URL("https://3.84.195.179/uploads/${watermarkPost.folder_pos}/"),
                "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/${watermarkPost.folder}"
            )
            runOnUiThread {
                toast("The attachment has been saved...")
            }
            val intent = Intent(this, PopUpWindow::class.java)
            intent.putExtra("popuptitle", "Saved")
            intent.putExtra("popuptext", "Attached file saved to Downloads folder.")
            intent.putExtra("popupbtn", "OK")
            intent.putExtra("darkstatusbar", true)
            startActivity(intent)
        }.start()
    }

    private fun downloadFile(url: URL, fileName: String) {
        Log.d("Download file", "url: $url, filename: $fileName")
        try {
            url.openStream().use { Files.copy(it, Paths.get(fileName)) }
        } catch (e: Exception) {
            Log.e("download exception", e.toString())
        }
    }


    private fun showEmbeddedInfo() {
        Log.d("DisplayInfo", "watermarkPost = ${Gson().toJson(watermarkPost).toString()}")
        watermarkPost.isModified = isModified
        watermarkPost.img_uri = imageUri
        // required
        view.jpg.text = watermarkPost.title

        // BOOL check
        view.warning.isVisible = isModified
        if (LoginManager.isLoggedIn.value == true) {
            view.uright.text = watermarkPost.authorized.toString()
        } else {
            view.imageInfo.removeView(view.urRow)
        }
        if (!watermarkPost.msg_encrypted) {
            view.chipEnter.isVisible = false
            view.locked.isVisible = false
            view.msg.isVisible = true
        }


        // optionals
        watermarkPost.username?.let { view.cname.text = it } ?:
        view.imageInfo.removeView(view.cnameRow)
        watermarkPost.fullname?.let { view.rname.text = it } ?:
        view.imageInfo.removeView(view.rnameRow)
        watermarkPost.email?.let { view.eaddr.text = it } ?:
        view.imageInfo.removeView(view.eaddrRow)
        watermarkPost.phoneNumber?.let { view.pnumber.text = it } ?:
        view.imageInfo.removeView(view.pnumRow)
        watermarkPost.timestamp?.let { view.tstamp.text = it } ?:
        view.imageInfo.removeView(view.tsRow)
        watermarkPost.folder?.let { view.zip.text = it } ?:
        view.imageInfo.removeView(view.zipRow)
        watermarkPost.message?.let { view.msg.text = it } ?:
        view.imageInfo.removeView(view.msgRow)

    }
}