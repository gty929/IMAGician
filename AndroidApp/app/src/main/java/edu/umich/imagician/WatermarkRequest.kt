package edu.umich.imagician

import android.util.Log
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import edu.umich.imagician.Sendable.Mode
import edu.umich.imagician.ApiStrings.*

// Class of request
class WatermarkRequest (var id: Int? = null,
                        var watermarkPost: WatermarkPost? = null,
                        var sender: String? = null,
                        var message: String? = null,
                        var timestamp: String? = null,
                        var status: String? = "PENDING",
                        var mode: Mode = Mode.IDLE) : Sendable {

    lateinit var body: MultipartBody.Builder
    companion object CompanionObject {
        var request = WatermarkRequest()
    }

    override suspend fun send(request: RequestBody): Response<ResponseBody>? {
        return when (mode) {
            Mode.EMPTY -> RetrofitManager.networkAPIs.getRequestDetail(id.toString()) // get detail of one request
            Mode.FULL -> RetrofitManager.networkAPIs.postRequest(request) // post new request
            Mode.LAZY -> RetrofitManager.networkAPIs.handleRequest(request) // send action
            else -> null
        }
    }

    override fun getRequestBodyBuilder(): MultipartBody.Builder {
        body = MultipartBody.Builder().setType(MultipartBody.FORM)
        Log.d("RequestBuilder", "Building request $mode")
        return when (mode) {
            Mode.FULL -> body
                .addFormDataPart(IMG_TAG.field, this.watermarkPost?.tag ?:"")
                .addFormDataPart(REQ_MSG.field, this.message?:"")
            Mode.LAZY -> body
                .addFormDataPart(REQ_ID.field, this.id.toString())
                .addFormDataPart(ACTION.field, this.status?:"")
            else -> body.addFormDataPart(REQ_ID.field, this.id.toString())
        }
    }

    override fun parse(responseData: String) {
        Log.d("PostParser", "Parsing post $responseData with $mode mode")
        when (mode) {
            Mode.EMPTY -> parseAll(responseData)
            Mode.IDLE -> parseSelf(responseData)
            else -> {}
        }
        Log.d("PostParser", "Parsed post to ${Gson().toJson(this)}")
    }

    private fun parseAll(jsonObject: String) {
        try {
            val obj = JSONObject(jsonObject)

            watermarkPost = WatermarkPost(mode = Mode.EMPTY)
            watermarkPost!!.parse(obj.getJSONObject("image").toString())
            val req = obj.getJSONObject("request")
            val f = { api:ApiStrings -> try {req.getString(api.field)} catch (e: Exception) {null} }
            id = try {req.getInt(ID.field) } catch (e: Exception) {null} // cannot be null
            timestamp = f(REQ_TIME)
            sender = f(REQUESTER)
            message = f(REQ_MSG)
            status = f(STATUS)

        } catch (e: Exception) {
            Log.e("UserInfo", "cannot parse JSON string $jsonObject", e)
        }
    }

    private fun parseSelf(jsonObject: String) {
        try {
            val obj = JSONObject(jsonObject)
            val f = { api:ApiStrings -> try {obj.getString(api.field)} catch (e: Exception) {null} }
            id = try {obj.getInt(ID.field) } catch (e: Exception) {null} // cannot be null
            timestamp = f(REQ_TIME)
            sender = f(REQUESTER)
            message = f(REQ_MSG)
            status = f(STATUS)

        } catch (e: Exception) {
            Log.e("UserInfo", "cannot parse JSON string $jsonObject", e)
        }
    }


}