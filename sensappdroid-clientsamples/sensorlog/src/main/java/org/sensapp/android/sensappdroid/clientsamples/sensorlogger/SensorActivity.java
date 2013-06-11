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
package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This is the UI used to run or stop the sensor logging into SensApp.
 */
public class SensorActivity extends Activity{

    protected static final String SERVICE_RUNNING = "pref_service_is_running";
    private static final String TAG = SensorActivity.class.getSimpleName();

    private Button buttonService;
    private TextView tvStatus;

    final static int GREEN=0xFF4EFD4E;
    final static int RED=0xFFFF5E4C;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonService = (Button) findViewById(R.id.b_status);
        tvStatus = (TextView) findViewById(R.id.tv_status);

        SensorLoggerService.initSensorArray();

        SensorManager mManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        for(Sensor s: mManager.getSensorList(Sensor.TYPE_ALL)){
            final AndroidSensor as = new AndroidSensor(s, "Android_Tab");
            SensorLoggerService.addSensor(as);
            final Button b = new Button(this);
            b.setText("Start logging " + as.getName());
            b.setBackgroundColor(GREEN);
            final LinearLayout l = (LinearLayout) findViewById(R.id.general_view);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(as.isListened()){
                        //stop listening and change button text
                        as.setListened(false);
                        b.setText("Start logging " + as.getName());
                        b.setBackgroundColor(GREEN);
                        //l.refreshDrawableState();
                    }
                    else{
                        //begin listening and change button text
                        as.setListened(true);
                        b.setText("Stop logging " + as.getName());
                        b.setBackgroundColor(RED);
                        //l.refreshDrawableState();
                    }
                }
            });

            l.addView(b);
        }



        updateLabels();

        buttonService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Get the shared preferences.
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (!preferences.getBoolean(SERVICE_RUNNING, false)) {
                    // If service is not running.

                    // Check if SensApp is installed.
                    if (!SensAppHelper.isSensAppInstalled(getApplicationContext())) {
                        // If not suggest to install and return.
                        SensAppHelper.getInstallationDialog(SensorActivity.this).show();
                        return;
                    }
                    // Update the preference. Service is now running.
                    preferences.edit().putBoolean(SERVICE_RUNNING, true).commit();
                    // Schedule a repeating alarm to start the service, which stops itself.
                    AlarmHelper.setAlarm(getApplicationContext());
                } else {
                    // Service is running so it must stop.
                    // Update the preference.
                    preferences.edit().putBoolean(SERVICE_RUNNING, false).commit();
                    // Request for disable, cancel the alarm.
                    AlarmHelper.cancelAlarm(getApplicationContext());
                }
                // Update button and text view.
                updateLabels();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences:
                startActivity(new Intent(getApplicationContext(), Preferences.class));
                return true;
        }
        return false;
    }

    private void updateLabels() {
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(SERVICE_RUNNING, false)) {
            buttonService.setText(R.string.button_service_stop);
            tvStatus.setText(R.string.tv_status_running);
        } else {
            buttonService.setText(R.string.button_service_start);
            tvStatus.setText(R.string.tv_status_stoped);
        }
    }
}

