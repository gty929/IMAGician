package edu.umich.imagician

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import org.json.JSONArray
import org.json.JSONException
import kotlin.reflect.full.declaredMemberProperties

object ItemStore {
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
                status = "Granted"
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
                status = "Rejected"
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
                status = "Pending"
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
                            id = postEntry[10] as Int?,
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

    }

    fun getRequestDetail(index: Int) {

    }
}
