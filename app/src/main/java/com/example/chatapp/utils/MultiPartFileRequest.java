package com.example.chatapp.utils;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.example.chatapp.enumvalue.MessageType;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * dùng để upload một hoặc nhiều file lên server
 */
public class MultiPartFileRequest<T> extends Request<T> {

    private final Map<String, String> mStringParts;
    private final List<File> mFileParts;
    private MultipartEntityBuilder mBuilder;
    private final Response.Listener<T> mListener;
    private final List<String> images = List.of("jpg", "jpeg", "png", "gif", "bmp");
    private final List<String> videos = List.of(
            "mp4", //MP4
            "mpg", "mp2", "mpeg", "mpeg", "mpv", // MPEG-1
            "m2v", // MPEG-2
            "3gp", "3g2" // 3gp
    );

    public MultiPartFileRequest(String url,
                                Map<String, String> stringParts,
                                List<File> mFileParts,
                                Response.Listener<T> listener,
                                Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mStringParts = stringParts;
        this.mFileParts = mFileParts;
        buildMultipartEntity();
    }

    private void buildMultipartEntity() {
        if (mBuilder != null) {
            mBuilder = null;
        }
        mBuilder = MultipartEntityBuilder.create();
        mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        mBuilder.setBoundary("_____" + System.currentTimeMillis() + "_____");
        mBuilder.setCharset(Consts.UTF_8);
        if (mStringParts != null) {
            for (Map.Entry<String, String> entry : mStringParts.entrySet()) {
                mBuilder.addTextBody(entry.getKey(), entry.getValue(), ContentType.create("text/plain", StandardCharsets.UTF_8));
            }
        }

        Log.e("Size", "Size: " + mFileParts.size());
        for (File file : mFileParts) {
            ContentType imageContentType;
            /*
            set type cho file IMAGE, VIDEO, FILE
             */
            if (images.contains(FilenameUtils.getExtension(file.getName()).toLowerCase())) {
                imageContentType = ContentType.create(MessageType.IMAGE.toString());
            } else if (videos.contains(FilenameUtils.getExtension(file.getName()).toLowerCase())) {
                imageContentType = ContentType.create(MessageType.VIDEO.toString());
            } else {
                imageContentType = ContentType.create(MessageType.FILE.toString());
            }
            mBuilder.addBinaryBody("files", file, imageContentType, file.getName());
        }
    }

    @Override
    public String getBodyContentType() {
        return mBuilder.build().getContentType().getValue();
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mBuilder.build().writeTo(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    public HttpEntity getEntity() {
        return mBuilder.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return (Response<T>) Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

}