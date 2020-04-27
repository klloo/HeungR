package com.midisheetmusic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
//import com.google.firebase.database.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream






class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "KotlinActivity"
    }
/*

    private lateinit var database: DatabaseReference
*/

    lateinit var t : Thread


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
/*
        //데이터 베이스에서 정보 가져오기
        database = FirebaseDatabase.getInstance().reference*/


        data_btn.setOnClickListener {

            
        }

        guitar_btn.setOnClickListener {
            /*val intent: Intent = Intent(this, PlayActivity::class.java)
            startActivity(intent)*/
            val sdCard = Environment.getExternalStorageDirectory()
            val file = File(sdCard, "file.bin")
            var fos: FileOutputStream? = null

            val buf = ByteArray(5)

            // prepare data.
            buf[0] = 0x01
            buf[1] = 0x02
            buf[2] = 0x03
            buf[3] = 0x04
            buf[4] = 0x05

            try {
                // open file.
                fos = FileOutputStream(file)  // fos = new FileOutputStream("file.bin") ;

                // write file.
                fos.write(buf)

            } catch (e: Exception) {
                e.printStackTrace()
            }


            // close file.
            if (fos != null) {
                // catch Exception here or throw.
                try {
                    fos.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            guitar_btn.text = file.exists().toString()

            Toast.makeText(this, file.path+"!!",Toast.LENGTH_LONG).show()

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

/*
    private fun writeNewSong(id: String, album: String, title: String, key: String, quater_note: Int, bpm: Int, pitch: List<String> , chords:List<String> , total_length: Double, midi:List<Int> ) {

        val newsong =  Song(id, album, title, key, bpm, quater_note, pitch, chords, total_length , midi)
        database.child(album).child(title).setValue(newsong)
    }
*/

}

