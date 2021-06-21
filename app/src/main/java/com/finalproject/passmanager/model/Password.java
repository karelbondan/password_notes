package com.finalproject.passmanager.model;

import java.util.Calendar;
import java.util.Comparator;

public class Password {
    private String itemid;
    private String itemName;
    private String userName;
    private String password;
    private String URL;
    private String note;
    private String date;
    private String time;
    private static Calendar calendar;

    public Password(){

    }

    public Password(String itemid, String itemName, String userName, String password, String URL, String note, String date, String time) {
        this.itemid = itemid;
        this.itemName = itemName;
        this.userName = userName;
        this.password = password;
        this.URL = URL;
        this.note = note;
        this.date = date;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Passwords{" +
                "itemName='" + itemName + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", URL='" + URL + '\'' +
                ", note='" + note + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public String getItemid() {
        return itemid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public static String getItemTime() {
        calendar = Calendar.getInstance();
        String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(calendar.get(Calendar.MINUTE));

        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + hour;
        }
        if (calendar.get(Calendar.MINUTE) < 10){
            minute = "0" + minute;
        }
        return hour + ":" + minute;

    }

    public static String getItemDate() {
        calendar = Calendar.getInstance();
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        if (calendar.get(Calendar.DAY_OF_MONTH) < 10){
            day = "0" + day;
        }
        if (calendar.get(Calendar.MONTH) + 1 < 10){
            month = "0" + month;
        }
        if (calendar.get(Calendar.YEAR) < 10){
            year = "0" + year;
        }
        return day + "/" + month + "/" + year;
    }

    public static Comparator<Password> sortDescending = new Comparator<Password>() {
        @Override
        public int compare(Password pass1, Password pass2) {
            return pass1.getItemName().compareToIgnoreCase(pass2.getItemName());
        }
    };
}
