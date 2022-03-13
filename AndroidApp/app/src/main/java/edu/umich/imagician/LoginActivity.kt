package edu.umich.imagician

/**
 * Created by Tianyao Gu on 2022/3/12.
 */


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import edu.umich.imagician.utils.PasswordStrength
import edu.umich.imagician.utils.toast
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity(), TextWatcher {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        var isSignup = false
        setContentView(R.layout.activity_login)
        val login = findViewById<TextView>(R.id.login)
        val signup = findViewById<TextView>(R.id.signup)
        val circleImageView = findViewById<ImageView>(R.id.circleImageView)
        val loginSignupTxt = findViewById<TextView>(R.id.login_signup_txt)
        val loginSignupBtn = findViewById<Button>(R.id.userinfo_update_btn)
        val usernameInput = findViewById<EditText>(R.id.username_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val passwordStrengthHint = findViewById<ConstraintLayout>(R.id.password_strength_hint)
        passwordInput.addTextChangedListener(this)

        login.setOnClickListener {
            login.setTextColor(Color.parseColor("#FFFFFF"))
            login.setBackgroundColor(Color.parseColor("#FF2729C3"))
            signup.setTextColor(Color.parseColor("#FF2729C3"))
            signup.setBackgroundResource(R.drawable.bordershape)
            circleImageView.setImageResource(R.drawable.sigin_boy_img)
            loginSignupTxt.text = getString(R.string.log_in)
            loginSignupBtn.text = getString(R.string.log_in)
            isSignup = false
            passwordStrengthHint.isVisible = false

        }
        login.performClick()
        signup.setOnClickListener {
            signup.setTextColor(Color.parseColor("#FFFFFF"))
            signup.setBackgroundColor(Color.parseColor("#FF2729C3"))
            login.setTextColor(Color.parseColor("#FF2729C3"))
            login.setBackgroundResource(R.drawable.bordershape)
            circleImageView.setImageResource(R.drawable.sigup_boy_img)
            loginSignupTxt.text = getString(R.string.sign_up)
            loginSignupBtn.text = getString(R.string.sign_up)
            isSignup = true
            passwordStrengthHint.isVisible = true
        }
        loginSignupBtn.setOnClickListener {
            if (isSignup && PasswordStrength.calculateStrength(passwordInput.text.toString()) == PasswordStrength.WEAK) {
                toast("The password is too weak")
            } else {
                MainScope().launch {
                    if (LoginManager.loginOrSignup(
                            context,
                            isSignup,
                            usernameInput.text.toString(),
                            passwordInput.text.toString()
                        )
                    ) {
                        startActivity(Intent(context, MainActivity::class.java))
                    }

                }
            }
        }

    }
    override fun afterTextChanged(s: Editable) {}
    override fun beforeTextChanged(
        s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        updatePasswordStrengthView(s.toString())
    }

    private fun updatePasswordStrengthView(password: String) {

        val progressBar = findViewById<ProgressBar>(R.id.progressBar) as ProgressBar
        val strengthView = findViewById<ProgressBar>(R.id.password_strength) as TextView
        if (TextView.VISIBLE != strengthView.visibility)
            return

        if (TextUtils.isEmpty(password)) {
            strengthView.text = ""
            progressBar.progress = 0
            return
        }

        val str = PasswordStrength.calculateStrength(password)
        strengthView.text = str.getText(this)
        strengthView.setTextColor(str.color)

        progressBar.progressDrawable.setColorFilter(str.color, android.graphics.PorterDuff.Mode.SRC_IN)
        if (str.getText(this) == "Weak") {
            progressBar.progress = 25
        } else if (str.getText(this) == "Medium") {
            progressBar.progress = 50
        } else if (str.getText(this) == "Strong") {
            progressBar.progress = 75
        } else {
            progressBar.progress = 100
        }
    }

}