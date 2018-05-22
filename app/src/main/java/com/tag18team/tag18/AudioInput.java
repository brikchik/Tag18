package com.tag18team.tag18;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class AudioInput extends Activity {
    private int RESULT_SPEECH_TO_TEXT;

    public AudioInput(){onCreate();}
    public void onCreate(){
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak please");
        //startActivity(speechIntent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_SPEECH_TO_TEXT && resultCode == RESULT_OK) {
            List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Toast.makeText(getApplicationContext(),matches.toArray().toString(), Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
