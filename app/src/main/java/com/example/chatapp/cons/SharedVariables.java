package com.example.chatapp.cons;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SharedVariables {
    public static <S extends Serializable> void writeObject(
            final Context context, String key, S serializableObject) {

        ObjectOutputStream objectOut = null;
        try {
            FileOutputStream fileOut = context.getApplicationContext().openFileOutput(key, Activity.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serializableObject);
            fileOut.getFD().sync();
        } catch (IOException e) {
            Log.e("SharedVariable", e.getMessage(), e);
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e) {
                    Log.e("SharedVariable", e.getMessage(), e);
                }
            }
        }
    }
}
