package edu.umich.imagician

/**
 * Created by Tianyao Gu on 2022/3/12.
 */


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.umich.imagician.utils.toast
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class UserInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        setContentView(R.layout.activity_userinfo)
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val phoneInput = findViewById<EditText>(R.id.phone_input)
        val updateButton = findViewById<Button>(R.id.userinfo_update_btn)
        val logoutButton = findViewById<Button>(R.id.userinfo_logout_btn)

        usernameInput.setText(LoginManager.currUsername)
        emailInput.setText(LoginManager.currEmail)
        phoneInput.setText(LoginManager.currPhone)

        updateButton.setOnClickListener {
            MainScope().launch {
                if (true /**TODO*/) {
                    LoginManager.currEmail = emailInput.text.let { if (it.isEmpty()) null else it.toString()}
                    LoginManager.currPhone = phoneInput.text.let { if (it.isEmpty()) null else it.toString()}
                    toast("Your information has been updated")
                }

            }
        }

        logoutButton.setOnClickListener {
            LoginManager.logout(this)
            startActivity(Intent(context, MainActivity::class.java))
        }
    }

}