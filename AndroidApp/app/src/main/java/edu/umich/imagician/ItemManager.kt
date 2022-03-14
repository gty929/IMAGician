package edu.umich.imagician

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import com.google.gson.Gson
import edu.umich.imagician.utils.toast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Response
import java.lang.Exception
import kotlin.reflect.full.declaredMemberProperties

object ItemManager {
    val requests = arrayListOf<WatermarkRequest?>()
    val posts = arrayListOf<WatermarkPost?>()




    @ExperimentalCoroutinesApi
    suspend fun postDataAfterLogin(context: Context, data: Any): Boolean {
        if (!LoginManager.isLoggedIn) {
            Log.e("LoginManager:", "not logged in")
            return false
        }
        if (LoginManager.cookie == null) {
            Log.e("LoginManager:", "cookie not found")
            return false
        }
//        val requestBody = LoginManager.cookieWrapper(data)
        val requestBody = when(data) {
            is WatermarkPost -> data.toFormData()
            else -> MultipartBody.Builder().setType(MultipartBody.FORM).build()
        }
        return withContext(RetrofitManager.retrofitExCatcher) {
            // Use Retrofit's suspending POST request and wait for the response
            Log.d("Post send","Sending ${Gson().toJson(data)}")

            var response: Response<ResponseBody>? = null
            try {
                response = RetrofitManager.networkAPIs.updateUserInfo(requestBody)
            } catch (e: Exception) {
                Log.e("data post", "post failed", e)
            }
            if (response != null && response.isSuccessful) {
                return@withContext true
            } else {
                Log.e("data post", response?.errorBody()?.string() ?: "Retrofit error")

                /**mock*/
                return@withContext true



                return@withContext false
            }

        }
    }
}
