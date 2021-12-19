package com.example.chatapp.cons;

public class Constant {
    // Gemonytion
//    public static final String API_SIGNUP = "http://10.0.3.2:8080/api/auth/signup/";
//    public static final String API_AUTH = "http://10.0.3.2:8080/api/auth/";
//    public static final String API_INBOX = "http://10.0.3.2:8080/api/inboxs";
//    public static final String API_CHAT = "http://10.0.3.2:8080/api/messages/inbox/";
//    public static final String API_FRIEND_LIST = "http://10.0.3.2:8080/api/friends";
//    public static final String API_CHAT_TO = "http://10.0.3.2:8080/chat";
//    public static final String API_ME = "http://10.0.3.2:8080/api/user/me";
//    public static final String API_USER = "http://10.0.3.2:8080/api/user/";
//    public static final String API_MESSAGE = "http://10.0.3.2:8080/api/messages/";
//    public static final String WEB_SOCKET = "ws://10.0.3.2:8080/ws/websocket";
//    public static final String API_ROOM = "http://10.0.3.2:8080/api/rooms/";
//    public static final String API_FILE = "http://10.0.3.2:8080/api/file";
//    public static final String API_FRIEND_REQUEST= "http://10.0.3.2:8080/api/friend-request";

    // Android Studio
//    public static final String API_SIGNUP = "http://10.0.2.2:8080/api/auth/signup/";
//    public static final String API_AUTH = "http://10.0.2.2:8080/api/auth/";
//    public static final String API_INBOX = "http://10.0.2.2:8080/api/inboxs";
//    public static final String API_CHAT = "http://10.0.2.2:8080/api/messages/inbox/";
//    public static final String API_FRIEND_LIST = "http://10.0.2.2:8080/api/friends";
//    public static final String API_CHAT_TO = "http://10.0.2.2:8080/chat";
//    public static final String API_ME = "http://10.0.2.2:8080/api/user/me";
//    public static final String API_USER = "http://10.0.2.2:8080/api/user/";
//    public static final String API_MESSAGE = "http://10.0.2.2:8080/api/messages/";
//    public static final String WEB_SOCKET = "ws://10.0.2.2:8080/ws/websocket";
//    public static final String API_ROOM = "http://10.0.2.2:8080/api/rooms/";
//    public static final String API_FILE = "http://10.0.2.2:8080/api/file";
//    public static final String API_FRIEND_REQUEST = "http://10.0.2.2:8080/api/friend-request";
//    public static final String API_BLOCK = "http://10.0.2.2:8080/api/blocks";


    //    public static final String SERVER = "http://chatappmongoelasticbeanstalk-env.eba-qzcfuyxf.ap-southeast-1.elasticbeanstalk.com";
    public static final String SERVER = "https://loadbalancerchatappmongo2gb-1982200755.ap-southeast-1.elb.amazonaws.com";
    //    public static final String SERVER = "http://54.179.42.252:8080";
    public static final String API_SIGNUP = SERVER + "/api/auth/signup/";
    public static final String API_AUTH = SERVER + "/api/auth/";
    public static final String API_INBOX = SERVER + "/api/inboxs";
    public static final String API_CHAT = SERVER + "/api/messages/inbox/";
    public static final String API_FRIEND_LIST = SERVER + "/api/friends";
    public static final String API_CHAT_TO = SERVER + "/chat";
    public static final String API_ME = SERVER + "/api/user/me";
    public static final String API_USER = SERVER + "/api/user/";
    public static final String API_MESSAGE = SERVER + "/api/messages/";
    public static final String WEB_SOCKET = SERVER + "/ws/websocket";
    public static final String API_ROOM = SERVER + "/api/rooms/";
    public static final String API_FILE = SERVER + "/api/file";
    public static final String API_FRIEND_REQUEST = SERVER + "/api/friend-request";
    public static final String API_BLOCK = SERVER + "/api/blocks";

    public class Value {
        public static final int DEFAULT_LANGUAGE_ID = 0;
    }

    public class RequestCode {
        public static final int CHANGE_LANGUAGE = 10000;
    }

}
