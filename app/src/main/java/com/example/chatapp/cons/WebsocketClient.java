package com.example.chatapp.cons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebsocketClient {
    private StompClient mStompClient;
    private Context context;
    private SendData sendData;

    public void connect(String userId, String access_token, Context context) {
        this.context = context;
        sendData = (SendData) context;
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("userId", userId));
        headers.add(new StompHeader("access_token", access_token));

        Log.i("userId", userId);
        Log.i("access_token", access_token);

        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws/websocket");

//        mStompClient.disconnect();

        mStompClient.connect(headers);
        final String[] str = {null};
        mStompClient.topic("/users/queue/messages").subscribe(x -> {
            Log.i(">>>receiver", x.getPayload());
            str[0] = x.getPayload().toString();
            sendData.SendingData(str[0]);
        });




    }


    public void send(String s) {

        mStompClient.send("/app/chat", s)
                .subscribe(() -> {
                    //ok
                }, throwable -> {
                    //error
                });
    }


}