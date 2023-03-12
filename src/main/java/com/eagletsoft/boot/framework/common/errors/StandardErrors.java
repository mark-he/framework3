package com.eagletsoft.boot.framework.common.errors;

/*
* 标准错误信息*/
public class StandardErrors {
    public static ServerError NO = new ServerError(0, null);
    public static ServerError UNKNOWN = new ServerError(1001, "error.unknown");
    public static ServerError UNAVAILABLE = new ServerError(1002, "error.unavailable");
    public static ServerError VALIDATION = new ServerError(1003, "error.validation");
    public static ServerError UNAUTHORIZED = new ServerError(1004, "error.unauthorized");
    public static ServerError AUTHORIZED_WRONG = new ServerError(1005, "error.authorized");

    public static ServerError CLIENT_ERROR = new ServerError(2001, "error.client");
    public static ServerError INTERNAL_ERROR = new ServerError(3001, "error.internal");
    public static ServerError EXTERNAL_ERROR = new ServerError(4001, "error.external");

    public static class ServerError {
        private int status;
        private String message;
        public ServerError(int status, String message) {
            super();
            this.status = status;
            this.message = message;
        }
        public int getStatus() {
            return status;
        }
        public String getMessage() {
            return message;
        }
    }
}
