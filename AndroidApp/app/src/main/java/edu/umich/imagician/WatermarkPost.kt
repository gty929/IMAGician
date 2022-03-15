package edu.umich.imagician

import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import edu.umich.imagician.Sendable.Mode
import org.json.JSONObject
import edu.umich.imagician.ApiStrings.*

// Class of post

class WatermarkPost (var tag: String? = null,
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
                     var usernameFlag: Boolean = false, /** set as false for anonymous posting*/
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

    override fun parse(jsonObjectStr: String) {
        Log.d("PostParser", "Parsing post ${mode}")
        when (mode) {
            Mode.EMPTY -> parseAll(jsonObjectStr)
            else -> {}
        }
    }

    private fun parseAll(jsonObjectStr: String) {
        try {
            val obj = JSONObject(jsonObjectStr)
            val f = { api:ApiStrings -> try {obj.getString(api.field).let { if (it.isEmpty()) null else it }} catch (e: Exception) {null} }
//            if (tag != f(TAG)) { throw error("Incorrect tag!") }

            authorized = (obj.getInt("authorized") == 1)
            msg_encrypted = (obj.getInt("message_encrypted") == 1)
            filename = f(FILE_NAME)
            username = f(CREATOR)
            realName = f(REAL_NAME)
            phoneNumber = f(PHONE)
            email = f(EMAIL)
            timestamp = f(TIME)
            checksum = f(CHECKSUM)
            folder = f(FOLDER_NAME)
            folder_pos = f(FOLDER_POS)
            message = f(MESSAGE)

        } catch (e: Exception) {
            Log.e("UserInfo", "cannot parse JSON string $jsonObjectStr", e)
        }
    }

}