package com.mrcoders.www.voiceassistant.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.mrcoders.www.voiceassistant.R;
import com.mrcoders.www.voiceassistant.functionality.BatteryStatus;
import com.mrcoders.www.voiceassistant.functionality.Bluetooth;
import com.mrcoders.www.voiceassistant.functionality.Wifi;
import com.mrcoders.www.voiceassistant.global.Constant;
import com.mrcoders.www.voiceassistant.service.TalkService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class TalkActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback
                            ,AIListener,TextToSpeech.OnInitListener{

    public static final String TAG = TalkActivity.class.getSimpleName();

    private static final int REQUEST_RECORD_AUDIO = 1;

    private View mLayout;

    private TextView txtSpeechInput;
    private TextToSpeech tts;
    private AIService aiService;
    private Intent serviceIntent;
    private HashMap<String,String> ttsParams;
    private AudioManager audioManager;
    private int currentVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        mLayout = findViewById(R.id.main_layout);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        tts = new TextToSpeech(this, this);
        txtSpeechInput = (TextView) findViewById(R.id.tv_user_command);

        final AIConfiguration config = new AIConfiguration(Constant.clientAccessToken,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Record Audio permission has not been granted.

            requestRecordAudioPermission();

        } else {

            // Record audio permissions is already available, show the camera preview.
            aiService.startListening();
            Log.i(TAG, "RECORD AUDIO permission has already been granted.");
        }

        serviceIntent = new Intent(TalkActivity.this, TalkService.class);
        ttsParams = new HashMap<String, String>();

        try{
            stopService(serviceIntent);
        }catch(Exception e){
            Log.d(TAG,"Service is not running");
        }

        BatteryStatus bs = new BatteryStatus(this);
        Toast.makeText(this, ""+bs.batteryLevel(), Toast.LENGTH_SHORT).show();
    }

    private void requestRecordAudioPermission() {
        Log.i(TAG, "RECORD AUDIO permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(record_audio_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG, "Record Audio permission is required");
            Snackbar.make(mLayout, R.string.permission_recordaudio_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(TalkActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO},
                                    REQUEST_RECORD_AUDIO);
                        }
                    })
                    .show();
        } else {

            // Record Audio permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
        // END_INCLUDE(record_audio_permission_request)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_RECORD_AUDIO && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            aiService.startListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        Log.d(TAG,"onDestroy");
        Log.d(TAG,"Starting PocketSphinx service");
        startService(serviceIntent);
        Log.d(TAG,"Restoring the media volume");
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    @Override
    public void onResult(AIResponse response) {

        Log.d(TAG,"onResult Stoping Service");
        aiService.stopListening();

        Log.d(TAG,"onResult");
        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        String toSpeak = result.getFulfillment().getSpeech();
        String action = result.getAction();

        // Show results in TextView.
        txtSpeechInput.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + toSpeak);

        if(action.equals("service")){
            Log.d(TAG,result.getParameters().get("Service").getAsString());
            if(result.getParameters().get("Service").getAsString().equals("Bluetooth")){
                Log.d(TAG,result.getParameters().get("Boolean").getAsString());
                Bluetooth b = new Bluetooth();
                if(result.getParameters().get("Boolean").getAsString().equals("on")){
                    Log.d(TAG,"Bluetooth turned on");
                    b.setBluetooth(true);
                    speakOut("Bluetooth turned on");
                }else if(result.getParameters().get("Boolean").getAsString().equals("off")){
                    Log.d(TAG,"Bluetooth turn off");
                    b.setBluetooth(false);
                    speakOut("Bluetooth turned off");
                }
            }else if(result.getParameters().get("Service").getAsString().equals("Wi-Fi")){
                /*Log.d(TAG,result.getParameters().get("Boolean").getAsString());
                Wifi wifi = new Wifi(this);
                if(result.getParameters().get("Boolean").getAsString().equals("on")){
                    Log.d(TAG,"Wi-Fi turned on");
                    wifi.change_wifi(true);
                    speakOut("Wi-Fi turned on");
                }else if(result.getParameters().get("Boolean").getAsString().equals("off")){
                    Log.d(TAG,"Wi-Fi turn off");
                    wifi.change_wifi(false);
                    speakOut("Wi-Fi turned off");
                }*/
            }

        }else if(action.equals("battery")){

            BatteryStatus bs = new BatteryStatus(this);
            speakOut("Your battery status is "+bs.batteryLevel());

        }else{
            speakOut(toSpeak);
        }
    }

    private void speakOut(final String text) {


        //aiService.stopListening();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        Log.d(TAG,"speakOut");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, ttsParams);

    }

    @Override
    public void onError(AIError error) {

        Log.d(TAG,"onError"+error.hashCode());
        txtSpeechInput.setText(error.toString());

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                Log.d(TAG,"onError reseting service");
                aiService.stopListening();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Log.d(TAG,"onError restart listening");
                aiService.startListening();
            }
        }.execute();

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

        Log.d(TAG,"onListeningStarted");
    }

    @Override
    public void onListeningCanceled() {

        Log.d(TAG,"onListeningCanceled");
    }

    @Override
    public void onListeningFinished() {

        Log.d(TAG,"onListeningFinished");
    }

    @Override
    public void onInit(int status) {

        tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {

            @Override
            public void onUtteranceCompleted(final String utteranceId) {
                System.out.println("Completed");

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //UI changes
                        Log.d(TAG,"Resetting volume to zero");
                        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        aiService.startListening();
                    }
                });
            }
        });

        ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                TalkActivity.this.getPackageName());


        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG+"TTS", "This Language is not supported");
            } else {

            }

        } else {
            Log.e(TAG+"TTS", "Initilization Failed!");
        }

    }
}
