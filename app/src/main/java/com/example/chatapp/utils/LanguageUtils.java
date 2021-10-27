package com.example.chatapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import com.example.chatapp.R;
import com.example.chatapp.cons.Constant;
import com.example.chatapp.entity.Language;
import com.google.gson.Gson;

import java.util.Locale;

public class LanguageUtils {
    private static Language sCurrentLanguage = null;
    private static Context context = null;
    private static Gson gson = new Gson();
    public LanguageUtils(Context context) {
        this.context = context;

    }

    public static Language getCurrentLanguage(){
        if (sCurrentLanguage==null)
            sCurrentLanguage=initCurrentLanguage();
        return sCurrentLanguage;
    }

    private static Language initCurrentLanguage() {
        SharedPreferences sharedPreferencesLanguage = context.getSharedPreferences("multi-language",Context.MODE_PRIVATE);
        Language currentLanguage = gson.fromJson(sharedPreferencesLanguage.getString("language",null),Language.class);
        if (currentLanguage == null){
            Log.e("current-language null","currentLanguage.toString()");
            currentLanguage = new Language(Constant.Value.DEFAULT_LANGUAGE_ID,
                    context.getString(R.string.language_english),
                    context.getString(R.string.language_english_code));
            SharedPreferences.Editor editorLanguage = sharedPreferencesLanguage.edit();
            Log.e("current-language def",currentLanguage.toString());
            editorLanguage.putString("language",gson.toJson(currentLanguage));
        }
        return currentLanguage;

    }

    public static void loadLocale(){
        changeLanguage(initCurrentLanguage());
    }


    public static boolean changeLanguage(Language language) {
        SharedPreferences sharedPreferencesLanguage = context.getSharedPreferences("multi-language",Context.MODE_PRIVATE);
        SharedPreferences.Editor editorLanguage = sharedPreferencesLanguage.edit();
        editorLanguage.putString("language",gson.toJson(language)).apply();
        Log.e("change-language",language.toString());
        sCurrentLanguage = language;
        Locale locale = new Locale(language.getCode());
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
        return true;
    }



}
