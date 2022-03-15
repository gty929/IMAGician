package edu.umich.imagician

import android.util.Log
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import edu.umich.imagician.Sendable.Mode

// Class of request

class WatermarkRequest (var id: Int? = null,
                        var watermarkPost: WatermarkPost? = null,
                        var sender: String? = null,
                        var message: String? = null,
                        var timestamp: String? = null,
                        var status: String = "PENDING",
                        var mode: Mode = Mode.IDLE) : Sendable {

    companion object CompanionObject {
        val post = WatermarkRequest()
    }

    override suspend fun send(request: RequestBody): Response<ResponseBody>? {
        return RetrofitManager.networkAPIs.postWatermark(request)
    }

    override fun getRequestBodyBuilder(): MultipartBody.Builder {
        return MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("imgtag", this.watermarkPost?.tag ?:"")
            .addFormDataPart("message", this.message?:"")
    }

    override fun parse(jsonObject: String) {
        try {
            val obj = JSONObject(jsonObject)
            val f = { api:ApiStrings -> try {obj.getString(api.field)} catch (e: Exception) {null} }

        } catch (e: Exception) {
            Log.e("UserInfo", "cannot parse JSON string $jsonObject", e)
        }
    }





}