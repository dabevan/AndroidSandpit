package com.example.sandpit

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import java.io.File
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    private lateinit var mHandler: Handler
    private lateinit var mRunnable:Runnable
    private var n = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        n=0
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mHandler = Handler()


        button_start_recording.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions,0)
            } else {
                startRecording()
            }
        }


        button_stop_recording.setOnClickListener{
            mHandler.removeCallbacks(mRunnable)
            stopRecording()
        }

        button_monitor.setOnClickListener {
            mRunnable = Runnable {
                monitor()
                mHandler.postDelayed(mRunnable,5000)
            }
            mHandler.postDelayed(mRunnable,5000)
        }


    }


    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder()
            output = Environment.getExternalStorageDirectory().absolutePath + "/recording_$n.mp3"

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setOutputFile(output)

            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun monitor() {
        val amp = mediaRecorder?.maxAmplitude
        val ampString = "$amp:${n++}"
        System.out.println("Sound Level:$ampString")
        textview_sound_level.text = ampString
        stopRecording()

        emailFile(output)

        if (amp!! > 5000) {
            //
            emailFile(output)
            //File(output).renameTo(File(Environment.getExternalStorageDirectory().absolutePath + "/recording_LOUD_$n.mp3"))
        } else {
            File(output).delete()
        }
        startRecording()
        mediaRecorder?.maxAmplitude
    }

    fun emailFile(fileName:String?) {
        val senderEmail = "dabevan@gmail.com"
        val password = "gP0lperr0"
        val toMail = "email@davidbevan.co.uk"

        val email = HtmlEmail()
        email.hostName = "smtp.googlemail.com"
        email.setSmtpPort(465)
        email.setAuthenticator(DefaultAuthenticator(senderEmail, password))
        email.isSSLOnConnect = true
        email.setFrom(senderEmail)
        email.addTo(toMail)
        email.subject = "Test email with inline image sent using Kotlin"
        val kotlinLogoURL = URL("https://kotlinlang.org/assets/images/twitter-card/kotlin_800x320.png")
        val cid = email.embed(kotlinLogoURL, "Kotlin logo")
        email.setHtmlMsg("<html><h1>Kotlin logo</h1><img src=\"cid:$cid\"></html>")
        email.send()
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        Toast.makeText(this,"Resume!", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        recordingStopped = false
    }

    private fun stopRecording(){
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }

    }
}