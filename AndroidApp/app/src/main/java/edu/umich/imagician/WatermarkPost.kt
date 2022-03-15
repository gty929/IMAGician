package edu.umich.imagician

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.lang.Exception
import java.sql.Timestamp

// Class of post

class WatermarkPost (var id: Int? = null,
                     var username: String? = null,
                     var realName: String? = null,
                     var filename: String? = null,
                     var email: String? = null,
                     var phoneNumber: String? = null,
                     var uri: String? = null,
                     var message: String? = null,
                     var timestampFlag: Boolean = false,
                     var usernameFlag: Boolean = false, /** set as false for anonymous posting*/
                     var realNameFlag: Boolean = false,
                     var emailFlag: Boolean = false,
                     var phoneFlag: Boolean = false,
                     var timestamp: String? = null,
                     var numPending: Int? = null,
                     var checksum: String? = null,
                     var detailed: Boolean = false): Sendable {

    val pendingRequestList = arrayListOf<WatermarkRequest?>()

    companion object CompanionObject {
        val post = WatermarkPost()
    }

    override suspend fun send(request: RequestBody): Response<ResponseBody>? {
        return RetrofitManager.networkAPIs.postWatermark(request)
    }

    override fun getRequestBodyBuilder(): MultipartBody.Builder {
        return MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("username", this.username?:"")
            .addFormDataPart("realname", this.realName?:"")
    }

}