package com.example.google

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.google.assistant.AssistantActivity
import com.example.google.assistant.ExploreActivity
import com.example.google.functions.GoogleLensActivity
import com.example.google.utils.UiUtils.setCustomActionBar


class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var  hiGoogle : ImageView
    private lateinit var  googleLens : ImageView
    private lateinit var  explore : ImageView
    private val Record_Audio_Request_Code:Int=1
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            setCustomActionBar(supportActionBar, this)
            imageView = findViewById(R.id.action_button)
           googleLens= findViewById(R.id.action_google_lens)
            explore = findViewById(R.id.action_explore)
            hiGoogle= findViewById(R.id.hiGoogle)
            if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
                checkPermission()
            }
            imageView.setOnClickListener {
            startActivity(Intent(this, AssistantActivity::class.java))
        }
            hiGoogle.setOnClickListener {
                startActivity(Intent(this, AssistantActivity::class.java))
            }
            googleLens.setOnClickListener {
                startActivity(Intent(this, GoogleLensActivity::class.java))
            }
            explore.setOnClickListener {
                startActivity(Intent(this, ExploreActivity::class.java))
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==Record_Audio_Request_Code && grantResults.isNotEmpty())
        {
            //permigranted type of array that stores all the permission
            if (grantResults[0]== PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT)
            }
        }
    }
    private fun checkPermission(){
        ActivityCompat.requestPermissions(this,
        arrayOf(android.Manifest.permission.RECORD_AUDIO),
        Record_Audio_Request_Code )
    }
}