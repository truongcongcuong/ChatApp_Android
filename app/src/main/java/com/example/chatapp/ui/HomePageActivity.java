package com.example.chatapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.entity.Language;
import com.example.chatapp.ui.signin.SigninActivity;
import com.example.chatapp.ui.signup.SignUpActivity;
import com.example.chatapp.utils.LanguageUtils;

public class HomePageActivity extends AppCompatActivity {
    Button btn_home_page_language_vietnamese,btn_home_page_language_english,btn_home_page_sign_in,btn_home_page_sign_up;
    boolean isEnLanguage;
    TextView txt_home_page_language;

    private LanguageUtils languageUtils;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        languageUtils = new LanguageUtils(this);
        initView();

        Language languageVI = new Language(Constant.RequestCode.CHANGE_LANGUAGE,
                getString(R.string.language_vietnamese),
                getString(R.string.language_vietnamese_code));

        Language languageEN = new Language(Constant.Value.DEFAULT_LANGUAGE_ID,
                    getString(R.string.language_english),
                    getString(R.string.language_english_code));


        /// config change language
        if(languageUtils.getCurrentLanguage().getCode().equals("en")){
            isEnLanguage = true;
        } else isEnLanguage = false;
        setDisableButtonLanguage(isEnLanguage);
        ///


        btn_home_page_sign_in.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, SigninActivity.class);
            startActivity(intent);
        });

        btn_home_page_sign_up.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        btn_home_page_language_vietnamese.setOnClickListener(v -> {
            checkCurrentLanguage(languageVI, false);
        });

        btn_home_page_language_english.setOnClickListener(v->checkCurrentLanguage(languageEN,true));

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkCurrentLanguage(Language language , boolean isEnLanguage) {
        this.isEnLanguage = isEnLanguage;
        Log.e("check-language",languageUtils.getCurrentLanguage().toString());
        if (!language.getCode().equals(languageUtils.getCurrentLanguage().getCode())){
//            languageUtils.changeLanguage(language);
            onChangeLanguageSuccessfully(language);
        }
    }

    private void showDialogFeautureUpdate() {
        AlertDialog alertDialog = new AlertDialog.Builder(HomePageActivity.this)
                .setTitle(R.string.notification_feature_error)
                .setMessage(R.string.feature_error_content)
                .setNegativeButton(R.string.accept, null)
                .create();
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void onChangeLanguageSuccessfully(final Language language) {
        if(languageUtils.changeLanguage(language))
//            refreshLayout();
//            initView();
            refreshText();
    }
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void refreshLayout(){
//        Intent intent = getIntent();
//        overridePendingTransition(0, 0);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        finish();
//        overridePendingTransition(0, 0);
//        startActivity(intent);
//        setDisableButtonLanguage(isEnLanguage);
//    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setDisableButtonLanguage(boolean isEnLanguage){
        if(isEnLanguage){
            btn_home_page_language_vietnamese.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
            btn_home_page_language_english.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.susscess)));
        }
        else {
            btn_home_page_language_vietnamese.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.susscess)));
            btn_home_page_language_english.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void refreshText(){
        btn_home_page_language_vietnamese.setText(getResources().getString(R.string.vietnamese_language));
        btn_home_page_sign_in.setText(getResources().getString(R.string.sign_in_button));
        btn_home_page_sign_up.setText(getResources().getString(R.string.sign_up));
        btn_home_page_language_english.setText(getResources().getString(R.string.english_language));
        txt_home_page_language.setText(getResources().getString(R.string.language));
        setDisableButtonLanguage(isEnLanguage);
    }

    public void initView(){
        btn_home_page_language_vietnamese = findViewById(R.id.btn_home_page_language_vietnamese);
        btn_home_page_sign_in = findViewById(R.id.btn_home_page_sign_in);
        btn_home_page_sign_up = findViewById(R.id.btn_home_page_sign_up);
        btn_home_page_language_english = findViewById(R.id.btn_home_page_language_english);
        txt_home_page_language = findViewById(R.id.txt_home_page_language);
    }
}