package com.example.chatapp.cons;

import java.io.Serializable;

public interface SendDataCreateRoomActivity {
    void addUserToGroup(Serializable serializable);

    void deleteUser(String idToDelete);
}
