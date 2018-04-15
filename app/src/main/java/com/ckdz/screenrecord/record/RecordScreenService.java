package com.ckdz.screenrecord.record;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import java.io.File;
import java.io.IOException;

/**
 * Created by zhangtao on 18/1/10.
 */

public class RecordScreenService extends Service implements RecordFloatWindowManager.OnFloatItemClickListener
{
    private RecordFloatWindowManager mFloatViewManager;
    private MediaRecorder mMediaRecorder;
    private int mRequestCode;
    private Intent mResultData;
    private int width = 1920;
    private int height = 1080;
    private MediaProjection mMediaProjection;
    private boolean mIsRunning = false;
    private VirtualDisplay mVirtualDisplay;
    private boolean allowRecordVoice = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        mFloatViewManager = RecordFloatWindowManager.getInstance(this);
        mFloatViewManager.showFloatView();
        mFloatViewManager.setOnFloatItemClickListener(this);
        mMediaRecorder = new MediaRecorder();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mRequestCode = intent.getIntExtra("record_code" , -2);
        mResultData = intent.getParcelableExtra("record_data");
        if (mResultData != null && mRequestCode == Activity.RESULT_OK)
        {
            mMediaProjection = createMediaProjection();
        }
        startRecord();
        return Service.START_NOT_STICKY;
    }

    public boolean startRecord()
    {
        if (mMediaProjection == null || mIsRunning)
        {
            return false;
        }
        initRecorder();
        createVirtualDisplay();
        mMediaRecorder.start();
        mFloatViewManager.isStartClick(true);
        mIsRunning = true;
        return true;
    }

    private void initRecorder()
    {
        if (allowRecordVoice)
        {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setOutputFile(getSaveDirectory() + System.currentTimeMillis() + ".mp4");
        mMediaRecorder.setVideoSize(width, height);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        if (allowRecordVoice)
        {
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        }
        mMediaRecorder.setVideoEncodingBitRate(6000000);
        mMediaRecorder.setVideoFrameRate(30);
        try
        {
            mMediaRecorder.prepare();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void createVirtualDisplay()
    {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("MainScreen", width, height, 2,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC , mMediaRecorder.getSurface(), null, null);
    }

    private MediaProjection createMediaProjection()
    {
        return ((MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE)).
                getMediaProjection(Activity.RESULT_OK , mResultData);
    }

    @Override
    public void onStartClick()
    {
        if (mIsRunning)
        {
            stopRecord();
        }
        else
        {
            Intent intent = new Intent("com.ckdz.ckdzsyspaint.RECORD_SCREEN");
            sendBroadcast(intent);
        }
    }

    @Override
    public void onVoiceClick()
    {
        if (!mIsRunning)
        {
            if (allowRecordVoice)
            {
                allowRecordVoice = false;
                mFloatViewManager.isAllowVoice(false);
            }
            else
            {
                allowRecordVoice = true;
                mFloatViewManager.isAllowVoice(true);
            }
        }
    }

    public boolean stopRecord()
    {
        if (mIsRunning)
        {
            mIsRunning = false;
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mFloatViewManager.isStartClick(false);
            if (mVirtualDisplay != null)
            {
                mVirtualDisplay.release();
                mVirtualDisplay = null;
            }
            if (mMediaProjection != null)
            {
                mMediaProjection.stop();
                mMediaProjection = null;
            }
        }
        return true;
    }

    @Override
    public void onCloseClick()
    {
        stopSelf();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mIsRunning)
        {
            stopRecord();
            mFloatViewManager.isStartClick(false);
        }
        allowRecordVoice = true;
        mFloatViewManager.isAllowVoice(true);
        mFloatViewManager.removeFloatView();
    }

    public String getSaveDirectory()
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";
            File file = new File(rootDir);
            if (!file.exists())
            {
                if (!file.mkdirs())
                {
                    return null;
                }
            }
            return rootDir;
        }
        else
        {
            return null;
        }
    }
}
