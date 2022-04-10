package edu.umich.imagician

import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import edu.umich.imagician.ApiStrings.*
import edu.umich.imagician.Sendable.Mode
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Class of post

open class WatermarkPost (var tag: String? = null,
                          var username: String? = null,
                          var fullname: String? = null,
                          var title: String? = null,
                          var email: String? = null,
                          var phoneNumber: String? = null,
                          var file: File? = null,
                          var filename: String? = null,
                          var message: String? = null,
                          var msg_encrypted: Boolean = false,
                          var folder: String? = null,
                          var folder_pos: String? = null,
                          var timestampFlag: Boolean = false,
                          var usernameFlag: Boolean = false,/** set as false for anonymous posting*/
                          var fullnameFlag: Boolean = false,
                          var emailFlag: Boolean = false,
                          var phoneFlag: Boolean = false,
                          var timestamp: String? = null,
                          var numPending: Int? = null,
                          var checksum: String? = null,
                          var authorized: Boolean = false,
                          var img_uri: Uri? = null,
                          var isModified: Boolean? = null,
                          var mode: Mode = Mode.IDLE) : Sendable {

    val pendingRequestList = arrayListOf<WatermarkRequest?>()
    lateinit var body: MultipartBody.Builder

    companion object CompanionObject {
        var post = WatermarkPost()
    }

    override suspend fun send(request: RequestBody): Response<ResponseBody>? {
        Log.d("PostSender", "Send post $mode")
        return when (mode) {
            Mode.EMPTY -> RetrofitManager.networkAPIs.getWatermark(tag!!)
            Mode.FULL -> RetrofitManager.networkAPIs.postWatermark(request)
            Mode.LAZY -> RetrofitManager.networkAPIs.getPostDetail(tag!!)
            else -> null
        }

    }

    override fun getRequestBodyBuilder(): MultipartBody.Builder {
        body = MultipartBody.Builder().setType(MultipartBody.FORM)
        Log.d("PostBuilder", "Building post $mode")
        return when (mode) {
            Mode.FULL -> body
                .addFormDataPart(TAG.field, this.tag?:"")
                .addFormDataPart(FILE_NAME.field, this.title?:"")
                .addFormDataPart(CHECKSUM.field, this.checksum?:"")
                .addFormDataPart("username_public", this.usernameFlag.compareTo(false).toString())
                .addFormDataPart("fullname_public", this.fullnameFlag.compareTo(false).toString())
                .addFormDataPart("email_public", this.emailFlag.compareTo(false).toString())
                .addFormDataPart("phone_public", this.phoneFlag.compareTo(false).toString())
                .addFormDataPart("time_public",this.timestampFlag.compareTo(false).toString())
                .addFormDataPart(MESSAGE.field, this.message?:"")
                .addFormDataPart("message_encrypted", this.msg_encrypted.compareTo(false).toString())
                .also { if (this.file != null)
                    it.addFormDataPart("file", "${this.filename}",
                    this.file!!.asRequestBody())}

            else -> body.addFormDataPart(TAG.field, this.tag?:"")
        }

    }

    override fun parse(responseData: String) {
        Log.d("PostParser", "Parsing post $responseData with $mode mode")
        when (mode) {
            Mode.EMPTY -> parseAll(responseData)
            Mode.LAZY -> parseReqs(responseData)
            else -> {}
        }
        Log.d("PostParser", "Parsed post to ${Gson().toJson(this)}")
    }

    private fun parseAll(jsonObjectStr: String) {
        // parse all except pendingReqList
        try {
            val obj = JSONObject(jsonObjectStr)
            val f = { api:ApiStrings -> try {obj.getString(api.field).let { it.ifEmpty { null } }} catch (e: Exception) {null} }
//            if (tag != f(TAG)) { throw error("Incorrect tag!") }
            tag = f(TAG)
            authorized = try { obj.getInt("authorized") == 1 } catch (e: Exception) {false}
            msg_encrypted = try { (obj.getInt("message_encrypted") == 1)} catch (e: Exception) {false}
            numPending = try { (obj.getInt("num_pending"))} catch (e: Exception) {0}
            title = f(FILE_NAME)
            username = f(CREATOR) ?: "anonymous"
            fullname = f(FULLNAME)
            phoneNumber = f(PHONE)
            email = f(EMAIL)
            timestamp = f(TIME)
            timeExchange()
            checksum = f(CHECKSUM)
            folder = f(FOLDER_NAME)
            folder_pos = f(FOLDER_POS)
            message = f(MESSAGE)

        } catch (e: Exception) {
            Log.e("UserInfo", "cannot parse JSON string $jsonObjectStr", e)
        }
    }

    private fun timeExchange() {
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

    private fun parseReqs(responseData: String) {
        // pendingRequestList.clear() // do this before
        try {
            val objs = JSONObject(responseData).getJSONArray("requests")
            for (i in 0 until objs.length()) {
                val watermarkRequest = WatermarkRequest()
                watermarkRequest.parse(objs.getJSONObject(i).toString())
                pendingRequestList.add(watermarkRequest)
            }

        } catch (e: Exception) {
            Log.e("Watermark Post", "cannot parse JSON string $responseData", e)
        }
    }

}