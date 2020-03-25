package com.midisheetmusic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.media.SoundPool
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.*
import java.util.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "KotlinActivity"
    }

    private lateinit var database: DatabaseReference

    lateinit var t : Thread


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //데이터 베이스에서 정보 가져오기
        database = FirebaseDatabase.getInstance().reference


        data_btn.setOnClickListener {
            val ID = "user1"
            val album = "Test_Album"
            val title = "test_track1"
            val key = "C"
            val chords = listOf("C C G G","G G C C","D D G G")
            val  pitch  =  listOf("B2 D3 A3 B2", "B2 D3 A3 B2", "B2 D3 A3 B2")
            val bpm = 60
            val quater = 4 // 4분의 4박
            val total_length = 30.0 // ms 기준 1,000ms = 1sec
            val midi:List<Int>  =  listOf(1,2,3)


            writeNewSong(ID, album, title, key, bpm, quater, pitch, chords, total_length, midi)

            writeNewSong(ID, album, "test_track2", key, bpm, quater, pitch, chords, total_length, midi)
        }

        guitar_btn.setOnClickListener {
            val intent: Intent = Intent(this, PlayActivity::class.java)
            startActivity(intent)
        }

        choose_btn.setOnClickListener {
            val intent: Intent = Intent(this, ChooseSongActivity::class.java)
            startActivity(intent)
        }

        recording_btn.setOnClickListener {
            val intent: Intent = Intent(this, RecordingActivity::class.java)
            startActivity(intent)
        }



    }

    private fun writeNewSong(id: String, album: String, title: String, key: String, quater_note: Int, bpm: Int, pitch: List<String> , chords:List<String> , total_length: Double, midi:List<Int> ) {

        val newsong =  Song(id, album, title, key, bpm, quater_note, pitch, chords, total_length , midi)
        database.child(album).child(title).setValue(newsong)
    }

}

