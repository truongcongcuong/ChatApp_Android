package com.example.chatapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.chatapp.R;
import com.example.chatapp.entity.Language;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeAgo {
    private static final SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat yearFormat = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("dd/MM");
    private static final SimpleDateFormat onlyTimeFormat = new SimpleDateFormat("HH:mm");
    private static final Gson GSON = new Gson();

    public static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));

    public static long getMiliSeconds(String date1) throws ParseException {
        Date d1 = new Date();
        Date d2 = new Date();

        d2 = timeStampFormat.parse(date1);
        return (d1.getTime() - d2.getTime());
    }

    public static String getTime(String date1, Context context) throws ParseException {
        SharedPreferences sharedPreferencesLanguage = context.getSharedPreferences("multi-language", Context.MODE_PRIVATE);
        Language currentLanguage = GSON.fromJson(sharedPreferencesLanguage.getString("language", null), Language.class);
        boolean english = currentLanguage.getCode().equals("en");

        List<String> timesString = new ArrayList<>();
        timesString.add(context.getString(R.string.year));
        timesString.add(context.getString(R.string.month));
        timesString.add(context.getString(R.string.day));
        timesString.add(context.getString(R.string.hour));
        timesString.add(context.getString(R.string.minute));
        timesString.add(context.getString(R.string.seconds));

//        Date d1 = new Date(new Date().getTime() + 7 * 3600 * 1000);
        Date d1 = new Date();
        Date d2 = new Date();

        d2 = timeStampFormat.parse(date1);
        long mseconds = (d1.getTime() - d2.getTime());

        StringBuilder res = new StringBuilder();
        for (int i = 0; i < TimeAgo.times.size(); i++) {
            Long current = TimeAgo.times.get(i);
            long temp = mseconds / current;
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(d1);
            Calendar calendar2 = Calendar.getInstance();
            calendar1.setTime(d2);
            if ((i == 0 && temp > 0) || calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR) > 0)
                return yearFormat.format(d2);
            if ((i == 1 && temp > 0) || calendar1.get(Calendar.MONTH) - calendar2.get(Calendar.MONTH) > 0)
                return monthFormat.format(d2);
            if (i == 2 && temp == 1)
                return context.getString(R.string.yesterday);
            if ((i == 2 && temp > 7) || calendar1.get(Calendar.DAY_OF_MONTH) - calendar2.get(Calendar.DAY_OF_MONTH) > 7)
                return monthFormat.format(d2);
            if (temp > 0) {
                res.append(temp)
                        .append(" ")
                        .append(timesString.get(i))
                        .append(english && temp > 1 ? "s" : "");
                break;
            }
        }
        if ("".equals(res.toString()))
            return context.getString(R.string.now);
        else
            return res.toString();
    }

    public static String getTimeStamp(String date1) throws ParseException {

        Date d1 = new Date();
        Date d2 = new Date();

        d2 = timeStampFormat.parse(date1);

        return onlyTimeFormat.format(d2);
    }
}