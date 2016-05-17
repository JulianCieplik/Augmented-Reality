package org.opencv.samples.tutorial1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void useTestData(View view){
        Intent intent = new Intent(this,TestData.class);
        startActivity(intent);
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
