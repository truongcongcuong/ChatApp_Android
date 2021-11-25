package com.example.chatapp.ui.main.frag;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuButtonAdapterVertical;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.WebSocketClient;
import com.example.chatapp.dto.MyMenuItem;
import com.example.chatapp.dto.UserDetailDTO;
import com.example.chatapp.dto.UserSummaryDTO;
import com.example.chatapp.entity.Language;
import com.example.chatapp.ui.ChangePasswordActivity;
import com.example.chatapp.ui.HomePageActivity;
import com.example.chatapp.ui.ViewInformationActivity;
import com.example.chatapp.utils.LanguageUtils;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.json.Json;

public class InfoFragment extends Fragment {
    private TextView txt_info_name;
    private ImageView image_info_image;
    private List<MyMenuItem> myMenuItems;
    private NestedScrollView nestedScrollView;
    private Gson gson;
    private MenuButtonAdapterVertical menuAdapter;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private final BroadcastReceiver updateInfoSuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                UserDetailDTO userDetailDTO = (UserDetailDTO) bundle.getSerializable("dto");

                SharedPreferences sharedPreferencesUser = getActivity().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                UserSummaryDTO user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
                user.setUsername(userDetailDTO.getUsername());
                user.setDisplayName(userDetailDTO.getDisplayName());
                user.setImageUrl(userDetailDTO.getImageUrl());
                displayInformation(user);

                SharedPreferences.Editor editor = sharedPreferencesUser.edit();
                editor.putString("user-info", Json.encode(user)).apply();
            }
        }
    };

    public InfoFragment() {
    }

    public static InfoFragment newInstance(String param1, String param2) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(updateInfoSuccess, new IntentFilter("user/update/success"));

        /*
        enable menu trên action bar
         */
        setHasOptionsMenu(false);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_infor, container, false);

        txt_info_name = view.findViewById(R.id.name_of_user_info_fragment);
        image_info_image = view.findViewById(R.id.image_user_info_fragment);
        ListView lv_info_fragment_menu = view.findViewById(R.id.lv_info_fragment_menu);
        nestedScrollView = view.findViewById(R.id.nested_scroll_info_fragment);

        gson = new Gson();
//        SharedPreferences sharedPreferencesToken = getActivity().getSharedPreferences("token", Context.MODE_PRIVATE);
//        String token = sharedPreferencesToken.getString("access-token", null);

        initListMenuItems();

        menuAdapter = new MenuButtonAdapterVertical(getActivity(), R.layout.line_item_menu_button_vertical, myMenuItems);
        lv_info_fragment_menu.setAdapter(menuAdapter);
        lv_info_fragment_menu.setOnItemClickListener((parent, view1, position, itemId) -> {
            MyMenuItem item = myMenuItems.get(position);
            if (item.getKey().equals("viewProfile")) {
                viewInformation();
            } else if (item.getKey().equals("changePassword")) {
                changePassword();
            } else if (item.getKey().equals("signout")) {
                signout();
            } else if (item.getKey().equals("infoApp")) {
                Toast.makeText(getActivity(), getString(R.string.app_info), Toast.LENGTH_SHORT).show();
            } else if (item.getKey().equals("changeLanguage")) {
                showDialogChangeLanguage();
            }
        });

        getUserInfo();
        setListViewHeightBasedOnChildren(lv_info_fragment_menu);
        nestedScrollView.post(() -> nestedScrollView.scrollTo(0, 0));
        return view;
    }

    private void initListMenuItems() {
        try {
            myMenuItems.clear();
        } catch (Exception e) {
            myMenuItems = new ArrayList<>();
        }
        myMenuItems.add(MyMenuItem.builder()
                .key("viewProfile")
                .imageResource(R.drawable.ic_baseline_profile_circle_24)
                .name(getString(R.string.view_profile))
                .build());

        myMenuItems.add(MyMenuItem.builder()
                .key("changePassword")
                .imageResource(R.drawable.ic_baseline_key_24)
                .name(getString(R.string.change_password))
                .build());

        myMenuItems.add(MyMenuItem.builder()
                .key("changeLanguage")
                .imageResource(R.drawable.language)
                .name(getString(R.string.language))
                .build());

        myMenuItems.add(MyMenuItem.builder()
                .key("signout")
                .imageResource(R.drawable.ic_baseline_leave_24)
                .name(getString(R.string.logout))
                .build());

        myMenuItems.add(MyMenuItem.builder()
                .key("infoApp")
                .imageResource(R.drawable.ic_round_info_24_dark)
                .name(getString(R.string.app_info))
                .build());

        for (int i = 0; i < 10; i++) {
            myMenuItems.add(MyMenuItem.builder()
                    .key("---")
                    .name("---------------------")
                    .build());
        }
    }

    private void showDialogChangeLanguage() {
        TextView txt_change_language_english, txt_change_language_vietnamese;
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_change_language);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        txt_change_language_english = dialog.findViewById(R.id.txt_change_language_english);
        txt_change_language_vietnamese = dialog.findViewById(R.id.txt_change_language_vietnamese);
        LanguageUtils languageUtils = new LanguageUtils(getContext());
        Language languageVI = new Language(Constant.RequestCode.CHANGE_LANGUAGE,
                getString(R.string.language_vietnamese),
                getString(R.string.language_vietnamese_code));

        Language languageEN = new Language(Constant.Value.DEFAULT_LANGUAGE_ID,
                getString(R.string.language_english),
                getString(R.string.language_english_code));

        SharedPreferences sharedPreferencesLanguage = getContext().getSharedPreferences("multi-language", Context.MODE_PRIVATE);
        Language currentLanguage = gson.fromJson(sharedPreferencesLanguage.getString("language", null), Language.class);
        if (currentLanguage.getCode().equals("en"))
            txt_change_language_english.setCompoundDrawablesWithIntrinsicBounds(R.drawable.united_kingdom, 0, R.drawable.ic_baseline_check_circle_outline_24, 0);
        else
            txt_change_language_vietnamese.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vietnam, 0, R.drawable.ic_baseline_check_circle_outline_24, 0);

        txt_change_language_english.setOnClickListener(v -> {
            languageUtils.changeLanguage(languageEN);
            reloadActivity();
            dialog.dismiss();
        });

        txt_change_language_vietnamese.setOnClickListener(v -> {
            languageUtils.changeLanguage(languageVI);
            reloadActivity();
            dialog.dismiss();
        });

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.dimAmount = .5f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.gravity = Gravity.BOTTOM;
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        layoutParams.height = (int) (displayHeight * 0.3f);
        dialog.getWindow().setAttributes(layoutParams);

        dialog.show();
    }

    private void reloadActivity() {
//        SendingData sendData = (SendingData) getContext();
//        sendData.sendString("true");
        initListMenuItems();
        menuAdapter.setItems(myMenuItems);
        Intent intent = new Intent("language/change");
        Bundle bundle = new Bundle();
        bundle.putBoolean("change", true);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    // set dynamic height for list view
    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void changePassword() {
        Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
        startActivity(intent);
    }

    private void viewInformation() {
        Intent intent = new Intent(getContext(), ViewInformationActivity.class);
        startActivity(intent);
    }

    private void signout() {
        callSignout();
        SharedPreferences sharedPreferencesIsLogin = getActivity().getApplicationContext().getSharedPreferences("is-login", getActivity().getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editorIsLogin = sharedPreferencesIsLogin.edit();
        editorIsLogin.putBoolean("status-login", false).apply();
        Intent i = new Intent(getActivity().getApplicationContext(), HomePageActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("EXIT", true);
        startActivity(i);
        getActivity().finish();
    }

    private void getUserInfo() {
        SharedPreferences sharedPreferencesUser = getActivity().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        UserSummaryDTO user = gson.fromJson(sharedPreferencesUser.getString("user-info", null), UserSummaryDTO.class);
        displayInformation(user);
    }

    private void displayInformation(UserSummaryDTO user) {
        txt_info_name.setText(user.getDisplayName());
        Glide.with(this).load(user.getImageUrl())
                .placeholder(R.drawable.img_avatar_placeholer)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop().circleCrop().into(image_info_image);
    }

    private void callSignout() {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_AUTH + "signout",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
                        WebSocketClient.logout();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError) {
                        try {
                            String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(getString(R.string.logout_failed_try_again))
                                    .setPositiveButton(getString(R.string.confirm_button), (dialog, id) -> dialog.cancel());
                            builder.setCancelable(false);
                            builder.create().show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                SharedPreferences sharedPreferencesToken = getActivity().getApplicationContext().getSharedPreferences("token", getActivity().getApplicationContext().MODE_PRIVATE);
                String rfToken = sharedPreferencesToken.getString("refresh-token", null);
                map.put("Cookie", rfToken);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(retryPolicy);
        queue.add(request);
    }

    /*
    chỉ khi nào fragment được hiển thị thì mới hiện menu
     */
    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(isVisible());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_info_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateInfoSuccess);
        super.onDestroy();
    }

}