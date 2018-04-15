package com.ckdz.screenrecord.record;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import com.ckdz.screenrecord.R;

/**
 * Created by zhangtao on 18/1/10.
 */

public class RecordFloatWindowManager
{
    private int mTouchSlop;
    private static RecordFloatWindowManager mInstance;
    private WindowManager.LayoutParams mFloatLp;
    private WindowManager mWindowManager;
    private View mFloatView;
    private Chronometer mTimer;
    private ImageView mStartRecordBtn;
    private ImageView mCloseRecordBtn;
    private ImageView mVoiceControlBtn;
    private OnFloatItemClickListener mFloatItemClickListener;

    public void setOnFloatItemClickListener(OnFloatItemClickListener onFloatItemClickListener)
    {
        this.mFloatItemClickListener = onFloatItemClickListener;
    }

    private RecordFloatWindowManager(Context context)
    {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() / 2;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mFloatView = LayoutInflater.from(context).inflate(R.layout.record_control_layout , null);
        mStartRecordBtn = (ImageView) mFloatView.findViewById(R.id.start_record_btn);
        mVoiceControlBtn = (ImageView) mFloatView.findViewById(R.id.record_voice_btn);
        mCloseRecordBtn = (ImageView) mFloatView.findViewById(R.id.close_record_btn);
        mTimer = (Chronometer) mFloatView.findViewById(R.id.timer);
        mFloatView.setOnTouchListener(mFloatViewOnTouchListener);
        mStartRecordBtn.setOnClickListener(mFloatNumberOnClickListener);
        mVoiceControlBtn.setOnClickListener(mFloatNumberOnClickListener);
        mCloseRecordBtn.setOnClickListener(mFloatNumberOnClickListener);
    }

    public static RecordFloatWindowManager getInstance(Context context)
    {
        if (mInstance == null)
        {
            synchronized (RecordFloatWindowManager.class)
            {
                if (mInstance == null)
                {
                    mInstance = new RecordFloatWindowManager(context);
                }
            }
        }
        return mInstance;
    }

    public void showFloatView()
    {
        if (mFloatLp == null)
        {
            mFloatLp = new WindowManager.LayoutParams();
            mFloatLp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mFloatLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mFloatLp.gravity = Gravity.CENTER;
            mFloatLp.type = WindowManager.LayoutParams.TYPE_PHONE;
            mFloatLp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mFloatLp.format = PixelFormat.RGBA_8888;
        }

        mWindowManager.addView(mFloatView , mFloatLp);
    }

    public void removeFloatView()
    {
        if (mFloatView != null && mFloatView.isAttachedToWindow())
        {
            mWindowManager.removeView(mFloatView);
        }
    }

    private View.OnTouchListener mFloatViewOnTouchListener = new View.OnTouchListener()
    {
        float startX;
        float startY;
        float downX;

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();
                    downX = startX;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX();
                    float y = event.getRawY();
                    float dx = x - startX;
                    float dy = y - startY;
                    if (Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop)
                    {
                        mFloatLp.x += dx;
                        mFloatLp.y += dy;
                        mWindowManager.updateViewLayout(mFloatView , mFloatLp);
                    }
                    startX = x;
                    startY  = y;
                    break;
                case MotionEvent.ACTION_UP:
                    float endX = event.getRawX();
                    return Math.abs(endX - downX) > 6;
                default:
                    break;
            }
            return false;
        }
    };

    private View.OnClickListener mFloatNumberOnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.start_record_btn:
                    if (mFloatItemClickListener != null)
                    {
                        mFloatItemClickListener.onStartClick();
                    }
                    break;
                case R.id.close_record_btn:
                    if (mFloatItemClickListener != null)
                    {
                        mFloatItemClickListener.onCloseClick();
                    }
                    break;
                case R.id.record_voice_btn:
                    if (mFloatItemClickListener != null)
                    {
                        mFloatItemClickListener.onVoiceClick();
                    }
                    break;
            }
        }
    };

    public void isStartClick(boolean isRecording)
    {
        if (isRecording)
        {
            mStartRecordBtn.setImageResource(R.mipmap.puase_record_icon);
            mTimer.setBase(SystemClock.elapsedRealtime());//计时器清零
            int hour = (int) ((SystemClock.elapsedRealtime() - mTimer.getBase()) / 1000 / 60);
            mTimer.setFormat("0"+String.valueOf(hour)+":%s");
            mTimer.start();
        }
        else
        {
            mStartRecordBtn.setImageResource(R.mipmap.start_record_icon);
            mTimer.stop();
        }
    }

    public void isAllowVoice(boolean allowRecordVoice)
    {
        if (allowRecordVoice)
        {
            mVoiceControlBtn.setImageResource(R.mipmap.start_record_icon);
        }
        else
        {
            mVoiceControlBtn.setImageResource(R.mipmap.puase_record_icon);
        }
    }

    public interface OnFloatItemClickListener
    {
        void onStartClick();
        void onVoiceClick();
        void onCloseClick();
    }
}
