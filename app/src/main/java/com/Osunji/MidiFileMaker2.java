package com.Osunji;


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


    public byte[] writeToFile(ArrayList<ArrayList<Integer>> chords,  int key, int nn, int velocity, int octa) {

        Log.d("MAKE", "maker2 making!!!!!0!");

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
//멜로디 작성

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
        //================================ 반주 이벤트 작성 ==============================================
        makebPlayEvents(chords, octa, key, nn);


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


        //bplayEvents 작성

        for (int i = 0; i < bplayEvents.size(); i++) {
            int[] arr = bplayEvents.elementAt(i);
            for (int j = 0; j < arr.length; j++) {
                output.add((byte) arr[j]);
            }
        }


        for (int i = 0; i < footer.length; i++) {
            output.add((byte) footer[i]);
        }



        // byte[] 로 변경

        byte[] byteArr = new byte[output.size()];

        for(int i=0;i<byteArr.length;i++){
            byteArr[i] = output.get(i);
        }

        return byteArr;
    }

    public void writeToFile(File file, ArrayList<ArrayList<Integer>> chords, int key, int nn, int octa) { //throws IOException {


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        try {

            fos.write(intArrayToByteArray(header));

            // Calculate the amount of track data

            // _Do_ include the footer but _do not_ include the

            // track header


            int size = tempoEvent.length + keySigEvent.length + timeSigEvent.length

                    + footer.length;



            // Write out the track data size in big-endian format

            // Note that this math is only valid for up to 64k of data

            //  (but that's a lot of notes)

            int high = size / 256;

            int low = size - (high * 256);

            fos.write((byte) 0);

            fos.write((byte) 0);

            fos.write((byte) high);

            fos.write((byte) low);


            // Write the standard metadata — tempo, etc

            // At present, tempo is stuck at crotchet=60

            fos.write(intArrayToByteArray(tempoEvent));

            fos.write(intArrayToByteArray(keySigEvent));

            fos.write(intArrayToByteArray(timeSigEvent));

            fos.write(intArrayToByteArray(footer));



            // 멜로디 작성

            fos.write(intArrayToByteArray(trackHeader));
            size = footer.length;
            for (int i = 0; i < playEvents.size(); i++)
                size += playEvents.elementAt(i).length;
            high = size / 256;

            low = size - (high * 256);

            fos.write((byte) 0);

            fos.write((byte) 0);

            fos.write((byte) high);

            fos.write((byte) low);

            for (int i = 0; i < playEvents.size(); i++) {

                fos.write(intArrayToByteArray(playEvents.elementAt(i)));

            }
            // Write the footer
            fos.write(intArrayToByteArray(footer));


            //반주 이벤트
            makebPlayEvents(chords, octa, key,  nn );

            //반주 작성


            // Write the standard metadata — tempo, etc

            // At present, tempo is stuck at crotchet=60



            // Write out the note, etc., events

            fos.write(intArrayToByteArray(trackHeader));
            size = footer.length;
            for (int i = 0; i < bplayEvents.size(); i++)
                size += bplayEvents.elementAt(i).length;
            high = size / 256;

            low = size - (high * 256);

            fos.write((byte) 0);

            fos.write((byte) 0);

            fos.write((byte) high);

            fos.write((byte) low);

            for (int i = 0; i < bplayEvents.size(); i++) {

                fos.write(intArrayToByteArray(bplayEvents.elementAt(i)));

            }


            // Write the footer and close

            fos.write(intArrayToByteArray(footer));
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();

        }


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



    public void writeChord(int octa, int nn, int num,  boolean is7, boolean isMinor7){
        int velocity = 90;

        int octave =  octa - (octa %12); //Ckey

        if( 60 <= octave) // 사용자의 목소리가 5옥타브 이상일때만 낮추기
            octave -= 12;

        if(is7){

            int c1 = 0; //도
            int c2 = 4; //미
            int c3 = 7; //솔
            int c4 = 10; //시b

            c1 += num;
            c2 += num;
            c3 += num;
            c4 += num;

            c1 = c1%12 + octave;
            c2 = c2%12 + octave;
            c3 = c3%12 + octave;
            c4 = c4%12 + octave;



            // nn/4박 C7코드
            bnoteOn(0, c1, velocity);
            bnoteOn(0, c2, velocity);
            bnoteOn(0, c3, velocity);
            bnoteOn(0, c4, velocity);
            bnoteOff(nn*16, c1);
            bnoteOff(0, c2);
            bnoteOff(0, c3);
            bnoteOff(0, c4);
        }
        else if( isMinor7){
            int c1 = 0; //도
            int c2 = 3; //미b
            int c3 = 7; //솔
            int c4 = 10; //시b
            c1 += num;
            c2 += num;
            c3 += num;
            c4 += num;


            c1 = c1%12 + octave;
            c2 = c2%12 + octave;
            c3 = c3%12 + octave;
            c4 = c4%12 + octave;


            //4/4박 C-7코드
            bnoteOn(0, c1, velocity);
            bnoteOn(0, c2, velocity);
            bnoteOn(0, c3, velocity);
            bnoteOn(0, c4, velocity);
            bnoteOff(nn*16, c1);
            bnoteOff(0, c2);
            bnoteOff(0, c3);
            bnoteOff(0, c4);
        }
        else{
            int c1 = 0;
            int c2 = 4;
            int c3 = 7;

            c1 += num;
            c2 += num;
            c3 += num;


            c1 = c1%12 + octave;
            c2 = c2%12 + octave;
            c3 = c3%12 + octave;

            //4/4박 C코드
            bnoteOn(0, c1, velocity);
            bnoteOn(0, c2, velocity);
            bnoteOn(0, c3, velocity);
            bnoteOff(nn*16, c1);
            bnoteOff(0, c2);
            bnoteOff(0, c3);

        }

    }


    public void makebPlayEvents(ArrayList<ArrayList<Integer>> chords, int octa,  int key,  int nn){

        int Ckey[] = {0,2,4,5,7,9};

        int num;
        boolean isMinor7 , is7;


        for (ArrayList<Integer> chord : chords){
            int index;
            if(chord.size() == 0)
                index = 0;
            else
                index = chord.get(0);

            num = Ckey[index] + key;
            is7 = (index== 4);
            isMinor7 = ( index == 1 || index ==2 || index ==5 );

            writeChord(octa, nn, num, is7, isMinor7);
        }



    }
}