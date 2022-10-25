package com.example.emdrv1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.UUID;

import android.app.Activity;
import android.os.Message;

import android.util.Log;
import android.view.View;
import android.widget.Button;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    //Constantes
    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXAO = 2;

    //Dados vindo do bluetooth
    private static final int MESSAGE_READ = 3;

    //Conexão para o ESP
    ConnectedThread connectedThread;
    Handler mHandler;

    //Acumuda o que vem do bluetooth
    StringBuilder dadosBluetooth = new StringBuilder();

    BluetoothAdapter bluetooth;

    //Dispositivo remoto
    BluetoothDevice meuDevice;

    //Para controle dos dados
    BluetoothSocket meuSocket;

    //Verifica se a conexão está em andamento ou não
    boolean conexao = false;

    //Guarda o MAC
    private static String MAC;

    //Para criar um canal de comunicação
    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Obtenho o bluetooth
        bluetooth = BluetoothAdapter.getDefaultAdapter();

        //Se não tiver bluetooth mostra mensagem
        if (bluetooth == null) {
            Toast.makeText(this, "Seu dispositivo não possue Bluetooth", Toast.LENGTH_LONG).show();
        } else if (!bluetooth.isEnabled()) {
            //Se não tiver ativado o Bluetooth abro tela para ativar
            Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //Traz o resultado da ativação

            startActivityForResult(ativaBluetooth, SOLICITA_ATIVACAO);
        }
    }

    public void ligar_red(View v)
    {
        //Vejo se está conectado
        if(conexao){
            //Envia a mensagem para o ESP32
            connectedThread.enviar("0"); //liga
        }else{
            Toast.makeText(this, "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
        }
    }

    public void ligar_green(View v)
    {
        //Vejo se está conectado
        if(conexao){
            //Envia a mensagem para o ESP32
            connectedThread.enviar("1"); //desliga
        }else{
            Toast.makeText(this, "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
        }
    }

    public void ligar_blue(View v)
    {
        //Vejo se está conectado
        if(conexao){
            //Envia a mensagem para o ESP32
            connectedThread.enviar("2"); //desliga
        }else{
            Toast.makeText(this, "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
        }
    }

    public void aumenta_velocidade(View v)
    {
        //Vejo se está conectado
        if(conexao){
            //Envia a mensagem para o ESP32
            connectedThread.enviar("3"); //desliga
        }else{
            Toast.makeText(this, "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
        }
    }

    public void diminui_velocidade(View v)
    {
        //Vejo se está conectado
        if(conexao){
            //Envia a mensagem para o ESP32
            connectedThread.enviar("4"); //desliga
        }else{
            Toast.makeText(this, "Bluetooth não está conectado", Toast.LENGTH_LONG).show();
        }
    }


    public void conectar_clique(View v)
    {
        //Se já tiver conectado o Bluetooth ele será desconectado
        if (conexao){
            try{
                meuSocket.close();
                conexao = false;
                Toast.makeText(this, "Bluetooth foi desconectado", Toast.LENGTH_LONG).show();
            }catch (IOException erro){
                Toast.makeText(this, "Erro: "+erro.getMessage(), Toast.LENGTH_LONG).show();
            }
        }else{
            //Abre listagem de dispositivaos pareados, para usuário poder escolher um para conectar
            Intent abreLista = new Intent(getApplicationContext(), ListaDispositivos.class);
            startActivityForResult(abreLista, SOLICITA_CONEXAO);
        }
    }

    //Trato o resultado do dispositivo pareado escolhido para conectar ou da busca pelo Bluetooth *****************************
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case SOLICITA_ATIVACAO: //Retorno para a ativação do Bluetooth
                if(resultCode == Activity.RESULT_OK){
                    Toast.makeText(this, "Bluetooth foi ativado", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this, "Erro ao ativar Bluetooth", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case SOLICITA_CONEXAO: //Retorno da escolha do dispositivo pareado
                if(resultCode == Activity.RESULT_OK){
                    //Pego o MAC
                    MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);

                    //Conecta com dispositivo
                    meuDevice = bluetooth.getRemoteDevice(MAC);

                    //Cria canal de comunicação
                    try{
                        meuSocket = meuDevice.createInsecureRfcommSocketToServiceRecord(MEU_UUID);
                        meuSocket.connect();

                        conexao = true;

                        //Conecto no ESP
                        connectedThread = new ConnectedThread(meuSocket, null, MESSAGE_READ);
                        connectedThread.start();

                        Toast.makeText(this, "Você foi conectado com: "+MAC, Toast.LENGTH_LONG).show();
                    }catch (IOException erro){
                        conexao = false;
                        Toast.makeText(this, "Erro conexão: "+erro.getMessage(), Toast.LENGTH_LONG).show();
                        Log.v("erro", erro.getMessage());
                    }
                }else{
                    Toast.makeText(this, "Falha ao obter o MAC", Toast.LENGTH_LONG).show();
                }
        }
    }

}