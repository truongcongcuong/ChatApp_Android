package com.example.chatapp.cons;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompCommand;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;

public class WebSocketClient {
    private static StompClient stompClient;
    private static WebSocketClient instance = null;
    private static final List<String> subscribes;

    static {
        subscribes = new ArrayList<>();
        subscribes.add("/users/queue/messages");
        subscribes.add("/users/queue/friendRequest/received");
        subscribes.add("/users/queue/friendRequest/accept");
        subscribes.add("/users/queue/friendRequest/recall");
        subscribes.add("/users/queue/friendRequest/delete");
        subscribes.add("/users/queue/room/members/add");
        subscribes.add("/users/queue/room/members/delete");
        subscribes.add("/users/queue/room/rename");
        subscribes.add("/users/queue/room/changeImage");
        subscribes.add("/users/queue/room/members/admin/setNew");
        subscribes.add("/users/queue/room/members/admin/recall");
        subscribes.add("/users/queue/read");
        subscribes.add("/users/queue/reaction");
        subscribes.add("/users/queue/messages/delete");
        subscribes.add("/users/queue/friends/delete");
    }

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

    @SuppressLint("CheckResult")
    public static void logout() {
        for (String sub : subscribes) {
            stompClient
                    .send(new StompMessage(StompCommand.UNSUBSCRIBE,
                            Collections.singletonList(new StompHeader(StompHeader.ID, stompClient.getTopicId(sub))),
                            null))
                    .subscribe(() -> Log.d("unsubscribe", sub + " --- ok"));
        }
        instance = null;
        stompClient = null;
    }

    public StompClient getStompClient() {
        return stompClient;
    }

    public void connect(String userId, String access_token) {
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("userId", userId));
        headers.add(new StompHeader("access_token", access_token));

        Log.i("userId", userId);
        Log.i("access_token", access_token);

        stompClient.connect(headers);

    }

}