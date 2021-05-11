package com.example.google.assistant

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraManager
import android.location.Location
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.google.R
import com.example.google.data.AssitantDatabase
import com.example.google.databinding.ActivityAssistantBinding
import com.example.google.functions.AssistantFunctions.Companion.Animation_TIME
import com.example.google.functions.AssistantFunctions.Companion.CAPTUREPHOTO
import com.example.google.functions.AssistantFunctions.Companion.Dips
import com.example.google.functions.AssistantFunctions.Companion.READCONTACTS
import com.example.google.functions.AssistantFunctions.Companion.READSMS
import com.example.google.functions.AssistantFunctions.Companion.REQUEST_CALL
import com.example.google.functions.AssistantFunctions.Companion.REQUEST_CODE_SELECT_DOC
import com.example.google.functions.AssistantFunctions.Companion.REQUEST_ENABLE_BT
import com.example.google.functions.AssistantFunctions.Companion.SHAREAFILE
import com.example.google.functions.AssistantFunctions.Companion.SHAREATEXTFILE
import com.example.google.functions.AssistantFunctions.Companion.callContact
import com.example.google.functions.AssistantFunctions.Companion.capturePhoto
import com.example.google.functions.AssistantFunctions.Companion.clipBoardCopy
import com.example.google.functions.AssistantFunctions.Companion.clipBoardSpeak
import com.example.google.functions.AssistantFunctions.Companion.getAllPairedDevices
import com.example.google.functions.AssistantFunctions.Companion.getDate
import com.example.google.functions.AssistantFunctions.Companion.getTextFromBitmap
import com.example.google.functions.AssistantFunctions.Companion.getTime
import com.example.google.functions.AssistantFunctions.Companion.joke
import com.example.google.functions.AssistantFunctions.Companion.makeAPhoneCall
import com.example.google.functions.AssistantFunctions.Companion.motivationalThoughts
import com.example.google.functions.AssistantFunctions.Companion.openFacebook
import com.example.google.functions.AssistantFunctions.Companion.openGmail
import com.example.google.functions.AssistantFunctions.Companion.openGoogle
import com.example.google.functions.AssistantFunctions.Companion.openMaps
import com.example.google.functions.AssistantFunctions.Companion.openMessages
import com.example.google.functions.AssistantFunctions.Companion.openWhatsAPP
import com.example.google.functions.AssistantFunctions.Companion.openYoutube
import com.example.google.functions.AssistantFunctions.Companion.playRingtone
import com.example.google.functions.AssistantFunctions.Companion.question
import com.example.google.functions.AssistantFunctions.Companion.readMe
import com.example.google.functions.AssistantFunctions.Companion.readSMS
import com.example.google.functions.AssistantFunctions.Companion.search
import com.example.google.functions.AssistantFunctions.Companion.sendSMS
import com.example.google.functions.AssistantFunctions.Companion.shareAFile
import com.example.google.functions.AssistantFunctions.Companion.shareATextMessage
import com.example.google.functions.AssistantFunctions.Companion.speak
import com.example.google.functions.AssistantFunctions.Companion.stopRingtone
import com.example.google.functions.AssistantFunctions.Companion.turnOffBluetooth
import com.example.google.functions.AssistantFunctions.Companion.turnOffFlash
import com.example.google.functions.AssistantFunctions.Companion.turnOnBluetooth
import com.example.google.functions.AssistantFunctions.Companion.turnOnFlash
import com.example.google.functions.GoogleLensActivity

import com.example.google.utils.UiUtils.*
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import java.io.File
import java.io.FileNotFoundException
import java.util.*


class AssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssistantBinding
    private lateinit var assistantViewModel: AssistantViewModel
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private lateinit var keeper: String
    private lateinit var cameraManager: CameraManager
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var cameraID: String
    private lateinit var ringnote: Ringtone
    private lateinit var imageuri: Uri
    private lateinit var helper: OpenWeatherMapHelper

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.non_movable, R.anim.non_movable)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_assistant)
        setCustomActionBar(supportActionBar, this)
        if (Settings.System.canWrite(this)) {
            ringnote = RingtoneManager.getRingtone(applicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
        } else {
            //Migrate to Setting write permission screen.
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + this.packageName)
            startActivity(intent)
        }
        val application = requireNotNull(this).application
        val dataSource = AssitantDatabase.getInstance(this).assiatntDao
        val viewModelFactory = AssiatntViewModelFactory(dataSource, application)
        assistantViewModel = ViewModelProvider(this, viewModelFactory).get(AssistantViewModel::class.java)
        val adapter = AssisantAdapter()
        binding.recylerView.adapter = adapter
        assistantViewModel.messages.observe(this, {
            it?.let {
                adapter.data = it
            }
        })

        binding.lifecycleOwner = this
        //animations
        if (savedInstanceState == null) {
            binding.assistantConstraintLayout.visibility = View.INVISIBLE
            //Q&A so like a tree for ques and ans like a conversation with help if tree
            //chat like applications

            val viewTreeObserver: ViewTreeObserver = binding.assistantConstraintLayout.viewTreeObserver
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        circularRevealActivity()
                        binding.assistantConstraintLayout.viewTreeObserver
                                .removeOnGlobalLayoutListener(this)

                    }
                })


            }
            cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            try {
                cameraID = cameraManager.cameraIdList[0]
                //0 for back camera
                // 1 for front camera
            } catch (e: java.lang.Exception) {
                e.printStackTrace()

            }
            clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            helper = OpenWeatherMapHelper(R.string.OPEN_WEATHER_MAP_API_KEY.toString())
            textToSpeech = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result: Int = textToSpeech.setLanguage(Locale.ENGLISH)
                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                        Log.i(logTTS, "Language Not Supported")

                    } else {
                        Log.i(logTTS, "Language Supported")
                    }
                } else {
                    Log.i(logTTS, "Initialization of TTS Failed")
                }

            }
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    //leave empty no use
                }

                override fun onBeginningOfSpeech() {
                    Log.d(logSR, "started")
                }

                override fun onRmsChanged(rmsdB: Float) {

                }

                override fun onBufferReceived(buffer: ByteArray?) {

                }

                override fun onEndOfSpeech() {
                    Log.d(logSR, "ended")
                }

                override fun onError(error: Int) {
                    Log.d(logSR, error.toString())
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResults(bundles: Bundle?) {
                    val data = bundles!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (data != null) {
                        keeper = data[0]
                        Log.d(logKeeper, keeper)
                        when {
                            keeper.contains("thanks") -> speak("Its my job , let me know if there is something else", textToSpeech, assistantViewModel, keeper)
                            keeper.contains("search") -> search(this@AssistantActivity,keeper)
                            keeper.contains("welcome") -> speak("Its my pleasure to help you out", textToSpeech, assistantViewModel, keeper)
                            keeper.contains("go out with me") ||  keeper.contains("club") ||  keeper.contains("coffee") ||  keeper.contains("dance") ||  keeper.contains("love")  ||  keeper.contains("ok") -> speak("Yes , Ofcourse", textToSpeech, assistantViewModel, keeper)
                            keeper.contains("clear") ||  keeper.contains("delete")-> assistantViewModel.onClear()
                            keeper.contains("date") -> getDate(textToSpeech, assistantViewModel, keeper)
                            keeper.contains("time") -> getTime(textToSpeech, assistantViewModel, keeper)
                            keeper.contains("dial") -> makeAPhoneCall(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("send sms") || keeper.contains("send SMS") -> sendSMS(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("read my last sms") || keeper.contains("read my last SMS") || keeper.contains("read my SMS") -> readSMS(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("open Gmail") || keeper.contains("Gmail") ||keeper.contains("gmail") ||keeper.contains("mail") -> openGmail(this@AssistantActivity)
                            keeper.contains("open Maps") || keeper.contains("open maps")  || keeper.contains("maps")-> openMaps(this@AssistantActivity)
                            keeper.contains("open Google") || keeper.contains("open Google") || keeper.contains("open Chrome") -> openGoogle(this@AssistantActivity)
                            keeper.contains("open Whatsapp") || keeper.contains("open WhatsApp") -> openWhatsAPP(this@AssistantActivity)
                            keeper.contains("open facebook") || keeper.contains("open Facebook") || keeper.contains("open Face") || keeper.contains("open Facebook") -> openFacebook(this@AssistantActivity)
                            keeper.contains("open messages") -> openMessages(this@AssistantActivity, applicationContext)
                            keeper.contains("how to use google assistant") || keeper.contains("google assistant") || keeper.contains("how to use") || keeper.contains("can I do") || keeper.contains("what can I do") || keeper.contains("Google assistant") || keeper.contains("can")-> speak("Try some Commands : open whatsapp , open facebook , tell me a joke , hi , hello , explore , google lens", textToSpeech, assistantViewModel, keeper)
                            keeper.contains("open youtube") || keeper.contains("open YouTube") -> openYoutube(this@AssistantActivity)
                            keeper.contains("share file") -> shareAFile(this@AssistantActivity, applicationContext)
                            keeper.contains("share a text message") -> shareATextMessage(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("call") -> callContact(this@AssistantActivity, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("turn on bluetooth") || keeper.contains("turn on Bluetooth") -> turnOnBluetooth(this@AssistantActivity, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("turn off bluetooth") || keeper.contains("turn off Bluetooth") -> turnOffBluetooth(textToSpeech, assistantViewModel, keeper)
                            keeper.contains("get bluetooth devices") -> getAllPairedDevices(textToSpeech, assistantViewModel, keeper)
                            keeper.contains("turn on flash") -> turnOnFlash(cameraManager, cameraID, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("turn off flash") -> turnOffFlash(cameraManager, cameraID, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("copy to clipboard") ||   keeper.contains("copy") -> clipBoardCopy(clipboardManager, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("read last clipboard")|| keeper.contains("read my clipboard")|| keeper.contains("read clipboard")-> clipBoardSpeak(clipboardManager, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("capture photo") -> capturePhoto(this@AssistantActivity, applicationContext, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("play ringtone") ||  keeper.contains("play something") ||  keeper.contains("play")||  keeper.contains("song")-> playRingtone(ringnote, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("stop ringtone") || keeper.contains("stop playing") || keeper.contains("stop music") || keeper.contains("stop") || keeper.contains("sto") -> stopRingtone(ringnote, textToSpeech, assistantViewModel, keeper)
                            keeper.contains("read me") -> readMe(this@AssistantActivity)
                            keeper.contains("weather") ||
                                    keeper.contains("explore")||
                                    keeper.contains("Explore")||
                                    keeper.contains("Commands")||
                                    keeper.contains("commands")-> startActivity(Intent(this@AssistantActivity, ExploreActivity::class.java))
                            keeper.contains("lens")||keeper.contains("Lens")||keeper.contains("len")-> startActivity(Intent(this@AssistantActivity, GoogleLensActivity::class.java))
                            keeper.contains("motivate") || keeper.contains("any thoughts") || keeper.contains("motivational thoughts") || keeper.contains("motivational") -> motivationalThoughts(textToSpeech, assistantViewModel, keeper)
                            keeper.contains("joke") -> joke(textToSpeech, assistantViewModel, keeper)
                            keeper.contains("question") -> question(textToSpeech, assistantViewModel, keeper)
                            keeper.contains("haha") || keeper.contains("hehe") -> speak("I know , I am funny", textToSpeech, assistantViewModel, keeper)
                            keeper.contains("are you married") ||   keeper.contains("married") ||   keeper.contains("marry") -> speak("Yes to my work !", textToSpeech, assistantViewModel, keeper)
                            keeper.contains("boat") || keeper.contains("real magic")
                                    || keeper.contains("magic") || keeper.contains("useless talent")
                                    || keeper.contains("smelling place") || keeper.contains("smelling ") ->
                                speak("You are funny haha", textToSpeech, assistantViewModel, keeper)
                            keeper.contains("what is your name") || keeper.contains("your name")
                                    || keeper.contains("what do you call your self") ->
                                speak("I am Google Assistant at  your service", textToSpeech, assistantViewModel, keeper)
                            keeper.contains("hello") || keeper.contains("hi") || keeper.contains("hey") || keeper.contains("hay")
                            -> speak("Hello , how can I help you ?", textToSpeech, assistantViewModel, keeper)
                            else -> speak("Please try another comment like  what is your name , call someone , read my sms , open google lens , explore", textToSpeech, assistantViewModel, keeper)

                        }

                    }

                }

                override fun onPartialResults(partialResults: Bundle?) {

                }

                override fun onEvent(eventType: Int, params: Bundle?) {

                }

            })

            binding.assistantAction.setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_UP -> {
                        speechRecognizer.stopListening()
                    }

                    MotionEvent.ACTION_DOWN -> {
                        textToSpeech.stop()
                        speechRecognizer.startListening(recognizerIntent)
                    }
                }
                false

            }
            checkSpeechRecognizerAvailable()
        }

    }


    private fun checkSpeechRecognizerAvailable() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.d(logSR, "yes")
        } else {
            Log.d(logSR, "false")
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                makeAPhoneCall(this, applicationContext, textToSpeech, assistantViewModel, keeper)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == READSMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                readSMS(this, applicationContext, textToSpeech, assistantViewModel, keeper)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == SHAREAFILE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                shareAFile(this, applicationContext)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == SHAREATEXTFILE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                shareATextMessage(this, applicationContext, textToSpeech, assistantViewModel, keeper)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == READCONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                callContact(this, textToSpeech, assistantViewModel, keeper)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == CAPTUREPHOTO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                capturePhoto(this, applicationContext, textToSpeech, assistantViewModel, keeper)
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_DOC && resultCode == RESULT_OK) {
            val filePath = data!!.data!!.path
            Log.d("check", "path: $filePath")
            val file = File(filePath)
            val intentShare = Intent(Intent.ACTION_SEND)
            intentShare.type = "application/pdf"
            intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$file"))
            startActivity(Intent.createChooser(intentShare, "Share the file"))
        }

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                speak("Bluetooth is On", textToSpeech, assistantViewModel, keeper)
            } else {
                speak("could not able turn on Bluetooth ", textToSpeech, assistantViewModel, keeper)
            }

        }
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this, data)
            imageuri = imageUri
            startCrop(imageUri)

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageuri = result.uri
                try {
                    val inputStream = contentResolver.openInputStream(imageuri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    getTextFromBitmap(this, bitmap, textToSpeech, assistantViewModel, keeper)

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
            Toast.makeText(this, "Image Captured Successfully", Toast.LENGTH_SHORT).show()

        }
    }

    private fun startCrop(imageUri: Uri) {

        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true)
                .start(this)

    }

    private fun circularRevealActivity() {
        val cx: Int = binding.assistantConstraintLayout.right - getDips(Dips)
        val cy: Int = binding.assistantConstraintLayout.bottom - getDips(Dips)
        val finalRadius: Int = Math.max(
                binding.assistantConstraintLayout.width,
                binding.assistantConstraintLayout.height
        )
        val circularReveal = ViewAnimationUtils.createCircularReveal(
                binding.assistantConstraintLayout,
                cx,
                cy,
                0f,
                finalRadius.toFloat()
        )
        circularReveal.duration = Animation_TIME.toLong()
        binding.assistantConstraintLayout.visibility = View.VISIBLE
        circularReveal.start()
    }

    private fun getDips(i: Int): Int {
        val resources: Resources = resources
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                i.toFloat(),
                resources.displayMetrics
        ).toInt()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cx: Int = binding.assistantConstraintLayout.width - getDips(Dips)
            val cy = binding.assistantConstraintLayout.height - getDips(Dips)
            val finalRadius: Int = Math.max(
                    binding.assistantConstraintLayout.width,
                    binding.assistantConstraintLayout.height
            )
            val circularReveal = ViewAnimationUtils.createCircularReveal(binding.assistantConstraintLayout, cx, cy, finalRadius.toFloat(), 0f)
            circularReveal.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {


                }

                override fun onAnimationEnd(animation: Animator?) {
                    binding.assistantConstraintLayout.visibility = View.GONE
                    finish()

                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationRepeat(animation: Animator?) {

                }
            })
            circularReveal.duration = Animation_TIME.toLong()
            circularReveal.start()


        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
        speechRecognizer.cancel()
        speechRecognizer.destroy()
        Log.i(logSR, "destroy")
    }


}
