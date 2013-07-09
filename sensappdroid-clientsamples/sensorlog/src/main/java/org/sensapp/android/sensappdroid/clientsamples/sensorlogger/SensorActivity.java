package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
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

    final static int GREY=0xFFCCDDFF;
    static String compositeName = Build.MODEL + Build.ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorLoggerTask.initSensorManager(getApplicationContext());

        //Debug.startMethodTracing("SensorActivity");
        setContentView(R.layout.activity_main);
        TextView title = (TextView)findViewById(R.id.app_title);
        title.setText(R.string.app_title);

        SensorLoggerTask.initSensorArray();

        compositeName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.pref_compositename_key), SensorActivity.compositeName);

        final LinearLayout l = (LinearLayout) findViewById(R.id.general_view);

        SensorLoggerTask.setUpSensors(getApplicationContext(), (SensorManager) getSystemService(SENSOR_SERVICE));
        for(AbstractSensor s: SensorLoggerTask.sensors)
            addAbstractSensor(s, l);
    }

    private void addAbstractSensor(AbstractSensor as, LinearLayout l){
        final Button b = new Button(this);
        final ImageView image = new ImageView(this);
        LinearLayout line = new LinearLayout(this);
        LinearLayout forImage = new LinearLayout(this);

        initButton(b, as, image);
        initImage(image, l, as);
        initMainAppView(l, line, forImage, image, b, as);
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

    private void initMainAppView(LinearLayout l, LinearLayout line, LinearLayout img, final ImageView image, final Button b, final AbstractSensor as){
        img.setPadding(65, 10, 55, 0);
        img.addView(image);
        line.setPadding(0,11,50,0);
        line.addView(img);
        line.addView(b);
        line.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                OnClick(b, as, image);
            }
        });
        l.addView(line);
        LinearLayout separator = new LinearLayout(this);
        separator.setMinimumHeight(1);
        separator.setMinimumWidth(l.getWidth());
        separator.setBackgroundColor(GREY);
        l.addView(separator);
    }

    private void initButton(final Button b, final AbstractSensor as, final ImageView image){
        if(as.isListened())
            b.setText("Stop logging " + as.getName());
        else
            b.setText("Start logging " + as.getName());
        b.setBackgroundColor(Color.BLACK);
        b.setMinimumHeight(50);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.LEFT);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnClick(b, as, image);
            }
        });
    }

    private void initImage(final ImageView img, LinearLayout l, AbstractSensor as){
        if(as.isListened())
            img.setImageResource(R.drawable.button_round_green);
        else
            img.setImageResource(R.drawable.button_round_red);
        img.setMinimumWidth(l.getWidth());
        img.setScaleType(ImageView.ScaleType.FIT_START);
    }

    private void OnClick(Button b, AbstractSensor as, ImageView image){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!preferences.getBoolean(SERVICE_RUNNING, false)) {
            // If service is not already running.
            // Check if SensApp is installed.
            if (!SensAppHelper.isSensAppInstalled(getApplicationContext())) {
                // If not suggest to install and return.
                SensAppHelper.getInstallationDialog(SensorActivity.this).show();
                return;
            }
        }

        if (as.isListened()) {
            as.setListened(false);
            b.setText("Start logging " + as.getName());
            image.setImageResource(R.drawable.button_round_red);
            SensorManagerService.cancelLog(getApplicationContext(), as);
        } else {
            as.setListened(true);
            b.setText("Stop logging " + as.getName());
            image.setImageResource(R.drawable.button_round_green);
            SensorManagerService.setLog(getApplicationContext(), as);
        }
        preferences.edit().putBoolean(as.getFullName(), as.isListened()).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Debug.stopMethodTracing();
    }
}

