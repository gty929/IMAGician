package edu.umich.imagician

import android.content.Context
import android.util.Log
import edu.umich.imagician.RetrofitManager.networkAPIs
import edu.umich.imagician.RetrofitManager.retrofitExCatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.lang.Exception
import java.time.Instant

/**
 * Created by Tianyao Gu on 2022/3/12.
 */
object LoginManager {
    var isLoggedIn = false
    var currUsername: String? = null
    var currEmail: String? = null
    var currPhone: String? = null
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

    @ExperimentalCoroutinesApi
    suspend fun loginOrSignup(context: Context, isSignUp: Boolean, username: String, password: String): Boolean {
        if (isLoggedIn) {
            Log.e("LoginManager:", "already logged in")
            return false
        }
        val jsonObj = mapOf(
            "username" to username,
            "password" to password
        )
        val requestBody = JSONObject(jsonObj).toString().toRequestBody("application/json".toMediaType())

        withContext(retrofitExCatcher) {
            // Use Retrofit's suspending POST request and wait for the response
            var response: Response<ResponseBody>? = null
            try {
                response = if (isSignUp) networkAPIs.signup(requestBody) else networkAPIs.login(requestBody)
            } catch (e: Exception) {
                Log.e("login", if (isSignUp) "signup failed" else "login failed", e)
            }
            if (response != null && response.isSuccessful) {
                val responseObj = JSONObject(response.body()?.string() ?: "")
                // obtain chatterID from back end
                cookie = try {
                    responseObj.getString("cookie")
                } catch (e: JSONException) {
                    null
                }
                expiration = Instant.now().plusSeconds(
                    try {
                        responseObj.getLong("lifetime")
                    } catch (e: JSONException) {
                        0L
                    }
                )

                cookie?.let {
                    isLoggedIn = true
                    currUsername = username
                    save(context)
                }

            } else {
                /*mock */
                cookie = "1234"
                expiration = Instant.now().plusSeconds(86400)
                isLoggedIn = true
                currUsername = username
                Log.d("mock", "mocking login success")
                save(context)

                Log.e("login", response?.errorBody()?.string() ?: "Retrofit error")
            }

        }
        return isLoggedIn
    }


    fun logout(context: Context): Boolean {
        currUsername = null
        isLoggedIn = false
        delete(context)
        return true
    }

    /*
    * search for cookie on the device
    * */
    fun open(context: Context) {
        if (expiration != Instant.EPOCH) { // this is not first launch
            return
        }
        try {
            context.getSharedPreferences(ID_FILE, Context.MODE_PRIVATE)
                .getString(KEY_NAME, null)?.let {
                    expiration = Instant.parse(it.takeLast(INSTANT_LENGTH))
                    cookie = it.dropLast(INSTANT_LENGTH)
                    Log.d("get cookie", "Cookie: $cookie, Expiration: $expiration")
                }
        } catch (e: Exception) {
            Log.e("Open cookie failed","Open cookie failed", e)
        }
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

    fun delete(context: Context) {
        val folder = File(context.getFilesDir().getParent()?.toString() + "/shared_prefs/")
        val files = folder.list()
        files?.forEach {
            context.getSharedPreferences(it.replace(".xml", ""), Context.MODE_PRIVATE)
                .edit().clear().apply() // clear each preference file from memory
            File(folder, it).delete()   // delete the file
        }
    }

}