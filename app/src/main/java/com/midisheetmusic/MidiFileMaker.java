package com.midisheetmusic;

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

    static final int keySigEvent[] = new int[]

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

    public void writeToFile(String filename) throws IOException {

        FileOutputStream fos = new FileOutputStream(filename);


        fos.write(intArrayToByteArray(header));


        // Calculate the amount of track data

        // _Do_ include the footer but _do not_ include the

        // track header


        int size = tempoEvent.length + keySigEvent.length + timeSigEvent.length

                + footer.length;


        for (int i = 0; i < playEvents.size(); i++)

            size += playEvents.elementAt(i).length;


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


        // Write out the note, etc., events

        for (int i = 0; i < playEvents.size(); i++) {

            fos.write(intArrayToByteArray(playEvents.elementAt(i)));

        }


        // Write the footer and close

        fos.write(intArrayToByteArray(footer));

        fos.close();

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


    public void noteSequenceFixedVelocity(int[] sequence, int velocity) {

        boolean lastWasRest = false;

        int restDelta = 0;

        for (int i = 0; i < sequence.length; i += 2) {

            int note = sequence[i];

            int duration = sequence[i + 1];

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





    public int freq2MidiNum(double freq){
        int MidiNum = 0;
        int octa = 0;
        int mul;
        double C , Db , D, Eb , E, F, Gb, G,Ab, A, Bb, B;
        C = 32.703;
        Db = 34.648;
        D = 36.708;
        Eb = 38.891;
        E = 41.203;
        F = 43.654;
        Gb = 46.249;
        G = 48.999;
        Ab = 51.913;
        A = 55.000;
        Bb = 58.270;
        B = 61.735;

        //계산

        //setp 1 옥타브 계산
        if( C <= freq && freq < 2 * C)
            octa = 1;
        else if( 2 * C <= freq && freq < 4* C)
            octa = 2;
        else if( 4 * C <= freq && freq < 8 * C)
            octa = 3;
        else if(8 * C <= freq && freq < 16 * C)
            octa = 4;
        else if(16 * C <= freq && freq < 32 * C)
            octa = 5;
        else if(32 * C <= freq && freq < 64 * C)
            octa = 6;
        else if(64 * C <= freq && freq < 128 * C)
            octa = 7;
        else if(128 * C <= freq && freq < 256 * C)
            octa = 8;

        mul = (int) pow(2, octa-1); // 1옥타브는 C 2옥타브는 C*2 3옥타브는 C * 2^2 4옥타브는 C*2^3

        //setp 2 음정 계산

        if(freq >= (C * mul) && freq < ( Db* mul )) {
            //C
            if( (freq - (C* mul)) <= ((Db*mul) - freq) )
                MidiNum = 12 * octa + 12;
            else // Db
                MidiNum = 12 * octa + 12 + 1;
        }
        else if(freq >= (Db * mul)  && freq < (D* mul)) {
            //Db
            if( (freq - (Db* mul)) <= ((D*mul) - freq) )
                MidiNum = 12 * octa + 12 + 1 ;
            else //D
                MidiNum = 12 * octa + 12 + 2;
        }
        else if(freq >= (D * mul) && freq < (Eb* mul)) {
            //D
            if( (freq - (D* mul)) <= ((Eb*mul) - freq) )
                MidiNum = 12 * octa + 12 + 2 ;
            else // Eb
                MidiNum = 12 * octa + 12 + 3;
        }
        else if(freq >= (Eb * mul) && freq < (E* mul)) {
            //Eb
            if( (freq - (Eb* mul)) <= ((E*mul) - freq) )
                MidiNum = 12 * octa + 12 + 3 ;
            else // E
                MidiNum = 12 * octa + 12 + 4;
        }
        else if(freq >= (E * mul) && freq < (F* mul)) {
            //E
            if( (freq - (E* mul)) <= ((F*mul) - freq) )
                MidiNum = 12 * octa + 12 + 4 ;
            else // F
                MidiNum = 12 * octa + 12 + 5;
        }
        else if(freq >= (F * mul) && freq <= (Gb* mul)) {
            //F
            if( (freq - (F* mul)) <= ((Gb*mul) - freq) )
                MidiNum = 12 * octa +12 + 5 ;
            else // Gb
                MidiNum = 12 *octa + 12 + 6;
        }
        else if(freq >= (Gb * mul) && freq < (G* mul)) {
            //Gb
            if( (freq - (Gb* mul)) <= ((G*mul) - freq) )
                MidiNum = 12 * octa +12 + 6 ;
            else // G
                MidiNum = 12 * octa + 12 + 7;
        }
        else if(freq >= (G * mul)  && freq < (Ab * mul)) {
            //G
            if( (freq - (G* mul)) <= ((Ab*mul) - freq) )
                MidiNum = 12 * octa +12 + 7 ;
            else // Ab
                MidiNum = 12 * octa + 12 + 8;
        }
        else if(freq >= (Ab * mul) && freq < (A* mul)) {
            //Ab
            if( (freq - (Ab* mul)) <= ((A*mul) - freq) )
                MidiNum = 12 * octa +12 + 8 ;
            else // A
                MidiNum = 12 * octa + 12 + 9;
        }
        else if(freq >= (A * mul) && freq < (Bb* mul)) {
            //A
            if( (freq - (A* mul)) <= ((Bb*mul) - freq) )
                MidiNum = 12 * octa +12 + 9 ;
            else
                MidiNum = 12 * octa + 12 +10;
        }
        else if(freq >= (Bb * mul) && freq < (B* mul)) {
            //Bb
            if( (freq - (Bb* mul)) <= ((B*mul) - freq) )
                MidiNum = 12 * octa +12 + 10 ;
            else // B
                MidiNum = 12 * octa + 12 + 11;
        }
        else if(freq >= (B * mul) && freq < (2 * C * mul)) {
            //B
            if( (freq - (B* mul)) <= ((C*mul) - freq) )
                MidiNum = 12 * octa +12 + 11 ;
            else // C
                MidiNum = 12 * octa + 12 + 12;
        }

        //  System.out.println("octa " + octa + " freq  "+freq + " || MidiNum" + MidiNum);
        return MidiNum;
    }




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

        System.out.println(temp);
        System.out.println( Integer.toHexString(n1) +" " + Integer.toHexString(n2) + " "+Integer.toHexString(n3));

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

}
