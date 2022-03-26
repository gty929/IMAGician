package edu.umich.imagician

import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception

object ItemStore {

    val watermarkPosts= WatermarkPosts()
    val watermarkRequests= WatermarkRequests()
//    val requests = arrayListOf<WatermarkRequest?>()
//    val posts = arrayListOf<WatermarkPost?>()

//    private val nReqFields = WatermarkRequest::class.declaredMemberProperties.size
//    private val nPosFields = WatermarkPost::class.declaredMemberProperties.size
//
//    private lateinit var reqQueue: RequestQueue
//    private lateinit var posQueue: RequestQueue
    private const val serverUrl = "https://35.192.222.203/"

    /*
    fun fakeItems() {
        requests.clear()
        posts.clear()
        for (i in 0 until 3) {
            requests.add(
                WatermarkRequest(
                watermarkPost = WatermarkPost(title = "fake.bmp"),
                timestamp = "yyyy/dd/mm, time",
                status = "GRANTED"
            ))
            posts.add(WatermarkPost(
                title = "image.bmp",
                timestamp = "yyyy/dd/mm, time",
                numPending = 0
            ))
        }
        for (i in 0 until 3) {
            requests.add(WatermarkRequest(
                watermarkPost = WatermarkPost(title = "fake.bmp"),
                timestamp = "yyyy/dd/mm, time",
                status = "REJECTED"
            ))
            posts.add(WatermarkPost(
                    title = "image.bmp",
                    timestamp = "yyyy/dd/mm, time",
                    numPending = 2
            ))
        }
        for (i in 0 until 3) {
            requests.add(WatermarkRequest(
                watermarkPost = WatermarkPost(title = "fake.bmp"),
                timestamp = "yyyy/dd/mm, time",
                status = "PENDING"
            ))
            posts.add(WatermarkPost(
                    title = "image.bmp",
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
                            watermarkPost = WatermarkPost(title = requestEntry[0].toString()),
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
                            title = postEntry[0].toString(),
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
*/
    // TODO 3/26
    fun getPostDetail(index: Int) {
        val watermarkPost = watermarkPosts.posts[index]
        watermarkPost?.pendingRequestList?.add(WatermarkRequest(
            sender = "Ron",
            message = "dsdsds"
        ))
        watermarkPost?.pendingRequestList?.add(WatermarkRequest(
            sender = "Him",
            message = "dsrgggggdsds"
        ))
    }

    // TODO 3/26
    fun getRequestDetail(index: Int) {
        val watermarkRequest = watermarkRequests.requests[index]
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
        watermarkPosts.clear()
        watermarkRequests.clear()
    }

    fun refreshWatermarkPosts(successCallback: (() -> Unit), failureCallback: (() -> Unit)? = null) {
        httpCall(watermarkPosts) { returncode ->
            if (returncode != 200) {
                if (failureCallback != null) {
                    failureCallback()
                }
                Log.e("watermark posts", "get watermarkPosts failed")
            } else {
                successCallback()
            }
        }
    }

    fun refreshWatermarkRequests(successCallback: (() -> Unit), failureCallback: (() -> Unit)? = null) {
        httpCall(watermarkRequests) { returncode ->
            if (returncode != 200) {
                if (failureCallback != null) {
                    failureCallback()
                }
                Log.e("watermark requests", "get watermarkRequests failed")
            } else {
                successCallback()
            }
        }
    }
}
