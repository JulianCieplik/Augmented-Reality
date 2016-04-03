package org.opencv.samples.tutorial1;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        SharedPreferences mPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        int i = Integer.valueOf(mPrefs.getString("d1", "4"));
        int j = Integer.valueOf(mPrefs.getString("d2","11"));
        int k = Integer.valueOf(mPrefs.getString("sync_frequency","1"));
        setContentView(R.layout.activity_about);
        TextView mytextview = (TextView) findViewById(R.id.display1);
        mytextview.setText("value"+i);
    }
}
