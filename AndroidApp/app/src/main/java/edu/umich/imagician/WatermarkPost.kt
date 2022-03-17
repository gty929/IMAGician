package edu.umich.imagician

import android.util.Log
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import edu.umich.imagician.Sendable.Mode
import org.json.JSONObject
import edu.umich.imagician.ApiStrings.*
import java.text.SimpleDateFormat
import java.util.*

// Class of post

open class WatermarkPost (var tag: String? = null,
                          var username: String? = null,
                          var realName: String? = null,
                          var filename: String? = null,
                          var email: String? = null,
                          var phoneNumber: String? = null,
                          var uri: String? = null,
                          var message: String? = null,
                          var msg_encrypted: Boolean = false,
                          var folder: String? = null,
                          var folder_pos: String? = null,
                          var timestampFlag: Boolean = false,
                          var usernameFlag: Boolean = false,/** set as false for anonymous posting*/
                     var realNameFlag: Boolean = false,
                          var emailFlag: Boolean = false,
                          var phoneFlag: Boolean = false,
                          var timestamp: String? = null,
                          var numPending: Int? = null,
                          var checksum: String? = null,
                          var authorized: Boolean = false,
                          var mode: Mode = Mode.IDLE) : Sendable {

    val pendingRequestList = arrayListOf<WatermarkRequest?>()
    lateinit var body: MultipartBody.Builder

    companion object CompanionObject {
        var post = WatermarkPost()
    }

    override suspend fun send(request: RequestBody): Response<ResponseBody>? {
        return when (mode) {
            Mode.EMPTY -> RetrofitManager.networkAPIs.getWatermark(tag!!)
            Mode.FULL -> RetrofitManager.networkAPIs.postWatermark(request)
            else -> null
        }

    }

    override fun getRequestBodyBuilder(): MultipartBody.Builder {
        body = MultipartBody.Builder().setType(MultipartBody.FORM)
        Log.d("PostBuilder", "Building post $mode")
        return when (mode) {
            Mode.FULL -> body
                .addFormDataPart(TAG.field, this.tag?:"")
                .addFormDataPart(FILE_NAME.field, this.filename?:"")
                .addFormDataPart(CHECKSUM.field, this.checksum?:"")
                .addFormDataPart("fullname_public", this.realNameFlag.compareTo(false).toString())
                .addFormDataPart("email_public", this.emailFlag.compareTo(false).toString())
                .addFormDataPart("phone_public", this.phoneFlag.compareTo(false).toString())
                .addFormDataPart("time_public",this.timestampFlag.compareTo(false).toString())
                .addFormDataPart(MESSAGE.field, this.message?:"")
                .addFormDataPart("message_encrypted", this.msg_encrypted.compareTo(false).toString())

            else -> body.addFormDataPart(TAG.field, this.tag?:"")
        }

    }

    override fun parse(responseData: String) {
        Log.d("PostParser", "Parsing post ${responseData} with ${mode} mode")
        when (mode) {
            Mode.EMPTY -> parseAll(responseData)
            else -> {}
        }
        Log.d("PostParser", "Parsed post to ${Gson().toJson(this)}")
    }

    private fun parseAll(jsonObjectStr: String) {
        try {
            val obj = JSONObject(jsonObjectStr)
            val f = { api:ApiStrings -> try {obj.getString(api.field).let { if (it.isEmpty()) null else it }} catch (e: Exception) {null} }
//            if (tag != f(TAG)) { throw error("Incorrect tag!") }

            authorized = try { obj.getInt("authorized") == 1 } catch (e: Exception) {false}
            msg_encrypted = try { (obj.getInt("message_encrypted") == 1)} catch (e: Exception) {false}
            numPending = try { (obj.getInt("num_pending"))} catch (e: Exception) {0}
            filename = f(FILE_NAME)
            username = f(CREATOR)
            realName = f(REAL_NAME)
            phoneNumber = f(PHONE)
            email = f(EMAIL)
            timestamp = f(TIME)
            TimeExchange()
            checksum = f(CHECKSUM)
            folder = f(FOLDER_NAME)
            folder_pos = f(FOLDER_POS)
            message = f(MESSAGE)

        } catch (e: Exception) {
            Log.e("UserInfo", "cannot parse JSON string $jsonObjectStr", e)
        }
    }

    private fun TimeExchange() {
        when (timestamp) {
            null -> timestamp = "Unknown Time"
            else -> {
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("GMT")
                val dt = formatter.parse(timestamp)
                val formatter2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                timestamp = formatter2.format(dt)
            }
        }
    }

}