package edu.umich.imagician

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Response
import java.lang.Exception
import kotlin.reflect.full.declaredMemberProperties

object ItemStore {

    val watermarkCreations= WatermarkCreations()
    val requests = arrayListOf<WatermarkRequest?>()
    val posts = arrayListOf<WatermarkPost?>()

    private val nReqFields = WatermarkRequest::class.declaredMemberProperties.size
    private val nPosFields = WatermarkPost::class.declaredMemberProperties.size

    private lateinit var reqQueue: RequestQueue
    private lateinit var posQueue: RequestQueue
    private const val serverUrl = "https://35.192.222.203/"

    fun fakeItems() {
        requests.clear()
        posts.clear()
        for (i in 0 until 3) {
            requests.add(
                WatermarkRequest(
                watermarkPost = WatermarkPost(filename = "fake.bmp"),
                timestamp = "yyyy/dd/mm, time",
                status = "GRANTED"
            ))
            posts.add(WatermarkPost(
                filename = "image.bmp",
                timestamp = "yyyy/dd/mm, time",
                numPending = 0
            ))
        }
        for (i in 0 until 3) {
            requests.add(WatermarkRequest(
                watermarkPost = WatermarkPost(filename = "fake.bmp"),
                timestamp = "yyyy/dd/mm, time",
                status = "REJECTED"
            ))
            posts.add(WatermarkPost(
                    filename = "image.bmp",
                    timestamp = "yyyy/dd/mm, time",
                    numPending = 2
            ))
        }
        for (i in 0 until 3) {
            requests.add(WatermarkRequest(
                watermarkPost = WatermarkPost(filename = "fake.bmp"),
                timestamp = "yyyy/dd/mm, time",
                status = "PENDING"
            ))
            posts.add(WatermarkPost(
                    filename = "image.bmp",
                    timestamp = "yyyy/dd/mm, time",
                    numPending = 3
            ))
        }
    }

    fun getRequests(context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(serverUrl+"getrequests/",
            { response ->
                requests.clear()
                val requestsReceived = try { response.getJSONArray("requests") } catch (e: JSONException) { JSONArray() }
                for (i in 0 until requestsReceived.length()) {
                    val requestEntry = requestsReceived[i] as JSONArray
                    if (requestEntry.length() == nReqFields) {
                        requests.add(WatermarkRequest(
                            id = requestEntry[10] as Int?,
                            watermarkPost = WatermarkPost(filename = requestEntry[0].toString()),
                            status = requestEntry[1].toString(),
                            timestamp = requestEntry[2].toString(),
                            message = requestEntry[3].toString(),
                            sender = requestEntry[4].toString()
                        ))
                    } else {
                        Log.e("getRequests", "Received unexpected number of fields: " + requestEntry.length().toString() + " instead of " + nReqFields.toString())
                    }
                }
                completion()
            }, { completion() }
        )

        if (!this::reqQueue.isInitialized) {
            reqQueue = newRequestQueue(context)
        }
        reqQueue.add(getRequest)
    }

    fun getPost(context: Context, completion: () -> Unit) {
        val getPost = JsonObjectRequest(serverUrl+"getposts/",
            { response ->
                posts.clear()
                val postsReceived = try { response.getJSONArray("posts") } catch (e: JSONException) { JSONArray() }
                for (i in 0 until postsReceived.length()) {
                    val postEntry = postsReceived[i] as JSONArray
                    if (postEntry.length() == nReqFields) {
                        posts.add(WatermarkPost(
                            tag = postEntry[10].toString(),
                            filename = postEntry[0].toString(),
                            numPending = postEntry[1] as Int?,
                            timestamp = postEntry[2].toString()))
                    } else {
                        Log.e("getRequests", "Received unexpected number of fields: " + postEntry.length().toString() + " instead of " + nPosFields.toString())
                    }
                }
                completion()
            }, { completion() }
        )

        if (!this::posQueue.isInitialized) {
            posQueue = newRequestQueue(context)
        }
        posQueue.add(getPost)
    }

    fun getPostDetail(index: Int) {
        var watermarkPost = watermarkCreations.posts[index]
        watermarkPost?.pendingRequestList?.add(WatermarkRequest(
            sender = "Ron",
            message = "dsdsds"
        ))
        watermarkPost?.pendingRequestList?.add(WatermarkRequest(
            sender = "Him",
            message = "dsrgggggdsds"
        ))
    }

    fun getRequestDetail(index: Int) {

    }

    /** http wrapper for both post and get during login state, cookie must be provided
     * should add a callback to handle the return code
     * the fields within data can be modified
     * */
    fun httpCall(data: Sendable, callback: (returncode : Int) -> Unit) {
        MainScope().launch {
            withContext(RetrofitManager.retrofitExCatcher) {
                // Use Retrofit's suspending POST request and wait for the response
                var returnCode = 0
                var response: Response<ResponseBody>? = null
                try {
                    val request = data.getRequestBodyBuilder().build()
                    response = data.send(request)
//                    Log.d("Receives response", response?.body()?.string() ?: "")
                } catch (e: Exception) {
                    Log.e("send", "send failed", e)
                }
                if (response != null) {
                    returnCode = response.code()
                    try {
                        if (response.isSuccessful) {
                            data.parse(response.body()?.string()?:"")
                        } else {
                            Log.e("http failure response", response.errorBody()?.string()?:"")
                        }

                    } catch (e: Exception) {
                        Log.e("response parse exception", e.toString())
                    }

                }
                callback(returnCode)

            }
        }
    }

    fun clear() {
        watermarkCreations.clear()
    }

    fun refresh(successCallback: (() -> Unit), failureCallback: (() -> Unit)? = null) {
        httpCall(watermarkCreations) { returncode ->
            if (returncode != 200) {
                if (failureCallback != null) {
                    failureCallback()
                }
                Log.e("watermark Creations", "get watermarkCreations failed")
            } else {
                successCallback()
            }
        }
    }
}
