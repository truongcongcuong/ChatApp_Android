package com.example.chatapp.cons;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebsocketClient {
    private StompClient mStompClient;

    public void connect(String userId, String access_token) {

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("userId", userId));
        headers.add(new StompHeader("access_token", access_token));

        Log.i("userId", userId);
        Log.i("access_token", access_token);

        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.3.2:8080/ws/websocket");

//        mStompClient.disconnect();

        mStompClient.connect(headers);

    }

    public void send(String s) {
        mStompClient.topic("/users/queue/messages").subscribe(x -> {
            Log.i(">>>receiver", x.getPayload());
        });

        mStompClient.send("/app/chat", s)
                .subscribe(() -> {
                    //ok
                }, throwable -> {
                    //error
                });
    }

}