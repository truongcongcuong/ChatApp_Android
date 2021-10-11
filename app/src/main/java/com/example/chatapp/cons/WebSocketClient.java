package com.example.chatapp.cons;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebSocketClient {
    private static StompClient stompClient;
    private static WebSocketClient instance = null;

    private WebSocketClient() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, Constant.WEB_SOCKET);
    }

    public static WebSocketClient getInstance() {
        if (instance == null)
            instance = new WebSocketClient();
        if (stompClient == null)
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, Constant.WEB_SOCKET);
        return instance;
    }

    public StompClient getStompClient() {
        return stompClient;
    }

    public void connect(String userId, String access_token) {
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("userId", userId));
        headers.add(new StompHeader("access_token", access_token));


        stompClient.connect(headers);

    }

}