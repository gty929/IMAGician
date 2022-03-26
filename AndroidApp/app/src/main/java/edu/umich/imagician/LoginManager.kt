package edu.umich.imagician

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import edu.umich.imagician.RetrofitManager.networkAPIs
import edu.umich.imagician.RetrofitManager.retrofitExCatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.lang.Exception
import java.time.Instant


/**
 * Created by Tianyao Gu on 2022/3/12.
 */
@ExperimentalCoroutinesApi
object LoginManager {
//    var isLoggedIn = false
    val isLoggedIn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    var info = UserInfo()
    var expiration = Instant.EPOCH
    var cookie: String? = null
        get() {
            return if (Instant.now() >= expiration) null else field
        }
        set(newValue) {
            field = newValue
        }
    private const val ID_FILE = "IMAGician"
    private const val KEY_NAME = "UserID"
    private const val INSTANT_LENGTH = 24
    private const val IV_LENGTH = 12

    init {
        isLoggedIn.value = false
    }

    @ExperimentalCoroutinesApi
    suspend fun loginOrSignup(context: Context, isSignUp: Boolean, username: String, password: String): Boolean {
        if (isLoggedIn.value == true) {
            Log.e("LoginManager:", "already logged in")
            return false
        }

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("username", username)
            .addFormDataPart("password", password).build()
//        val requestBody = JSONObject(jsonObj).toString().toRequestBody("application/json".toMediaType())

        withContext(retrofitExCatcher) {
            // Use Retrofit's suspending POST request and wait for the response
            var response: Response<ResponseBody>? = null
            try {
                response = if (isSignUp) networkAPIs.signup(requestBody) else networkAPIs.login(requestBody)
            } catch (e: Exception) {
                Log.e("login", if (isSignUp) "signup failed" else "login failed", e)
            }
            if (response != null && response.isSuccessful) {
                response.headers()["Set-Cookie"]?.let {
                    cookie = it
                    info.username = username
                    expiration = Instant.now().plusSeconds(21*86400)
                    isLoggedIn.value = true
                    save(context)
                    onGetCookie(context)
                }


            } else {

                Log.e("login", response?.errorBody()?.string() ?: "Retrofit error")
            }

        }
        return isLoggedIn.value == true
    }

    fun logout(context: Context): Boolean {
        info = UserInfo()
        isLoggedIn.value = false
        ItemStore.clear()
        delete(context)
        return true
    }

    /*
    * search for cookie on the device
    * */
    fun open(context: Context, callback: ((success:Boolean)->Unit)? = null) {
        if (expiration != Instant.EPOCH) { // this is not first launch
            return
        }
        try {
            context.getSharedPreferences(ID_FILE, Context.MODE_PRIVATE)
                .getString(KEY_NAME, null)?.let {
                    expiration = Instant.parse(it.takeLast(INSTANT_LENGTH))
                    cookie = it.dropLast(INSTANT_LENGTH)
                    onGetCookie(context, callback)
                    Log.d("get cookie", "Cookie: $cookie, Expiration: $expiration")
                }
        } catch (e: Exception) {
            Log.e("Open cookie failed","Open cookie failed", e)
        }
    }

    private fun onGetCookie(context: Context, callback: ((success:Boolean)->Unit)? = null) {
        RetrofitManager.update(cookie)
        val newInfo = UserInfo()
        ItemStore.httpCall(newInfo) { code -> if (code == 200) {info = newInfo; isLoggedIn.value = true} }
    }

    fun save(context: Context) {
        cookie?.let {
            Log.d("save", "saving")
            val idVal = cookie+expiration.toString()
            context.getSharedPreferences(ID_FILE, Context.MODE_PRIVATE)
                .edit().putString(KEY_NAME, idVal).apply()
            Log.d("saved cookie", "Cookie: $cookie, Expiration: $expiration")
        }
    }

    private fun delete(context: Context) {
        val folder = File(context.getFilesDir().getParent()?.toString() + "/shared_prefs/")
        val files = folder.list()
        files?.forEach {
            context.getSharedPreferences(it.replace(".xml", ""), Context.MODE_PRIVATE)
                .edit().clear().apply() // clear each preference file from memory
            File(folder, it).delete()   // delete the file
        }
    }

}
