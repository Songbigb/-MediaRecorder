package com.test.myrecord;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;

public class PlayVideo extends Activity implements MediaPlayer.OnPreparedListener, mMediaController.MediaPlayerControl {
    public static final String TAG = "PlayVideo";
    private MyVideoView videoView;
    private mMediaController controller;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.playvideo);
        String videoPath = getIntent().getExtras().getString("videoPath");

        File sourceVideoFile = new File(videoPath);
        videoView = (MyVideoView) findViewById(R.id.videoView);
        int screenW = getWindowManager().getDefaultDisplay().getWidth();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        params.width = screenW;
        params.height = screenW * 4 / 3;
        params.gravity = Gravity.TOP;
        videoView.setLayoutParams(params);

        videoView.setOnPreparedListener(this);
        controller = new mMediaController(this);
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            videoView.setVideoURI(Uri.fromFile(sourceVideoFile));
        }


    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Intent intent = new Intent(PlayVideo.this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
        return true;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(this);
        controller.setAnchorView((ViewGroup) findViewById(R.id.fl_videoView_parent));
        controller.show();

    }

    @Override
    public void start() {
        videoView.start();
    }

    @Override
    public void pause() {
        if (videoView.isPlaying()){
            videoView.pause();
        }
    }

    @Override
    public int getDuration() {
        return videoView.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        videoView.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return videoView.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return videoView.canPause();
    }

    @Override
    public boolean canSeekBackward() {
        return videoView.canSeekBackward();
    }

    @Override
    public boolean canSeekForward() {
        return videoView.canSeekForward();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }
}
