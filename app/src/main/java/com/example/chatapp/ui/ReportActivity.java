package com.example.chatapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.adapter.LineItemMediaAdapter;
import com.example.chatapp.adapter.LineItemPictureBeforeSendAdapter;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.dto.MyMedia;
import com.example.chatapp.dto.UserProfileDto;
import com.example.chatapp.entity.UserReport;
import com.example.chatapp.enumvalue.MediaType;
import com.example.chatapp.enumvalue.MessageType;
import com.example.chatapp.utils.MultiPartFileRequest;
import com.example.chatapp.utils.PathUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ReportActivity extends AppCompatActivity {

    private MessageDto message;
    private UserProfileDto user;
    private String token;
    private TextInputLayout report_content_txt;
    private List<File> fileList;
    private static final int REQUEST_PERMISSION = 3;
    private static final int PICK_IMAGE = 1;
    private RecyclerView rcv_report_photo;
    private Gson gson;
    private FloatingActionButton btn_continue_report;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ChatApp_SlidrActivityTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // gạt ở cạnh trái để trở về
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            user = (UserProfileDto) bundle.getSerializable("user");
            message = (MessageDto) bundle.getSerializable("message");
        }

        gson = new Gson();
        SharedPreferences sharedPreferencesToken = getApplicationContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPreferencesToken.getString("access-token", null);

        btn_continue_report = findViewById(R.id.btn_continue_report);
        Button btn_report_attach_photo = findViewById(R.id.btn_report_attach_photo);
        rcv_report_photo = findViewById(R.id.rcv_report_photo);
        report_content_txt = findViewById(R.id.report_content_txt);

        Toolbar toolbar = findViewById(R.id.toolbar_report);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        /*
        hiện nút mũi tên quay lại trên toolbar
         */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_continue_report.setOnClickListener(v -> {
            String content;
            if (report_content_txt.getEditText() != null
                    && !(content = report_content_txt.getEditText().getText().toString().trim()).isEmpty()) {
                btn_continue_report.setVisibility(View.GONE);
                report_content_txt.setError(null);
                ProgressDialog progress = new ProgressDialog(this);
                progress.setMessage(getResources().getString(R.string.please_wait));
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setIndeterminate(true);
                progress.setCanceledOnTouchOutside(false);
                progress.setCancelable(false);
                progress.show();
                String toId = user != null ? user.getId() : null;
                String messageId = null;
                if (message != null && message.getId() != null && !message.getType().equals(MessageType.SYSTEM)) {
                    messageId = message.getId();
                    toId = message.getSender() != null ? message.getSender().getId() : null;
                }
                UserReport userReport = UserReport.builder()
                        .content(content)
                        .toId(toId)
                        .messageId(messageId)
                        .build();
                if (fileList == null || fileList.isEmpty()) {
                    sendReportUser(userReport, progress);
                } else {
                    uploadAttachPhotoThenSendReportUser(userReport, fileList, progress);
                }
            } else {
                btn_continue_report.setVisibility(View.VISIBLE);
                report_content_txt.setError(getString(R.string.error_report_content_empty));
                report_content_txt.requestFocus();
            }
        });

        btn_report_attach_photo.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_needed_title))
                            .setMessage(getString(R.string.permission_needed_message))
                            .setPositiveButton(getString(R.string.confirm_button), (dialog, which) ->
                                    ActivityCompat.requestPermissions(ReportActivity.this,
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            REQUEST_PERMISSION))
                            .setNegativeButton(getString(R.string.cancel_button), (dialog, which) -> dialog.cancel()).create().show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                }
            } else {
                openFileChoose();
            }
        });

        LinearLayout layout_report_message = findViewById(R.id.layout_report_message);
        LinearLayout layout_report_user = findViewById(R.id.layout_report_user);

        layout_report_message.setVisibility(View.GONE);
        layout_report_user.setVisibility(View.GONE);

        if (user != null) {
            setTitle(R.string.report_user);
            layout_report_user.setVisibility(View.VISIBLE);

            ImageView image_User = findViewById(R.id.report_user_image);
            TextView txt_user_display_name = findViewById(R.id.report_user_content);
            TextView txt_user_detail = findViewById(R.id.report_user_detail);

            Glide.with(this).load(user.getImageUrl())
                    .centerCrop().circleCrop()
                    .placeholder(R.drawable.img_avatar_placeholer)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(image_User);
            image_User.setBackgroundResource(R.drawable.border_for_circle_image);
            txt_user_display_name.setText(user.getDisplayName());
            txt_user_detail.setText(user.getDisplayName());
        } else if (message != null) {
            setTitle(R.string.report_message);
            layout_report_message.setVisibility(View.VISIBLE);

            TextView report_message_content = findViewById(R.id.report_message_content);
            TextView report_message_time = findViewById(R.id.report_message_time);
            TextView report_message_sender = findViewById(R.id.report_message_sender);
            RecyclerView report_message_rcv_media = findViewById(R.id.report_message_rcv_media);

            if (message.getContent() == null || message.getContent().isEmpty()) {
                report_message_content.setVisibility(View.GONE);
            } else {
                report_message_content.setVisibility(View.VISIBLE);
                report_message_content.setText(message.getContent());
            }
            report_message_time.setText(message.getCreateAt());

            String senderName = message.getSender().getDisplayName();
            if (message.getSender() != null && senderName != null && !senderName.isEmpty()) {
                String s = String.format("%s: %s", getString(R.string.sender), senderName);
                report_message_sender.setText(s);
            }

            List<MyMedia> media = message.getMedia();
            if (media != null) {
                media.sort((o1, o2) -> o1.getType().compareTo(o2.getType()));
                GridLayoutManager layoutManager;
                LineItemMediaAdapter mediaAdapter;
                int maxColumn = Math.min(media.size(), 4);
                layoutManager = new GridLayoutManager(this, maxColumn);
                mediaAdapter = new LineItemMediaAdapter(this, message, maxColumn);
                layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (media.get(position).getType().equals(MediaType.FILE))
                            return maxColumn;
                        return 1;
                    }
                });
                report_message_rcv_media.setLayoutManager(layoutManager);
                report_message_rcv_media.setAdapter(mediaAdapter);
            }
        }
    }

    private void uploadAttachPhotoThenSendReportUser(UserReport userReport, List<File> fileList, Dialog progressDialog) {
        MultiPartFileRequest<String> restApiMultiPartRequest =
                new MultiPartFileRequest<String>(Request.Method.POST, Constant.API_FILE,
                        null, // danh sách request param
                        fileList,
                        response -> {
                            Log.d("--", "respone media report");
                            try {
                                Log.d("--", "try");
                                String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                                Type listType = new TypeToken<List<MyMedia>>() {
                                }.getType();
                                List<MyMedia> media = new Gson().fromJson(res, listType);
                                userReport.setMedia(media);
                                sendReportUser(userReport, progressDialog);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            btn_continue_report.setVisibility(View.VISIBLE);
                            progressDialog.cancel();
                            Log.i("upload error", error.toString());
                        }) {

                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + token);
                        return map;
                    }
                };

        restApiMultiPartRequest.setRetryPolicy(new DefaultRetryPolicy(0, 1, 2));//10000
        Volley.newRequestQueue(this).add(restApiMultiPartRequest);
    }

    private void sendReportUser(UserReport userReport, Dialog progressDialog) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    JSONObject object = new JSONObject(gson.toJson(userReport));
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constant.API_USER + "report", object,
                            response -> {
                                btn_continue_report.setVisibility(View.VISIBLE);
                                progressDialog.cancel();
                                System.out.println("response report = " + response.toString());
                                AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
                                builder.setMessage(R.string.report_success)
                                        .setPositiveButton(R.string.confirm_button, (dialog, id) -> {
                                            dialog.cancel();
                                            onBackPressed();
                                            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.setCancelable(false);
                                dialog.show();
                            }, error -> {
                        btn_continue_report.setVisibility(View.VISIBLE);
                        progressDialog.cancel();
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("Authorization", "Bearer " + token);
                            return map;
                        }
                    };
                    request.setRetryPolicy(new DefaultRetryPolicy(0, 1, 2));//10000
                    Volley.newRequestQueue(ReportActivity.this).add(request);
                } catch (JSONException e) {
                    btn_continue_report.setVisibility(View.VISIBLE);
                    progressDialog.cancel();
                    e.printStackTrace();
                }
            }
        }, 3000);
    }

    private void openFileChoose() {
        Intent intent = new Intent();
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        super.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChoose();
            } else {
                Toast.makeText(this, getString(R.string.allow_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            List<File> files = new ArrayList<>();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    File file = new File(PathUtil.getPath(ReportActivity.this, imageUri));
                    files.add(file);
                }
            } else {
                File file = new File(PathUtil.getPath(ReportActivity.this, data.getData()));
                files.add(file);
            }
            Log.d("--file da chon", files.toString());
            fileList = files;
            rcv_report_photo = findViewById(R.id.rcv_report_photo);
            LineItemPictureBeforeSendAdapter adapterAttach = new LineItemPictureBeforeSendAdapter(this, fileList);
            LinearLayoutManager layoutManagerAttach = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
            rcv_report_photo.setAdapter(adapterAttach);
            rcv_report_photo.setLayoutManager(layoutManagerAttach);
        } else {
            // chưa có hình ảnh nào được chọn
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}