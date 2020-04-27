package com.midisheetmusic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream






class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "KotlinActivity"
    }

    lateinit var t : Thread


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        choose_btn.setOnClickListener {
            val intent: Intent = Intent(this, ChooseSongActivity::class.java)
            startActivity(intent)
        }

        recording_btn.setOnClickListener {
            val intent: Intent = Intent(this, RecordingActivity::class.java)
            startActivity(intent)
        }



    }

/*
    private fun writeNewSong(id: String, album: String, title: String, key: String, quater_note: Int, bpm: Int, pitch: List<String> , chords:List<String> , total_length: Double, midi:List<Int> ) {

        val newsong =  Song(id, album, title, key, bpm, quater_note, pitch, chords, total_length , midi)
        database.child(album).child(title).setValue(newsong)
    }
*/

}

