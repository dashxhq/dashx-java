package com.dashx;

public final class Constants {
    public static final String PACKAGE_NAME = "com.dashx";
    public static final String DEFAULT_INSTANCE = "default";
    public static final String DATA = "data";

    public static final class UserAttributes {
        private UserAttributes() {}

        public static final String UID = "uid";
        public static final String ANONYMOUS_UID = "anonymousUid";
        public static final String EMAIL = "email";
        public static final String PHONE = "phone";
        public static final String NAME = "name";
        public static final String FIRST_NAME = "firstName";
        public static final String LAST_NAME = "lastName";
    }

    public static final class RequestType {
        private RequestType() {}

        public static final String PUT = "PUT";
    }

    public static final class FileConstants {
        private FileConstants() {}

        public static final String CONTENT_TYPE = "Content-Type";
        public static final String IMAGE_CONTENT_TYPE = "image/*";
        public static final String VIDEO_CONTENT_TYPE = "video/*";
        public static final String FILE_CONTENT = "*/*";
    }

    public static final class UploadConstants {
        private UploadConstants() {}

        public static final long POLL_INTERVAL = 3000L;
        public static final int POLL_TIME_OUT = 10;
        public static final String READY = "ready";
        public static final String WAITING = "waiting";
    }
}
