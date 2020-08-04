package com.midisheetmusic;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

class MidiFileMaker2 extends MidiFileMaker{

    // Format 1 로 바꾸고 track 개수 3으로 변경해준 MidiFile Header

    static final int header[] = new int[]

            {

                    0x4d, 0x54, 0x68, 0x64, 0x00, 0x00, 0x00, 0x06,

                    0x00, 0x01, // single-track format -> format 1

                    0x00, 0x03, // one track -> three tracks

                    0x00, 0x10, // 16 ticks per quarter

                    0x4d, 0x54, 0x72, 0x6B

            };

    // 반주 저장할 vector

    protected Vector<int[]> bplayEvents;

    public MidiFileMaker2() {
        super();
        bplayEvents = new Vector<int[]>();
    }

    static final int trackHeader[] = new int[]{

            0x4d, 0x54, 0x72, 0x6B
    };

    public byte[] writeToFile(ArrayList<ArrayList<Integer>> chords,  int key, int nn, int velocity) { //throws IOException {


        ArrayList<Byte> output = null;

        output = new ArrayList<>();
        for (int i = 0; i < header.length; i++) {
            output.add((byte) header[i]);
        }

        int size = tempoEvent.length + keySigEvent.length + timeSigEvent.length

                + footer.length;

        int high = size / 256;

        int low = size - (high * 256);

        output.add((byte) 0);

        output.add((byte) 0);

        output.add((byte) high);

        output.add((byte) low);

        for (int i = 0; i < tempoEvent.length; i++) {
            output.add((byte) tempoEvent[i]);
        }

        for (int i = 0; i < keySigEvent.length; i++) {
            output.add((byte) keySigEvent[i]);
        }

        for (int i = 0; i < timeSigEvent.length; i++) {
            output.add((byte) timeSigEvent[i]);
        }

        for (int i = 0; i < footer.length; i++) {
            output.add((byte) footer[i]);
        }

        for (int i = 0; i < trackHeader.length; i++) {
            output.add((byte) trackHeader[i]);
        }

        size = footer.length;

        for (int i = 0; i < playEvents.size(); i++)
            size += playEvents.elementAt(i).length;

        high = size / 256;

        low = size - (high * 256);

        output.add((byte) 0);

        output.add((byte) 0);

        output.add((byte) high);

        output.add((byte) low);

        for (int i = 0; i < playEvents.size(); i++) {
            int[] arr = playEvents.elementAt(i);
            for (int j = 0; j < arr.length; j++) {
                output.add((byte) arr[j]);
            }
        }

        for (int i = 0; i < footer.length; i++) {
            output.add((byte) footer[i]);
        }

        //반주 이벤트
        makebPlayEvents(chords, key, nn);

        for (int i = 0; i < trackHeader.length; i++) {
            output.add((byte) trackHeader[i]);
        }

        size = footer.length;

        for (int i = 0; i < bplayEvents.size(); i++)
            size += bplayEvents.elementAt(i).length;

        high = size / 256;

        low = size - (high * 256);

        output.add((byte) 0);

        output.add((byte) 0);

        output.add((byte) high);

        output.add((byte) low);

        for (int i = 0; i < bplayEvents.size(); i++) {
            int[] arr = bplayEvents.elementAt(i);
            for (int j = 0; j < arr.length; j++) {
                output.add((byte) arr[j]);
            }
        }

        for (int i = 0; i < footer.length; i++) {
            output.add((byte) footer[i]);
        }

        byte[] byteArr = new byte[output.size()];
        for(int i=0;i<byteArr.length;i++){
            byteArr[i] = output.get(i);
        }

        return byteArr;
    }



    public void bnoteOn(int delta, int note, int velocity) {

        int[] data = new int[4];

        data[0] = delta;

        data[1] = 0x90;

        data[2] = note;

        data[3] = velocity;

        bplayEvents.add(data);

    }


    /**
     * Store a note-off event
     */

    public void bnoteOff(int delta, int note) {

        int[] data = new int[4];

        data[0] = delta;

        data[1] = 0x80;

        data[2] = note;

        data[3] = 0;

        bplayEvents.add(data);

    }



    public void writeChord(int nn, int num, boolean is7, boolean isMinor7){

        if(is7){

            int c1 = 60; //도
            int c2 = 64; //미
            int c3 = 67; //솔
            int c4 = 70; //시b

            c1 += num;
            c2 += num;
            c3 += num;
            c4 += num;

            if( c1 > 73)
                c1 -= 12;
            if( c2 > 73)
                c2 -= 12;
            if( c3 > 73)
                c3 -= 12;
            if( c4 > 73)
                c4 -= 12;


            // nn/4박 C7코드
            bnoteOn(0, c1, 100);
            bnoteOn(0, c2, 100);
            bnoteOn(0, c3, 100);
            bnoteOn(0, c4, 100);
            bnoteOff(nn*16, c1);
            bnoteOff(0, c2);
            bnoteOff(0, c3);
            bnoteOff(0, c4);
        }
        else if( isMinor7){
            int c1 = 60; //도
            int c2 = 63; //미b
            int c3 = 67; //솔
            int c4 = 70; //시b
            c1 += num;
            c2 += num;
            c3 += num;
            c4 += num;

            if( c1 > 73)
                c1 -= 12;
            if( c2 > 73)
                c2 -= 12;
            if( c3 > 73)
                c3 -= 12;
            if( c4 > 73)
                c4 -= 12;


            //4/4박 C-7코드
            bnoteOn(0, c1, 100);
            bnoteOn(0, c2, 100);
            bnoteOn(0, c3, 100);
            bnoteOn(0, c4, 100);
            bnoteOff(nn*16, c1);
            bnoteOff(0, c2);
            bnoteOff(0, c3);
            bnoteOff(0, c4);
        }
        else{
            int c1 = 60;
            int c2 = 64;
            int c3 = 67;

            c1 += num;
            c2 += num;
            c3 += num;

            if( c1 > 73)
                c1 -= 12;
            if( c2 > 73)
                c2 -= 12;
            if( c3 > 73)
                c3 -= 12;

            //4/4박 C코드
            bnoteOn(0, c1, 100);
            bnoteOn(0, c2, 100);
            bnoteOn(0, c3, 100);
            bnoteOff(nn*16, c1);
            bnoteOff(0, c2);
            bnoteOff(0, c3);

        }

    }


    public void makebPlayEvents(ArrayList<ArrayList<Integer>> chords, int key,  int nn){

        int Ckey[] = {0,2,4,5,7,9};

        int num;
        boolean isMinor7 , is7;

        Log.d("TAG", "key : " + key);


        for (ArrayList<Integer> chord : chords){
            int index;
            if(chord.size() == 0)
                index = 0;
            else
                index = chord.get(0);

            num = Ckey[index] + key;
            is7 = (index== 4);
            isMinor7 = ( index == 1 || index ==2 || index ==5 );

            writeChord(nn, num, is7, isMinor7);
        }



    }
}