package com.example.emdrv1;

import android.Manifest;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.Set;

/**
 * Created by ALEXEI on 20/06/2019.
 */

public class ListaDispositivos extends ListActivity {

    BluetoothAdapter meuBluetoothAdapter2 = null;

    //Retornará para fazer a conexão
    static String ENDERECO_MAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> ArrayBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        meuBluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();

       Set<BluetoothDevice> dispositivosPareados = meuBluetoothAdapter2.getBondedDevices();

        if(dispositivosPareados.size() > 0){
            for(BluetoothDevice dispositivo : dispositivosPareados){
                String nomeBt = dispositivo.getName();
                String macBt = dispositivo.getAddress();
                ArrayBluetooth.add(nomeBt + "\n" + macBt);
            }
        }
        setListAdapter(ArrayBluetooth);
    }

    //Ao clicar no item
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String informacaoGeral = ((TextView) v).getText().toString();
        String enderecoMac = informacaoGeral.substring(informacaoGeral.length() - 17);

        //Toast.makeText(getApplicationContext(), "Info: "+ informacaoGeral, Toast.LENGTH_SHORT).show();

        //Retornando para activity principal
        Intent retornaMac = new Intent();
        retornaMac.putExtra(ENDERECO_MAC, enderecoMac);
        setResult(RESULT_OK, retornaMac);

        //Fecha lista
        finish();
    }
}
