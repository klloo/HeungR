package com.midisheetmusic

import android.graphics.Color
import android.media.AudioManager
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_play.*
import java.text.SimpleDateFormat
import kotlin.experimental.and


class PlayActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "KotlinActivity"
    }

    private lateinit var database: DatabaseReference

    lateinit var t : Thread


    var ID : String = ""
    var Album = ""
    var Title:String  = ""
    var Length :Double = 0.0

    var Quater :Int  = 0
    var Bpm : Int = 0
    var Key :String = ""

    var Chords:List<String>  = mutableListOf("")
    var Pitch:List<String> =  mutableListOf("")

    var soundPool: SoundPool? = null
    var chord :Int = 0
    var timing :Int = 0

    var playing:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)


        //데이터 베이스에서 정보 가져오기
        database = FirebaseDatabase.getInstance().reference


        Bpm = 60
        Quater = 4 // 4분의 4박
        timing = 30 // 계산이 이상한데 .. 이 시간마다 코드 바꿔줘야 함




        // 사운드풀 객체 생성
        soundPool = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        val e4 = soundPool!!.load(this, R.raw.e4, 1)
        val a2 = soundPool!!.load(this, R.raw.a2, 1)
        val d3  = soundPool!!.load(this, R.raw.d3, 1)
        chord = e4

        var streamid :Int = -1
        var time = 0
        t = Thread(Runnable {
            //첫 시작한 현재시간
            val start = System.currentTimeMillis()

            //시간포맷팅을 위한 포맷설정
            val sdf = SimpleDateFormat("mm:ss:SSS")

            while (!t.isInterrupted()) {
                runOnUiThread {
                    time++;
                    //쓰레드가 돌때마다 계속 현재시간 갱신
                    val end = System.currentTimeMillis()
                    //진행된시간을 계산하여 포맷에 맞게 TextView에 뿌리기
                    timeText.setText(sdf.format(end - start).substring(0, 8))

                    if( time == timing){
                        time = 0

                        //코드 변경
                        if( chord == e4) {
                            chord = a2
                            cur_chord.text= "a2"
                            streamid = playChord(streamid)
                        }
                        else {
                            chord = e4
                            cur_chord.text = "e4"
                            streamid = playChord(streamid)
                        }

                    }

                }
                //0.01초마다 Thread돌리기
                SystemClock.sleep(100)
            }
        })

        playBtn.setOnClickListener {
            if(playing) {

                playBtn.setBackgroundColor(Color.TRANSPARENT)
            }
            else {
                t.start()
                playBtn.setBackgroundColor(Color.GRAY)
            }
        }
        button.setOnClickListener {

            loadData("Test_Album","test_track1")
        }


    }


    fun ready2soundPool(){

    }

    fun playChord( id :Int ) : Int {
        if( id != -1)
            soundPool?.stop(id)

        Log.d("TAG", "play :"+ id + ": " +chord);

        var soundId = soundPool?.play(chord,1f,1f,0,1,1f)
        return soundId!!
    }


    private fun loadData( album : String, title: String ) {
        val dbRef = FirebaseDatabase.getInstance().reference

        val dataQuery = dbRef.child(album).orderByChild("title").equalTo(title)
        dataQuery.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (issue in dataSnapshot.children) {
                        // do something with the individual "issues"
                        val data = issue.getValue(Song::class.java)

                        Log.d("TAG", " $title find!->" + data?.title )

                        Album = data!!.album.toString()
                        Title = data.title.toString()
                        ID = data.id.toString()
                        Bpm = data.bpm
                        Length = data.total_length
                        Chords = data.chord as List<String>
                        Key = data.key

                        textView.text = "$Album + $Title + $ID + $Bpm + $Length + $Key \n $Chords \n"
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

                Log.d("TAG", "onCancelled!" )
            }
        })
    }

    fun byte2Int () : List<Int>{
        val src : ByteArray = ByteArray(10)

        val dstLength =src.size
        val dst = IntArray(dstLength)

        for (i in 0 until dstLength) {

            dst[i] = src[i].toInt()
        }
        val dstList : List<Int> = dst.toList()
        return dstList
    }

    fun int2Byte(srcList :List<Int>): ByteArray {

        var src = srcList.toIntArray()

        val dstLength = src.size
        val dst = ByteArray(dstLength)



        for (i in 0 until dstLength) {
             dst[i] = src[i].toByte()
        }
        return dst
    }

}
