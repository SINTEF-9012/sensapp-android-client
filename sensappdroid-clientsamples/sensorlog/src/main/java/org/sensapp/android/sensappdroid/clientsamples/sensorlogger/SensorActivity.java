package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.benchmark.BenchmarkTask;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorlog.AbstractSensorLoggerTask;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AbstractSensor;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AndroidSensor;

import java.util.Hashtable;
import java.util.Map;

/**
 * This is the UI used to run or stop the sensor logging into SensApp.
 */
public class SensorActivity extends Activity{

    static public final String SERVICE_RUNNING = "pref_service_is_running";
    static private boolean BENCH_MARKED;
    static final int GREY=0xFFCCDDFF;
    static public String compositeName = Build.MODEL + Build.ID;
    static final Map<AbstractSensor, TextView> consumptionTv = new Hashtable<AbstractSensor, TextView>();
    static private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        BENCH_MARKED = sp.getBoolean(getString(R.string.benchmarked), false);
        if(!BENCH_MARKED){
            doBenchmark();
            sp.edit().putBoolean(getString(R.string.benchmarked), true).commit();
        }

        AbstractSensorLoggerTask.initSensorManager(getApplicationContext());

        setContentView(R.layout.activity_main);
        TextView title = (TextView)findViewById(R.id.app_title);
        title.setText(R.string.app_title);

        AbstractSensorLoggerTask.initSensorArray();

        compositeName = sp.getString(getString(R.string.pref_compositename_key), SensorActivity.compositeName);

        final LinearLayout l = (LinearLayout) findViewById(R.id.general_view);

        AbstractSensorLoggerTask.setUpSensors(getApplicationContext(), (SensorManager) getSystemService(SENSOR_SERVICE));
        for(AbstractSensor s: AbstractSensorLoggerTask.sensors)
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
            case R.id.exit:
                exitActivity();
                return true;
        }
        return false;
    }

    private void initMainAppView(LinearLayout l, final LinearLayout line, LinearLayout img, final ImageView image, final Button b, final AbstractSensor as){
        img.setPadding(65, 10, 55, 0);
        img.addView(image);
        line.setPadding(0, 11, 50, 0);
        line.addView(img);
        line.addView(b);
        line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myOnClick(b, as, image);
            }
        });

        TextView tv = new TextView(getApplicationContext());
        writeText(tv, computeCostPerHour(as), getApplicationContext());
        line.addView(tv);
        consumptionTv.put(as, tv);

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
                myOnClick(b, as, image);
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

    private void myOnClick(Button b, AbstractSensor as, ImageView image){
        if (!sp.getBoolean(SERVICE_RUNNING, false)) {
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
        sp.edit().putBoolean(as.getFullName(), as.isListened()).commit();
    }

    private void exitActivity(){
        for(AbstractSensor as : AbstractSensorLoggerTask.sensors)
            SensorManagerService.cancelLog(getApplicationContext(), as);
        getApplicationContext().stopService(SensorManagerService.getIntent());
        this.finish();
    }

    private void doBenchmark(){
        BenchmarkTask.initSensorManager(getApplicationContext());
        BenchmarkTask.initSensorArray();
        BenchmarkTask.setUpSensors(getApplicationContext(), (SensorManager) getSystemService(SENSOR_SERVICE));
    }

    static public void refreshConsumption(AbstractSensor as, Context c){
        writeText(consumptionTv.get(as), computeCostPerHour(as), c);
    }

    static private void writeText(TextView tv, double value, Context c){
        if(Double.valueOf(value).isNaN())
            tv.setText("n/a");
        else{
            int batteryPower = Integer.parseInt(sp.getString(c.getString(R.string.pref_battery_key), "0"));
            if(batteryPower == 0)
                tv.setText(String.format("%.2f mAh", value));
            else
                tv.setText(String.format("%.2f mAh (%.2f%%)", value, (value/batteryPower*100.0)));
        }
    }

    static private double computeCostPerHour(AbstractSensor as){
        if(!(as instanceof AndroidSensor))
            return Float.NaN;

        int hour = 60000;
        double nbTickHour = (double)hour/(as.getMeasureTime()/1000.0);


        if(Double.valueOf(((AndroidSensor) as).getBenchmarkAvg()).isNaN()){
            ((AndroidSensor) as).setBenchmarkAvg(sp.getFloat(as.getName()+"_avg", Float.NaN));
        }
        double avg = ((AndroidSensor) as).getBenchmarkAvg()/1000.0;

        return nbTickHour*avg*as.getSensor().getPower();
    }
}

