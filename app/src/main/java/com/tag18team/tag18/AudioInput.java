package com.tag18team.tag18;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class AudioInput extends Activity {
    private int RESULT_SPEECH_TO_TEXT;
    final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO=10;
    public AudioInput() {}
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0)
        {
            Toast.makeText(this, "problem",Toast.LENGTH_SHORT);
        }
    }
    public void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        startActivityForResult(intent, MY_PERMISSIONS_REQUEST_RECORD_AUDIO );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList<String> matches = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);
        Toast.makeText(getApplicationContext(),matches.toArray().toString(), Toast.LENGTH_SHORT).show();
        super.onActivityResult(requestCode, resultCode, data);
    }
}
