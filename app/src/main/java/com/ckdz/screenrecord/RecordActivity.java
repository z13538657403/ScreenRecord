package com.ckdz.screenrecord;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.ckdz.screenrecord.record.RecordScreenService;

/**
 * Created by zhangtao on 18/1/10.
 */

public class RecordActivity extends Activity implements View.OnClickListener
{
    private static final int REQUEST_CODE = 1;
    private MediaProjectionManager mMediaProjectionManager;
    private Intent mRecordIntent;
    private MyRecordReceiver myRecordReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        setContentView(R.layout.record_activity);

        Button button1 = (Button) findViewById(R.id.my_start_service);
        button1.setOnClickListener(this);

        IntentFilter intentFilter = new IntentFilter("com.ckdz.ckdzsyspaint.RECORD_SCREEN");
        myRecordReceiver = new MyRecordReceiver();
        registerReceiver(myRecordReceiver , intentFilter);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.my_start_service:
                mRecordIntent = new Intent(RecordActivity.this , RecordScreenService.class);
                startService(mRecordIntent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        mRecordIntent = new Intent(RecordActivity.this , RecordScreenService.class);
        mRecordIntent.putExtra("record_code" , resultCode);
        mRecordIntent.putExtra("record_data" , data);
        startService(mRecordIntent);
    }

    class MyRecordReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE);
        }
    }

    @Override
    protected void onResume()
    {
        Log.d("RecordActivity" , "onResume");
        super.onResume();
    }

    @Override
    protected void onRestart()
    {
        Log.d("RecordActivity" , "onRestart");
        super.onRestart();
    }

    @Override
    protected void onPause()
    {
        Log.d("RecordActivity" , "onPause");
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        Log.d("RecordActivity" , "onNewIntent");
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop()
    {
        Log.d("RecordActivity" , "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(myRecordReceiver);
        if (mRecordIntent != null)
        {
            stopService(mRecordIntent);
        }
    }
}
