package edu.umich.imagician

/**
 * Created by Tianyao Gu on 2022/3/12.
 */


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import edu.umich.imagician.utils.toast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
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

        usernameInput.setText(LoginManager.info.username)
        emailInput.setText(LoginManager.info.email)
        phoneInput.setText(LoginManager.info.phone)

        updateButton.setOnClickListener {
            MainScope().launch {
                val newInfo = UserInfo(
                    username = LoginManager.info.username,
                    email = emailInput.text.let { if (it.isEmpty()) null else it.toString()},
                    phone = phoneInput.text.let { if (it.isEmpty()) null else it.toString()}
                )
                if (LoginManager.updateUserInfo(context, newInfo)) {
                    toast("Your information has been updated")
                } else {

                    /** mock */
                    LoginManager.info = newInfo
                    /** mock */


                    toast("Some error occurs please try again")
                }





            }
        }

        logoutButton.setOnClickListener {
            LoginManager.logout(this)
            startActivity(Intent(context, MainActivity::class.java))
        }
    }

}