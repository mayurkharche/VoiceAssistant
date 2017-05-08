package com.mrcoders.www.voiceassistant.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.mrcoders.www.voiceassistant.R;
import com.mrcoders.www.voiceassistant.functionality.BatteryStatus;
import com.mrcoders.www.voiceassistant.functionality.Bluetooth;
import com.mrcoders.www.voiceassistant.functionality.CallToContact;
import com.mrcoders.www.voiceassistant.functionality.LaunchApp;
import com.mrcoders.www.voiceassistant.functionality.WeatherStatus;
import com.mrcoders.www.voiceassistant.functionality.Wifi;
import com.mrcoders.www.voiceassistant.global.Constant;
import com.mrcoders.www.voiceassistant.service.TalkService;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import github.vatsal.easyweather.WeatherMap;

public class TalkActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback
                            ,AIListener,TextToSpeech.OnInitListener{

    public static final String TAG = TalkActivity.class.getSimpleName();

    private static final int REQUEST_RECORD_AUDIO = 1;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private String[] mColumnProjection = new String[]{
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };

    private View mLayout;

    private TextView txtSpeechInput;
    private TextView txtSpeechOutput;
    private TextToSpeech tts;
    private AIService aiService;
    private Intent serviceIntent;
    private HashMap<String,String> ttsParams;
    private AudioManager audioManager;
    private int currentVolume;
    private WeatherMap weatherMap;
    String namee = "Rohit Gurjar";

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
        txtSpeechOutput = (TextView) findViewById(R.id.tv_assistant_response);

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

        weatherMap =  new WeatherMap(this, "efa265f590cc47001d75c2eda991a174");

        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.content);
        rippleBackground.startRippleAnimation();
    }

    public void getAllContacts(String namee) {

        ContentResolver cr = getContentResolver();
        String mSelectionClause = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + "  = ?";
        String[] mSelectionArguments = new String[]{namee};

        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> phone = new ArrayList<String>();
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,mSelectionClause,mSelectionArguments, null);

        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            names.add(name);
            phone.add(phoneNumber);
            break;
        }
        phones.close();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phone.get(0)));
        if (ActivityCompat.checkSelfPermission(TalkActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        startActivity(callIntent);

        Toast.makeText(this,phone.get(0).toString(),Toast.LENGTH_LONG).show();
    }

    public String getphone(String namee){

        ContentResolver cr = getContentResolver();

        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> phone = new ArrayList<String>();
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);

        phones.moveToFirst();

        do
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            names.add(name);
            phone.add(phoneNumber);

        }while (phones.moveToNext());

        int j = -1;
        String ph="";
        for(int i = 0 ;i < names.size() ; i++){

            if(namee.toLowerCase().equals(names.get(i).toLowerCase())){

                  ph = phone.get(i);
                  break;
            }
        }

        return ph;
    }

    public void makeACall(String number){

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+number));
        if (ActivityCompat.checkSelfPermission(TalkActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        startActivity(callIntent);
        finish();
    }

    public ArrayList<String> getNameEmailDetails(){

        ArrayList<String> names = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        String mSelectionClause = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + "  = ?";
        String[] mSelectionArguments = new String[]{"Rohit Gurjar"};
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null,mSelectionClause, mSelectionArguments,null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
             //   String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                //  String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            //    String email = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
             //
                String phone=null;

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //Query phone here.  Covered next
                    phone = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                names.add(phone);
            }
            return names;
        }
        else
            return null;
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

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
               // showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
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
        txtSpeechInput.setText(result.getResolvedQuery());
        txtSpeechOutput.setText(toSpeak);

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
                Log.d(TAG,result.getParameters().get("Boolean").getAsString());
                Wifi wifi = new Wifi(this);
                if(result.getParameters().get("Boolean").getAsString().equals("on")){
                    Log.d(TAG,"Wi-Fi turned on");
                    wifi.change_wifi(true);
                    speakOut("Wi-Fi turned on");
                }else if(result.getParameters().get("Boolean").getAsString().equals("off")){
                    Log.d(TAG,"Wi-Fi turn off");
                    wifi.change_wifi(false);
                    speakOut("Wi-Fi turned off");
                }
            }

        }else if(action.equals("battery")){

            BatteryStatus bs = new BatteryStatus(this);
            speakOut("Your battery status is "+bs.batteryLevel()+" percent");

        }else if(action.equals("launch")){

            String name = result.getParameters().get("app-name").getAsString();
            LaunchApp la = new LaunchApp(this);
            if(la.launchApp(name)){
                finish();
            }else{
                speakOut("Sorry, cann't find "+name+" in your mobile");
            }
        }else if(action.equals("weather")){

            Log.d("weather","weather called");
            String name = result.getParameters().get("city-name").getAsString();
            WeatherStatus w = new WeatherStatus(weatherMap, this);
            w.weatherStatus(name);

        }else if(action.equals("textservice")){

            Log.d("textservice","Text Service Called");

            String name="",op="",content="";
            try {
                name = result.getParameters().get("name").getAsString();
                op = result.getParameters().get("text_service").getAsString();
                content = result.getParameters().get("content").getAsString();
            }catch(Exception e){

            }

            Log.d("textService","option "+op+ "name" +name+ "content" + content);

            if(name.equals("") || op.equals("") || content.equals("")){

                speakOut(toSpeak);

            }else{

                if(op.equals("Email")){

                    Log.d("textService","In Email");
                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"mayurkharche1994@gmail.com"});
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "An email through voice assistance");
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));

                }else{

                    Log.d("textService","In Message");

                    try {
                        sendSMS(getphone(name), content);
                    }catch(Exception e){
                        speakOut("Something is going wrong,Can not send the message");
                    }
                }
            }

        }else if(action.equals("call")){

            Log.d("call","Calling called");
            String name = result.getParameters().get("name").getAsString();

            makeACall(getphone(name));

        }else if(action.equals("smalltalk.greetings.bye")){

            finish();

        }else if(action.equals("search")){

            Log.d("search","search called");
            String keyword = result.getParameters().get("any").getAsString();
            searchKeyword(keyword);

        }else{
            speakOut(toSpeak);
        }
    }

    private void searchKeyword(String keyword){

        Uri uri = Uri.parse("http://www.google.com/#q="+keyword);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
        finish();
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public void speakOut(final String text) {


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

    public void updateResponse(String textToUpdate){

        txtSpeechOutput.setText(textToUpdate);
    }
}
