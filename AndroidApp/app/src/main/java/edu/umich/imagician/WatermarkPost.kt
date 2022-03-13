package edu.umich.imagician

// Class of post

class WatermarkPost (var username: String? = null,
                     var filename: String? = null,
                     var uri: String? = null,
                     var message: String? = null,
                     var timestampFlag: Boolean = false
) {
    private lateinit var watermarkRequestList: ArrayList<WatermarkRequest>

}