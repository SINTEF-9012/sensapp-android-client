package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.Sensor;
import android.util.Log;
import org.sensapp.android.sensappdroid.api.SensAppUnit;
import org.sintef.jarduino.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 05/06/13
 * Time: 14:25
 *
 * This is an interface used to manage sensors provided by the Android system.
 * A generalisation is possible by passing arguments through the constructor like type, unit...
 * At the moment there are only sensors with 1 or 3 data, so 2 data sensor or not implemented.
 */

public class BluetoothSensor extends AbstractSensor {
    private String sensorName;
    static final int DEFAULT_RATE = 5000;
    final BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mmDevice = null;
    private BluetoothSocket mmSocket;
    private JArduino mArduino;
    static int sensorValue = 2;
    private String mUUID = "00001101-0000-1000-8000-00805F9B34FB";

    public BluetoothSensor(String composite, String sensorName, BluetoothAdapter bluetoothDevice, String deviceName) {
        setEntryLevel();
        initData();
        this.sensorName = sensorName;
        mBluetoothAdapter = bluetoothDevice;
        mComposite = composite;

        List<String> mArray = new ArrayList<String>();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if(device.getName().equals(deviceName))
                    mmDevice = device;
                mArray.add(device.getName() + "\n" + device.getAddress());
            }
        }

        BluetoothSocket tmp = null;

        UUID myUUID = UUID.fromString(mUUID);
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = mmDevice.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) { }
        mmSocket = tmp;

        try {
            mmSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        mArduino = new BlinkAndAnalog(mmSocket);
        //mArduino.runArduinoProcess();
    }

    final public boolean isThreeDataSensor(){
        return false;
    }

    final public boolean isOneDataSensor(){
        return true;
    }

    private void initData(){
        data = new float[1];
    }

    public void setData(Context context/*, float x*/){
        if(!isOneDataSensor())
            return;
        data[0]=sensorValue*entryLevel;
    }

    private void setEntryLevel(){
        entryLevel = 1;
    }

    public String getData(){
        StringBuilder out = new StringBuilder();
        out .append(getSensor().getName()+"\r\n")
            .append(data[0]);
        if(isThreeDataSensor()){
            out .append(", " + data[1])
                .append(", " + data[2]);
        }
        out.append("\r\n\r\n");
        return out.toString();
    }

    public Sensor getSensor(){
        return mSensor;
    }

    public String getType() {
        return "TYPE_UNKNOWN";
    }

    public String getName() {
        return sensorName;
    }

    public SensAppUnit getUnit() {
        return SensAppUnit.MOL;
    }

    @Override
    public void setListened(boolean set){
        super.setListened(set);
        if(set){
           mArduino.runArduinoProcess();
        } else {
           mArduino.stopArduinoProcess();
        }

    }

    public int getDefaultRate() {
        return DEFAULT_RATE;
    }
}

class BlinkAndAnalog extends JArduino {
    final AnalogPin analogInPin = AnalogPin.A_0;

    public BlinkAndAnalog(BluetoothSocket socket) {
        super(JArduinoCom.AndroidBluetooth, new AndroidBluetoothConfiguration(socket));
    }

    @Override
    protected void setup() {
        // initialize the digital pin as an output.
        // Pin 13 has an LED connected on most Arduino boards:
        pinMode(DigitalPin.PIN_13, PinMode.OUTPUT);
    }

    @Override
    protected void loop() {
        // set the LED on
        digitalWrite(DigitalPin.PIN_13, DigitalState.HIGH);
        delay(1000); // wait for a second
        // set the LED off
        digitalWrite(DigitalPin.PIN_13, DigitalState.LOW);
        delay(1000); // wait for a second

        // get data from analog sensor on pin analogPin.
        int value = analogRead(analogInPin);
        BluetoothSensor.sensorValue = value;
        Log.d("coucou", String.valueOf(value));
    }
}
