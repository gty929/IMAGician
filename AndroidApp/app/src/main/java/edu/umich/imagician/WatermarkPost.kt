package edu.umich.imagician

import java.sql.Timestamp

// Class of post

class WatermarkPost (var username: String? = null,
                     var filename: String? = null,
                     var uri: String? = null,
                     var message: String? = null,
                     var timestamp: String? = null) {
    private lateinit var pendingRequestList: ArrayList<WatermarkRequest>

    fun getPendingNum() : Int {
        var numPending = 0
        if (this::pendingRequestList.isInitialized) {
            numPending = pendingRequestList.size
        }
        return numPending
    }

}