package com.test.myrecord;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OutputFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;


public class MainActivity extends Activity implements  OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = "MainActivity";
    private ImageView start;
    private ImageView img_resume_pause;
    private SurfaceView mSurfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean isRecording = false;
    private MediaRecorder mediarecorder;
    private Camera camera;
    private Camera.Parameters params;
    private File file;
    private int BitRate = 5;
    private int displayOrientation = 90;
    private long time = 0 - 1;
    private TextView time_tv;
    private LinearLayout ll_time;
    private Handler handler;
    private String vid_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        makeDirs();
        initView();
    }

    private void initView() {
        start = (ImageView) this.findViewById(R.id.start_record);
        start.setOnClickListener(this);

        time_tv = (TextView) findViewById(R.id.time);
        ll_time = (LinearLayout) findViewById(R.id.ll_time);
        handler = new Handler();

        mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceview);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        params.width = screenW;
        params.height = screenW * 4 / 3;
        params.gravity = Gravity.TOP;
        mSurfaceView.setLayoutParams(params);

//        mSurfaceView.setOnTouchListener(this);
        img_resume_pause = (ImageView) findViewById(R.id.img_resume_pause);
        img_resume_pause.setOnClickListener(this);

        surfaceHolder = mSurfaceView.getHolder();// 取得holder
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        mSurfaceView.setFocusable(true);
        surfaceHolder.addCallback(this); // holder加入回调接口


    }


    private Runnable timeRun = new Runnable() {
        @Override
        public void run() {

            time++;
            ll_time.setVisibility(View.VISIBLE);

            time_tv.setText(timeFormat((int) time));
            handler.postDelayed(timeRun, 1000);

        }
    };


    /**
     * 时间格式化
     *
     * @param timeMs
     * @return
     */
    public String timeFormat(int timeMs) {
        int totalSeconds = timeMs;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.getDefault());
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    private String getModifyTime() {
//        long l = file.lastModified();
        long l = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date(l);
        String s = format.format(date);
        return s;
    }




    /**
     * 开始录制
     */

    String currentVideoFilePath;

    protected void start() {
        img_resume_pause.setVisibility(View.VISIBLE);
        img_resume_pause.setImageResource(R.mipmap.icon_pause);
        vid_name = "VID_" + getModifyTime() + ".mp4";

        file = new File(getSDPath(this) + vid_name);
        if (file.exists()) {
            // 如果文件存在，删除它，演示代码保证设备上只有一个录音文件            file.delete();
        }
        camera.stopPreview();
        camera.unlock();

        mediarecorder = new MediaRecorder();// 创建mediarecorder对象

        mediarecorder.setCamera(camera);
        // 设置录制视频源为Camera(相机)
        mediarecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mediarecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        // 录像旋转90度
        mediarecorder.setOrientationHint(displayOrientation);
        // mediaRecorder.setVideoSource(VideoSource.CAMERA);
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mediarecorder.setOutputFormat(OutputFormat.MPEG_4);
        // // 设置录制的视频编码h263 h264
        mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
        mediarecorder.setVideoSize(1280, 720);

        mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        // 设置高质量录制,改变编码速率
        mediarecorder.setVideoEncodingBitRate(BitRate * 1024 * 512);

        // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
        mediarecorder.setVideoFrameRate(30);

        mediarecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        // 设置视频文件输出的路径

        currentVideoFilePath = getSDPath(MainActivity.this) + vid_name;
        mediarecorder.setOutputFile(currentVideoFilePath);
        try {
            // 准备、开始
            mediarecorder.prepare();
            mediarecorder.start();//开始刻录
            isRecording = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        mediarecorder.setOnErrorListener(new OnErrorListener() {

            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                // 发生错误，停止录制
                mediarecorder.stop();
                mediarecorder.reset();
                mediarecorder.release();
                mediarecorder = null;
                isRecording = false;

            }
        });



        start.setImageResource(R.mipmap.icon_recording);
        handler.post(timeRun);

    }

    protected void stop() {
        img_resume_pause.setImageResource(R.mipmap.icon_start);
        if (isRecording) {
            // 如果正在录制，停止并释放资源
            mediarecorder.setOnErrorListener(null);
            mediarecorder.setPreviewDisplay(null);

            mediarecorder.stop();
            mediarecorder.reset();
            mediarecorder.release();
            mediarecorder = null;
            isRecording = false;
            handler.removeCallbacks(timeRun);
            start.setImageResource(R.mipmap.icon_record);
//            camera.lock();
//            if (camera != null) {
//                camera.release();
//            }

        }
    }

    boolean isPause = false;
    String saveVideoPath = "";
    private boolean record = true;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                if (record) {

                    img_resume_pause.setVisibility(View.VISIBLE);
                    record = false;
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            start();
                        }
                    });

                } else {

//                    start.setImageResource(R.mipmap.record_start);
                    if (isPause) {
                        Intent intent = new Intent(MainActivity.this,PlayVideo.class);
                        Bundle bundle = new Bundle();
                            bundle.putString("videoPath",saveVideoPath);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();

                    } else {
                        stop();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    if (!(saveVideoPath.equals(""))) {
                                        String[] str = new String[]{saveVideoPath, currentVideoFilePath};
                                        VideoUtils.appendVideo(MainActivity.this, getSDPath(MainActivity.this) + "append.mp4", str);
                                        File reName = new File(saveVideoPath);
                                        File f = new File(getSDPath(MainActivity.this) + "append.mp4");
                                        f.renameTo(reName);
                                        if (reName.exists()) {
                                            f.delete();
                                            new File(currentVideoFilePath).delete();
                                        }

                                    }
                                    Intent intent = new Intent(MainActivity.this,PlayVideo.class);
                                    Bundle bundle = new Bundle();
                                    if (saveVideoPath.equals("")){
                                        bundle.putString("videoPath",currentVideoFilePath);

                                    }else {
                                        bundle.putString("videoPath",saveVideoPath);
                                    }
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    finish();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();


                    }
                }
                break;


            case R.id.img_resume_pause:

                if (!isRecording) {
                    time--;
                    start();
                    isPause = false;
                } else {
                    stop();
                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success == true)
                                MainActivity.this.camera.cancelAutoFocus();
                        }
                    });
                    isPause = true;

                    if (saveVideoPath.equals("")) {
                        saveVideoPath = currentVideoFilePath;

                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String[] str = new String[]{saveVideoPath, currentVideoFilePath};
                                    VideoUtils.appendVideo(MainActivity.this, getSDPath(MainActivity.this) + "append.mp4", str);
                                    File reName = new File(saveVideoPath);
                                    File f = new File(getSDPath(MainActivity.this) + "append.mp4");
                                    f.renameTo(reName);
                                    if (reName.exists()) {
                                        f.delete();
                                        new File(currentVideoFilePath).delete();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }

                break;
            default:
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (isPause) {
                    stop();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String[] str = new String[]{saveVideoPath, currentVideoFilePath};
                                VideoUtils.appendVideo(MainActivity.this, getSDPath(MainActivity.this) + "append.mp4", str);
                                File reName = new File(saveVideoPath);
                                File f = new File(getSDPath(MainActivity.this) + "append.mp4");
                                f.renameTo(reName);
                                if (reName.exists()) {
                                    f.delete();
                                    new File(currentVideoFilePath).delete();
                                }
                                Intent intent = new Intent(MainActivity.this,PlayVideo.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("videoPath",saveVideoPath);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                } else {
                    finish();
                }

                break;
            default:
                break;
        }
        return true;
    }


    public static String getSDPath(Context context) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        } else if (!sdCardExist) {

            Toast.makeText(context, "SD卡不存在", Toast.LENGTH_SHORT).show();

        }
        File eis = new File(sdDir.toString() + "/Video/");
        try {
            if (!eis.exists()) {
                eis.mkdir();
            }
        } catch (Exception e) {

        }
        return sdDir.toString() + "/Video/";

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
        Log.d(TAG, "surfaceCreated");

        if (null == camera) {
            camera = Camera.open();

//            try {
            try {

                camera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            camera();
            camera.startPreview();

        }

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");

        camera();
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            params.set("orientation","portrait");
            camera.setDisplayOrientation(90);
        }else {
            params.set("orientation","landscape");
            camera.setDisplayOrientation(0);
        }
    }

    private void camera() {
        try {
            camera.stopPreview();
            params = camera.getParameters();
            params.setPreviewSize(1280, 720);
            params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int ort = getResources().getConfiguration().orientation;
        if (ort == Configuration.ORIENTATION_PORTRAIT) {
            displayOrientation = 90;
//            Toast.makeText(this, "竖屏", Toast.LENGTH_SHORT).show();
        } else if (ort == Configuration.ORIENTATION_LANDSCAPE) {
            displayOrientation = 0;
//            Toast.makeText(this, "横屏", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
// surfaceDestroyed的时候同时对象设置为null

        if (mediarecorder != null) {

            mediarecorder.setOnErrorListener(null);
            mediarecorder.setPreviewDisplay(null);
            mediarecorder.stop();
            mediarecorder.reset();
            mediarecorder.release();
            mediarecorder = null;
            camera.lock();
        }
        camera.stopPreview();
        camera.release();
        camera = null;
//        finish();
    }





    public void makeDirs() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        } else if (!sdCardExist) {
        }

        File dirs_v = new File(sdDir + "/Video");

        if (!dirs_v.exists())
            dirs_v.mkdirs();

    }


}
