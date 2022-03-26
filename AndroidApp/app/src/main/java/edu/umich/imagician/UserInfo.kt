package edu.umich.imagician

import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import edu.umich.imagician.ApiStrings.*

/**
 * Created by Tianyao Gu on 2022/3/13.
 */
data class UserInfo(var username: String? = null,
                    var email: String? = null,
                    var phoneNumber: String? = null,
                    var fullname: String ?= null): Sendable {

    override suspend fun send(request: RequestBody): Response<ResponseBody> {
        return if (username == null) RetrofitManager.networkAPIs.getUserInfo()
        else RetrofitManager.networkAPIs.updateUserInfo(request)
    }

    override fun parse(responseData: String) {
        try {
            val obj = JSONObject(responseData)
            val f = { api:ApiStrings -> try {obj.getString(api.field)} catch (e: Exception) {null} }
            username = f(USERNAME)
            phoneNumber = f(PHONE_NUMBER)
            email = f(EMAIL)
            fullname = f(FULLNAME)
        } catch (e: Exception) {
            Log.e("UserInfo", "cannot parse JSON string $responseData", e)
        }

    }

    override fun getRequestBodyBuilder(): MultipartBody.Builder {
        return MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(FULLNAME.field, fullname?:"")
            .addFormDataPart(EMAIL.field, email?:"")
            .addFormDataPart(PHONE_NUMBER.field, phoneNumber?:"")
    }
}
