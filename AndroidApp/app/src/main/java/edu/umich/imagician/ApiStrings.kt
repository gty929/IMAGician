package edu.umich.imagician

/**
 * Created by Tianyao Gu on 2022/3/14.
 */
enum class ApiStrings (val field: String) {
    // account
    USERNAME("username"),
    PASSWORD("password"),
    EMAIL("email"),
    PHONE_NUMBER("phone_number"),
    FULLNAME("fullname"),

    // watermark post
    TAG("tag"),
    FILE_NAME("imgname"),
    CHECKSUM("checksum"),
    FOLDER_NAME("file"),
    FOLDER_POS("folder"),
    MESSAGE("message"),
    CREATOR("owner"),
    PHONE("phone"),
    TIME("time"),
    PENDING("num_pending"),

    // watermark request
    IMG_TAG("imgtag"),
    REQ_TIME("created"),
    REQ_ID("reqid"),
    REQ_MSG("message"),
    STATUS("status"),
    ACTION("action"),
    REQUESTER("username"),
}