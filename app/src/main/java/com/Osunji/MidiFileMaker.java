package com.Osunji;

//JinJin Branch

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import static java.lang.Math.pow;

class MidiFileMaker {

    // Note lengths

    //  We are working with 32 ticks to the crotchet. So

    //  all the other note lengths can be derived from this

    //  basic figure. Note that the longest note we can

    //  represent with this code is one tick short of a

    //  two semibreves (i.e., 8 crotchets)


    static final int SEMIQUAVER = 4;

    static final int QUAVER = 8;

    static final int CROTCHET = 16;

    static final int MINIM = 32;

    static final int SEMIBREVE = 64;


    // Standard MIDI file header, for one-track file

    // 4D, 54... are just magic numbers to identify the

    //  headers

    // Note that because we're only writing one track, we

    //  can for simplicity combine the file and track headers

    static final int header[] = new int[]

            {

                    0x4d, 0x54, 0x68, 0x64, 0x00, 0x00, 0x00, 0x06,

                    0x00, 0x00, // single-track format

                    0x00, 0x01, // one track

                    0x00, 0x10, // 16 ticks per quarter

                    0x4d, 0x54, 0x72, 0x6B

            };


// Standard footer

    static final int footer[] = new int[]

            {

                    0x01, 0xFF, 0x2F, 0x00

            };


// A MIDI event to set the tempo

    static int[] tempoEvent = new int[]

            {

                    0x00, 0xFF, 0x51, 0x03,

                    0x0F, 0x42, 0x40 // Default 1 million usec per crotchet

            };


// A MIDI event to set the key signature. This is irrelent to

//  playback, but necessary for editing applications

    static int[] keySigEvent = new int[]

            {

                    0x00, 0xFF, 0x59, 0x02,

                    0x00, // C

                    0x00  // major

            };




// A MIDI event to set the time signature. This is irrelent to

//  playback, but necessary for editing applications

    static int[] timeSigEvent = new int[]

            {

                    0x00, 0xFF, 0x58, 0x04,

                    0x04, // numerator

                    0x02, // denominator (2==4, because it's a power of 2)

                    0x60, // ticks per click (not used)

                    0x08  // 32nd notes per crotchet

            };





// The collection of events to play, in time order

    protected Vector<int[]> playEvents;


    /**
     * Construct a new MidiFile with an empty playback event list
     */

    public MidiFileMaker() {

        playEvents = new Vector<int[]>();

    }


    /**
     * Write the stored MIDI events to a file
     */

    public byte[] writeToFile() { //throws IOException {

        Log.d("MAKE", "maker1 making!!!!!0!");

        ArrayList<Byte> output = null;


        output = new ArrayList<>();
        for(int i=0;i<header.length;i++){
            output.add((byte) header[i]);
        }

//             Calculate the amount of track data
//
//             _Do_ include the footer but _do not_ include the
//
//             track header


        int size = tempoEvent.length + keySigEvent.length + timeSigEvent.length

                + footer.length;


        for (int i = 0; i < playEvents.size(); i++)

            size += playEvents.elementAt(i).length;


        // Write out the track data size in big-endian format

        // Note that this math is only valid for up to 64k of data

        //  (but that's a lot of notes)

        int high = size / 256;

        int low = size - (high * 256);

        output.add((byte) 0);

        output.add((byte) 0);

        output.add((byte) high);

        output.add((byte) low);


        // Write the standard metadata — tempo, etc

        // At present, tempo is stuck at crotchet=60

        for(int i=0;i<tempoEvent.length;i++){
            output.add((byte) tempoEvent[i]);
        }

        for(int i=0;i<keySigEvent.length;i++){
            output.add((byte) keySigEvent[i]);
        }

        for(int i=0;i<timeSigEvent.length;i++){
            output.add((byte) timeSigEvent[i]);
        }


        // Write out the note, etc., events

        for (int i = 0; i < playEvents.size(); i++) {
            int[] arr = playEvents.elementAt(i);
            for(int j=0;j<arr.length;j++){
                output.add((byte) arr[j]);
            }
        }


        // Write the footer and close

        //fos.write(intArrayToByteArray(footer));
        for(int i=0;i<footer.length;i++){
            output.add((byte) footer[i]);
        }

        byte[] byteArr = new byte[output.size()];
        for(int i=0;i<byteArr.length;i++){
            byteArr[i] = output.get(i);
        }



/*

        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/hello_.mid";


        File file = new File(path) ;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("TAG",e.toString() + "write To File ");
        }
        try {
            fos.write(byteArr);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

*/

        return byteArr;
    }


    /**
     * Convert an array of integers which are assumed to contain
     * <p>
     * unsigned bytes into an array of bytes
     */

    protected static byte[] intArrayToByteArray(int[] ints) {

        int l = ints.length;

        byte[] out = new byte[ints.length];

        for (int i = 0; i < l; i++) {

            out[i] = (byte) ints[i];

        }

        return out;

    }


    /**
     * Store a note-on event
     */

    public void noteOn(int delta, int note, int velocity) {

        int[] data = new int[4];

        data[0] = delta;

        data[1] = 0x90;

        data[2] = note;

        data[3] = velocity;

        playEvents.add(data);

    }


    /**
     * Store a note-off event
     */

    public void noteOff(int delta, int note) {

        int[] data = new int[4];

        data[0] = delta;

        data[1] = 0x80;

        data[2] = note;

        data[3] = 0;

        playEvents.add(data);

    }


    /**
     * Store a program-change event at current position
     */

    public void progChange(int prog) {

        int[] data = new int[3];

        data[0] = 0;

        data[1] = 0xC0;

        data[2] = prog;

        playEvents.add(data);

    }


    /**
     * Store a note-on event followed by a note-off event a note length
     * <p>
     * later. There is no delta value — the note is assumed to
     * <p>
     * follow the previous one with no gap.
     */

    public void noteOnOffNow(int duration, int note, int velocity) {

        noteOn(0, note, velocity);

        noteOff(duration, note);

    }


    public void noteSequenceFixedVelocity(ArrayList<Integer> sequence, int velocity) {

        boolean lastWasRest = false;

        int restDelta = 0;

        for (int i = 0; i < sequence.size(); i += 2) {

            int note = sequence.get(i);

            int duration = sequence.get(i + 1);

                if (note < 0) {

                    // This is a rest

                    restDelta += duration;

                    lastWasRest = true;

            } else {

                // A note, not a rest

                if (lastWasRest) {

                    noteOn(restDelta, note, velocity);

                    noteOff(duration, note);

                } else {

                    noteOn(0, note, velocity);

                    noteOff(duration, note);

                }

                restDelta = 0;

                lastWasRest = false;

            }

        }

    }




    /**
     * frequency -> MidiNumber
     * */


    /**
     * Test method — creates a file test1.mid when the class
     * <p>
     * is executed
     */

    public void setTempo(int bpm){
        // 1sec == 1,000,000 microseconds (1 마이크로초 = 백만분의 1초)
        int tempo = 60 * 1000000 / bpm ; // 1박자에 걸리는 시간 ( micro sec )

        int n1 = 0;
        int n2 = 0;
        int n3 = 0;

        String temp = Integer.toHexString(tempo);


        n3 = tempo % (16*16);
        n2 = tempo / ((int)pow(16,2)) % (int)pow(16,2);
        n1 = tempo / ((int)pow(16, 4)) % (int)pow(16,2);
/*
        System.out.println(temp);
        System.out.println( Integer.toHexString(n1) +" " + Integer.toHexString(n2) + " "+Integer.toHexString(n3));*/

        tempoEvent =  new int[]

                {
                        0x00, 0xFF, 0x51, 0x03,
                        n1,n2,n3
                };
    }

    public void setTimeSignature(int dd, int nn ){

        timeSigEvent = new int[]

                {

                        0x00, 0xFF, 0x58, 0x04,

                        nn, // numerator

                        dd, // denominator (2==4, because it's a power of 2)

                        0x60, // ticks per click (not used)

                        0x08  // 32nd notes per crotchet

                };
    }

    public void setKeySignature(int key){


        keySigEvent = new int[]

                {

                        0x00, 0xFF, 0x59, 0x02,

                        key ,

                        0x00  // major

                };
    }

}