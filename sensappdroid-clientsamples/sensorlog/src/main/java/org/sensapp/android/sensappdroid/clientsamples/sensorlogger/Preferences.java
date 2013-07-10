package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.method.DigitsKeyListener;

import java.util.List;

/**
 * There is the UI permitting to edit preferences regarding sensors.
 */
public class Preferences extends PreferenceActivity {

	public static class PreferencesFragment extends PreferenceFragment {
        @Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
            List<AbstractSensor> sensors = SensorLoggerTask.getSensors();

            final String compositeNameKey = getString(R.string.pref_compositename_key);
            final EditTextPreference compositeName = (EditTextPreference) findPreference(compositeNameKey);
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            final SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

            //Add Composite Name EditText to the list of preferences
            compositeName.setDefaultValue(sp.getString(compositeNameKey, SensorActivity.compositeName));
            compositeName.setSummary(sp.getString(compositeNameKey, SensorActivity.compositeName));
            compositeName.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String lastName = sp.getString(compositeNameKey, SensorActivity.compositeName);

                    String name = (String)newValue;
                    prefEditor.putString(compositeNameKey, name).commit();
                    compositeName.setSummary(name);

                    if(!name.equals(lastName)){
                        SensorActivity.compositeName = name;
                        if(SensorLoggerTask.sensors != null){
                            for(AbstractSensor s : SensorLoggerTask.sensors)
                                s.setCompositeName(name);
                        }
                    }

                    return true;
                }
            });

            //Add the sensors refresh rate into 'preferences'
            for(AbstractSensor s: sensors){
                EditTextPreference sNew = new EditTextPreference(getActivity());
                sNew.getEditText().setKeyListener(DigitsKeyListener.getInstance());
                sNew.setTitle(s.getName());
                sNew.setDialogTitle("Enter the refresh rate you wish for this sensor.");
                sNew.setDefaultValue(((Integer) s.getMeasureTime()).toString());
                sNew.setSummary(((Integer) s.getMeasureTime()).toString());

                getPreferenceScreen().addItemFromInflater(sNew);

                //set the function called when preferences changed for this preference/sensor
                sNew.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (((String) newValue).isEmpty()) {
                            return false;
                        }
                        AbstractSensor toChange = SensorLoggerTask.getSensorByName((String) preference.getTitle());
                        toChange.setRefreshRate(Integer.parseInt((String) newValue));

                        if(toChange.isListened()){

                            SensorManagerService.cancelLog(getActivity().getApplicationContext(), toChange);
                            SensorManagerService.setLog(getActivity().getApplicationContext(), toChange);
                        }
                        if(toChange instanceof AndroidSensor)
                            SensorActivity.refreshConsumption(toChange, getActivity().getApplicationContext());

                        prefEditor.putInt(toChange.getName(), Integer.parseInt((String) newValue));
                        prefEditor.commit();
                        preference.setSummary(((Integer) Integer.parseInt((String) newValue)).toString());
                        return true;
                    }
                });
            }
		}

        public void onDestroy(){
            super.onDestroy();
            getPreferenceScreen().removeAll();
        }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
	}
}
