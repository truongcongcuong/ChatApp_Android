package com.example.chatapp.ui.main.frag;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MenuButtonAdapterVertical;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.cons.SendData;
import com.example.chatapp.dto.MenuItem;
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

public class InfoFragment extends Fragment {
    private TextView txt_info_name;
    private ImageView image_info_image;
    private List<MenuItem> menuItems;
    private NestedScrollView nestedScrollView;
    private Gson gson;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

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

        menuItems = new ArrayList<>();
        menuItems.add(MenuItem.builder()
                .key("viewProfile")
                .imageResource(R.drawable.ic_baseline_profile_circle_24)
                .name(getString(R.string.view_profile))
                .build());

        menuItems.add(MenuItem.builder()
                .key("changePassword")
                .imageResource(R.drawable.ic_baseline_key_24)
                .name(getString(R.string.change_password))
                .build());

        menuItems.add(MenuItem.builder()
                .key("changeLanguage")
                .imageResource(R.drawable.language)
                .name(getString(R.string.language))
                .build());

        menuItems.add(MenuItem.builder()
                .key("signout")
                .imageResource(R.drawable.ic_baseline_leave_24)
                .name(getString(R.string.logout))
                .build());

        menuItems.add(MenuItem.builder()
                .key("infoApp")
                .imageResource(R.drawable.ic_round_info_24_dark)
                .name(getString(R.string.app_info))
                .build());

        for (int i = 0; i < 10; i++) {
            menuItems.add(MenuItem.builder()
                    .key("---")
                    .name("---------------------")
                    .build());
        }


        MenuButtonAdapterVertical menuAdapter = new MenuButtonAdapterVertical(getActivity(), R.layout.line_item_menu_button_vertical, menuItems);
        lv_info_fragment_menu.setAdapter(menuAdapter);
        lv_info_fragment_menu.setOnItemClickListener((parent, view1, position, itemId) -> {
            MenuItem item = menuItems.get(position);
            if (item.getKey().equals("viewProfile")) {
                viewInformation();
            } else if (item.getKey().equals("changePassword")) {
                changePassword();
            } else if (item.getKey().equals("signout")) {
                signout();
            } else if (item.getKey().equals("infoApp")) {
                Toast.makeText(getActivity(), getString(R.string.app_info), Toast.LENGTH_SHORT).show();
            } else if(item.getKey().equals("changeLanguage")){
                showDialogChangeLanguage();
            }
        });

        getUserInfo();
        setListViewHeightBasedOnChildren(lv_info_fragment_menu);
        nestedScrollView.post(() -> nestedScrollView.scrollTo(0, 0));
        return view;
    }

    private void showDialogChangeLanguage() {
        TextView txt_change_language_english,txt_change_language_vietnamese;
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_change_language);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        Language currentLanguage = gson.fromJson(sharedPreferencesLanguage.getString("language",null),Language.class);
        if (currentLanguage.getCode().equals("en"))
            txt_change_language_english.setCompoundDrawablesWithIntrinsicBounds(R.drawable.united_kingdom,0,R.drawable.ic_baseline_check_circle_outline_24,0);
        else
            txt_change_language_vietnamese.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vietnam,0,R.drawable.ic_baseline_check_circle_outline_24,0);

        txt_change_language_english.setOnClickListener(v->{
            languageUtils.changeLanguage(languageEN);
            reloadActivity();
            dialog.dismiss();
        });

        txt_change_language_vietnamese.setOnClickListener(v->{
            languageUtils.changeLanguage(languageVI);
            reloadActivity();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void reloadActivity() {

        SendData sendData = (SendData) getContext();
        sendData.SendingData("true");
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
        txt_info_name.setText(user.getDisplayName());
        Glide.with(this).load(user.getImageUrl())
                .placeholder(R.drawable.image_placeholer)
                .centerCrop().circleCrop().into(image_info_image);
    }

    private void callSignout() {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.API_AUTH + "signout",
                response -> {
                    try {
                        String res = URLDecoder.decode(URLEncoder.encode(response, "iso8859-1"), "UTF-8");
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

}