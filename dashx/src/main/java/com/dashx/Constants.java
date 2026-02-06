package com.dashx;

public final class Constants {
    public static final String PACKAGE_NAME = "com.dashx";
    public static final String DEFAULT_INSTANCE = "default";
    public static final String DEFAULT_BASE_URL = "https://api.dashx.com/graphql";
    public static final String DATA = "data";

    private Constants() {}

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
}
