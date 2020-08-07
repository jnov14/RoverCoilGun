package com.example.jnovak14.coilgunrover;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";

    //Defined variables or widgets
    private TextView textView1;
    private Button fireButton, relayButton, chargeButton;
    private SeekBar coilSeekBar, angleSeekBar, directionSeekBar, rmSeekBar, lmSeekBar;

    //Timer
    CountDownTimer timer;
    String counterDone = "Finished Charging";
    public int counter;

    //Bluetooth
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;

    //final byte delimiter = 33;
    int readBufferPosition = 0;

    //Instructions
    int coilInstruct = 0xA;
    int angleInstruct = 0xF;
    int directionInstruct = 0x8;
    int rmInstruction = 0x6;
    int lmInstruction = 0x4;
    int fireInstruction = 0x2;
    int relayInstruction = 0xD;
    int chargeInstruction = 0x0;
    int dummyData = 0;




    //send BT message function
    public void sendBtMsg(String msg2send) {


        try {



                String msg = msg2send;
                //msg += "\n";
                OutputStream mmOutputStream = mmSocket.getOutputStream();
                mmOutputStream.write(msg.getBytes());
                //Log.d(TAG, "socket now connected and outputting?");

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = (TextView) findViewById(R.id.textView1);
        fireButton = (Button) findViewById(R.id.fireButton);
        relayButton = (Button) findViewById(R.id.relayButton);
        chargeButton = (Button) findViewById(R.id.chargeButton);
        coilSeekBar = (SeekBar) findViewById(R.id.coilSeekBar);
        angleSeekBar = (SeekBar) findViewById(R.id.angleSeekBar);
        directionSeekBar = (SeekBar) findViewById(R.id.directionSeekBar);
        rmSeekBar = (SeekBar) findViewById(R.id.rmSeekBar);
        lmSeekBar = (SeekBar) findViewById(R.id.lmSeekBar);
        timer = null;

        final Handler handler = new Handler();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Runnable thread class to send data and receive data back with pi
        final class workerThread implements Runnable {

            private String btMsg;

            public workerThread(String msg) {
                btMsg = msg;
            }

            public void run()
            {
                sendBtMsg(btMsg);
                //Log.d(TAG,"Called sendBtMsg");
                while(!Thread.currentThread().isInterrupted())
                {

                    int bytesAvailable;
                    //boolean workDone = false;

                    try {



                        final InputStream mmInputStream;
                        mmInputStream = mmSocket.getInputStream();
                        bytesAvailable = mmInputStream.available();
                        //Log.d(TAG,String.format("bytesAvailabel value %d.",bytesAvailable));

                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            byte[] readBuffer = new byte[1024];
                            mmInputStream.read(packetBytes);

                            for(int i=0; i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                readBuffer[readBufferPosition++] = b;
                            }

                            byte[] encodedBytes = new byte [readBufferPosition];
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                            final String data = new String(encodedBytes, "US-ASCII");
                            readBufferPosition = 0;

                            handler.post(new Runnable()
                            {
                                public void run()
                                {
                                    textView1.setText(data);
                                }
                            });

                            //workDone = true;


                            //if (workDone == true){
                            //mmSocket.close();
                            //Log.d(TAG,"break 1");
                            break;
                            //}

                        }

                        else
                        //Log.d(TAG,"break 2");
                        //mmSocket.close();
                        break;


                    } catch (IOException e) {
                        //Log.d(TAG,"No bytes");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }

        //Add implementations
        lmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String lmP = dataInts(lmInstruction, progress);

                (new Thread(new workerThread(lmP))).start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                /*int lmValue = lmSeekBar.getProgress();

                String lmP = dataInts(lmInstruction, lmValue);

                (new Thread(new workerThread(lmP))).start();*/

                lmSeekBar.setProgress(255);
            }
        });

        //Add implementations
        rmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String rmP = dataInts(rmInstruction, progress);

                (new Thread(new workerThread(rmP))).start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                /*int rmValue = rmSeekBar.getProgress();

                String rmP = dataInts(rmInstruction, rmValue);

                (new Thread(new workerThread(rmP))).start();*/

                rmSeekBar.setProgress(255);

            }
        });

        //Add implementations
        coilSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String coilP = dataInts(coilInstruct, progress);

                (new Thread(new workerThread(coilP))).start();


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                /*int coilValue = coilSeekBar.getProgress();

                String coilP = dataInts(coilInstruct, coilValue);

                (new Thread(new workerThread(coilP))).start();*/

                coilSeekBar.setProgress(255);
            }
        });

        //Add implementations
        angleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                int angleValue = angleSeekBar.getProgress();

                String angleP = dataInts(angleInstruct, angleValue);

                (new Thread(new workerThread(angleP))).start();
            }
        });

        //Add implementations
        directionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String directionP = dataInts(directionInstruct, progress);

                (new Thread(new workerThread(directionP))).start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                /*int directionValue = directionSeekBar.getProgress();

                String directionP = dataInts(directionInstruct, directionValue);

                (new Thread(new workerThread(directionP))).start();*/

                directionSeekBar.setProgress(255);
            }
        });

        //Fire button method to send fire command and update textview1
        fireButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fireCmd = dataInts(fireInstruction, dummyData);

                (new Thread(new workerThread(fireCmd))).start();
            }
        });

        //Check Relay button method to send fire command and update textview1
        relayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String relayCmd = dataInts(relayInstruction, dummyData);

                (new Thread(new workerThread(relayCmd))).start();
            }
        });

        //Charge button method to send fire command and update textview1
        chargeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myCounter();

                String chargeCmd = dataInts(chargeInstruction, dummyData);

                (new Thread(new workerThread(chargeCmd))).start();

                chargeButton.setEnabled(false);
                chargeButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chargeButton.setEnabled(true);
                    }
                }, 60000);
                relayButton.setEnabled(false);
                relayButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        relayButton.setEnabled(true);
                    }
                }, 60000);
                fireButton.setEnabled(false);
                fireButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fireButton.setEnabled(true);
                    }
                }, 60000);
            }

        }); //end of charge button onClick


        //Check phone BT status (on/off) and gather paired device info
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("raspberrypi"))
                {
                    //Log.d(TAG,device.getName());
                    mmDevice = device;
                    doConnect(mmDevice);
                    break;
                }
            }
        }


    }//end of onCreate


    //Timer function implemented for charing wait time
    public void myCounter(){
        timer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                textView1.setText("Charging, please wait..."+String.valueOf(counter));
                counter++;
            }

            public void onFinish() {
                textView1.setText(counterDone);
                counter = 0;
            }
        };
        timer.start();
    }

    //Instruction data function
    public String dataInts (int com, int value) {

        int data;
        data = com << 0xB;
        data = data | 0x0080;
        data = data | ((value & 0x0380) << 1);
        data = data | (value & 0x007f);

        String toPiVal = Integer.toHexString(data);

        return toPiVal;
    }

    public void doConnect(BluetoothDevice ttDevice) {
        UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {

            mmSocket = ttDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);

            if (!mmSocket.isConnected()) {
                mmSocket.connect();
                //Log.d(TAG, "socket connected");
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            //Log.d(TAG,"Connection failed");
            e.printStackTrace();
        }
    }

}//end of public class
