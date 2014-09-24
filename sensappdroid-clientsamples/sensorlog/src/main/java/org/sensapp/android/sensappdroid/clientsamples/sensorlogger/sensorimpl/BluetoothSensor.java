/**
 * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.Sensor;
import org.sensapp.android.sensappdroid.api.SensAppUnit;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.SensorActivity;
import org.sintef.jarduino.*;

import java.io.IOException;
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
    private String deviceName = null;
    private BluetoothSocket mmSocket;
    static JArduino mArduino = null;
    static int sensorValue = 2;
    private String mUUID = "00001101-0000-1000-8000-00805F9B34FB";

    public BluetoothSensor(String composite, String sensorName, BluetoothAdapter bluetoothDevice, String deviceName) {
        setEntryLevel();
        initData();
        this.sensorName = sensorName;
        this.deviceName = deviceName;
        mBluetoothAdapter = bluetoothDevice;
        mComposite = composite;
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

    public void setData(Context context){
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

    void connectAndExecute(){
        // The class is allocated 2 times, but we need only one connection and one device.
        if(mArduino == null){
            BluetoothDevice mmDevice = null;

            //Retrieve the right paired bluetooth device.
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if(device.getName().equals(deviceName))
                        mmDevice = device;
                }
            }

            //Creating the socket.
            BluetoothSocket tmp = null;

            if(mmDevice == null){
                SensorActivity.ME.showError("Bluetooth issue!", "Make sure you have correctly set the bluetooth device name. Make also sure you have paired this device with your Android platform.");
                this.setListened(false);
                return;
            }

            if(mmSocket == null || !mmSocket.isConnected()){
                UUID myUUID = UUID.fromString(mUUID);
                try {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(myUUID);
                } catch (IOException e) { }
                mmSocket = tmp;

                try {
                    mmSocket.connect();
                } catch (IOException e) {
                    SensorActivity.ME.showError("Bluetooth issue!", "Connection attempt failed.");
                    this.setListened(false);
                }

                // instantiate and execute the JArduino program
                mArduino = new BlinkAndAnalog(mmSocket);
                mArduino.runArduinoProcess();
            }
        }
    }

    @Override
    public void setListened(boolean set){
        super.setListened(set);
        if(set){
            connectAndExecute();
        } else {
            if(mArduino != null){
                mArduino.stopArduinoProcess();
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                mArduino = null;
            }
        }
    }

    public int getDefaultRate() {
        return DEFAULT_RATE;
    }
}

// JArduino Program.
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
    }
}
