package edu.umich.imagician

// Class of post

class ImgPost (var username: String? = null,
               var filename: String? = null,
               var uri: String? = null,
               var message: String? = null) {
    private lateinit var imgRequestList: ArrayList<ImgRequest>

}