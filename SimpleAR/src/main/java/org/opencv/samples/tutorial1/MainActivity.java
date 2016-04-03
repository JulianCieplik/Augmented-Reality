package org.opencv.samples.tutorial1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this,Tutorial1Activity.class);
        startActivity(intent);
    }
    public void config(View view) {
        Intent intent = new Intent(this,ConfigActivity.class);
        startActivity(intent);
    }
    public void AboutActivity(View view) {
        Intent intent = new Intent(this,AboutActivity.class);
        startActivity(intent);
    }
}
