package com.mrcoders.www.voiceassistant.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.mrcoders.www.voiceassistant.R;

public class TalkActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    public static final String TAG = TalkActivity.class.getSimpleName();

    private static final int REQUEST_RECORD_AUDIO = 1;

    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        mLayout = findViewById(R.id.main_layout);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Record Audio permission has not been granted.

            requestRecordAudioPermission();

        } else {

            // Record audio permissions is already available, show the camera preview.
            Log.i(TAG, "RECORD AUDIO permission has already been granted.");
        }
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

        }
    }
}
