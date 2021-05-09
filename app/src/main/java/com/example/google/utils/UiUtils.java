package com.example.google.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.google.R;


public class UiUtils {
        //log for text to speech
        public static final String logTTS = "Text To Speech";
        //to see speech recognitions
        public static final String logSR = "SR";
        // to see the availability of text to speech
        public static final String logKeeper = "Keeper";
        public static void setCustomActionBar(ActionBar supportActionBar, Context context) {
                supportActionBar.setDisplayShowHomeEnabled(true);
                supportActionBar.setDisplayShowTitleEnabled(false);
                LayoutInflater mInflater = LayoutInflater.from(context);
                View mCustomView = mInflater.inflate(R.layout.customtoolbar, null);
                supportActionBar.setCustomView(mCustomView);
                supportActionBar.setDisplayShowCustomEnabled(true);
        }

        public static void setCustomActionBar(Fragment fragment, Context context) {
                setCustomActionBar( ((AppCompatActivity) fragment.requireActivity()).getSupportActionBar(), context);
        }


}
