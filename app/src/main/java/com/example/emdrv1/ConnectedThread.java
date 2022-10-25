package com.example.emdrv1;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ALEXEI on 05/07/2019.
 */

public class ConnectedThread extends Thread{

    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    Handler mHandler;
    int MESSAGE_READ;

    public ConnectedThread(BluetoothSocket socket, Handler mHandler, int MESSAGE_READ) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        this.mHandler = mHandler;
        this.MESSAGE_READ = MESSAGE_READ;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    //RECEBE OS DADOS DO ARDUINO
    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()


        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);

                //Transforma bytes em string
                String dadosBt = new String(buffer, 0, bytes);

                //Envia
                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, dadosBt).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }

    }

    /* ENVIA DADOS */
    public void enviar(String dadosEnviar) {
        byte[] msgBuffer = dadosEnviar.getBytes();
        try {
            mmOutStream.write(msgBuffer);
        } catch (IOException e) { }
    }


}
