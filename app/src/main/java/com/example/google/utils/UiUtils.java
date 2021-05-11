package com.example.google.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.google.R;


public class UiUtils {
        public static final String[] Commands = {"check my mail","what can i do","can we date","how to use google assistant","hey ", "for example : search assistant","explore","how to use google assistant","hi","hello","thanks","welcome","clear","date","time","dial","send SMS", "send sms", "joke", "tell me a joke"
        ,"ask me fun questions", "open Whatsapp" , "open Facebook" , "open Gmail", "open Youtube" , "open  GoogleMaps" , "open Google",
        "turn on Bluetooth" , "For example : call mum or call papa ",  "dial" , "turn off Bluetooth" , "turn on Flash" , "turn off Flash","capture photo" , "any thoughts",
                "play ringtone","stop ringtone","are you married","haha","read me","read my last sms","share file","share a text message that your message",
        "get bluetooth devices","copy to clipboard","read last clipboard","open google lens","explore","what is your name" , "play some music",
        "stop music"};
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
                @SuppressLint("InflateParams") View mCustomView = mInflater.inflate(R.layout.customtoolbar, null);
                supportActionBar.setCustomView(mCustomView);
                supportActionBar.setDisplayShowCustomEnabled(true);
        }

        public static void setCustomActionBar(Fragment fragment, Context context) {
                setCustomActionBar( ((AppCompatActivity) fragment.requireActivity()).getSupportActionBar(), context);
        }


}
