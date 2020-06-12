package com.Osunji;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {
    static ArrayList actList = new ArrayList<Activity>();

    public static ArrayList getActList() {
        return actList;
    }
    public void actFinish() {
        for(Object act : actList)
            ((Activity)act).finish();
        finish();
    }
}