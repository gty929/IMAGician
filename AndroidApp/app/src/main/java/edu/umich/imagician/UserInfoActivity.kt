package edu.umich.imagician

/**
 * Created by Tianyao Gu on 2022/3/12.
 */


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.umich.imagician.utils.editToStr
import edu.umich.imagician.utils.toast
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class UserInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        setContentView(R.layout.activity_userinfo)
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val fullnameInput = findViewById<EditText>(R.id.fullname_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val phoneInput = findViewById<EditText>(R.id.phone_input)
        val updateButton = findViewById<Button>(R.id.userinfo_update_btn)
        val logoutButton = findViewById<Button>(R.id.userinfo_logout_btn)

        usernameInput.setText(LoginManager.info.username)
        emailInput.setText(LoginManager.info.email)
        phoneInput.setText(LoginManager.info.phoneNumber)
        fullnameInput.setText(LoginManager.info.fullname)

        updateButton.setOnClickListener {
            val newInfo = UserInfo(
                username = LoginManager.info.username,
                email = editToStr(emailInput.text),
                phoneNumber = editToStr(phoneInput.text),
                fullname = editToStr(fullnameInput.text),
            )
            Log.d("NewInfo", Gson().toJson(newInfo))
            ItemStore.httpCall(newInfo.copy()) { code ->
                if (code == 200) {
                    LoginManager.info = newInfo
                    toast("Successfully updated")
                    startActivity(Intent(this, MainActivity::class.java)) // return to main menu
                } else if (code == 403){
                    toast("Please login again")
                    LoginManager.logout(this)
                    startActivity(Intent(this, MainActivity::class.java)) // return to main menu
                } else {
                    toast("Network error, please try again", false)
                }

            }
        }

        logoutButton.setOnClickListener {
            LoginManager.logout(this)
            startActivity(Intent(context, MainActivity::class.java))
        }
    }

}