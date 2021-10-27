package com.example.chatapp.utils;

import java.text.SimpleDateFormat;
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

    public static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));

    public static final List<String> timesString = Arrays.asList("năm", "tháng", "ngày", "giờ", "phút", "giây");

    public static String getTime(String date1) {

//        Date d1 = new Date(new Date().getTime() + 7 * 3600 * 1000);
        Date d1 = new Date();
        Date d2 = new Date();

        try {
            d2 = timeStampFormat.parse(date1);
        } catch (Exception e) {
        }
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
                return "hôm qua";
            if ((i == 2 && temp > 7) || calendar1.get(Calendar.DAY_OF_MONTH) - calendar2.get(Calendar.DAY_OF_MONTH) > 7)
                return monthFormat.format(d2);
            if (temp > 0) {
                res.append(temp)
                        .append(" ")
                        .append(TimeAgo.timesString.get(i));
                break;
            }
        }
        if ("".equals(res.toString()))
            return "bây giờ";
        else
            return res.toString();
    }

    public static String getTimeStamp(String date1) {

        Date d1 = new Date();
        Date d2 = new Date();

        try {
            d2 = timeStampFormat.parse(date1);
        } catch (Exception e) {
        }

        return onlyTimeFormat.format(d2);
    }
}