package com.dashx

const val PACKAGE_NAME = "com.dashx"
const val DEFAULT_INSTANCE = "default"

const val DATA = "data"

object UserAttributes {
    const val UID = "uid"
    const val ANONYMOUS_UID = "anonymousUid"
    const val EMAIL = "email"
    const val PHONE = "phone"
    const val NAME = "name"
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
}

object RequestType {
    const val PUT = "PUT"
}

object FileConstants {
    const val CONTENT_TYPE = "Content-Type"
    const val IMAGE_CONTENT_TYPE = "image/*"
    const val VIDEO_CONTENT_TYPE = "video/*"
    const val FILE_CONTENT = "*/*"
}

object UploadConstants {
    const val POLL_INTERVAL: Long = 3000
    const val POLL_TIME_OUT = 10

    const val READY = "ready"
    const val WAITING = "waiting"
}
