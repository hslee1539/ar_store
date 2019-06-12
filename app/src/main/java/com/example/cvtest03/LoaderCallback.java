package com.example.cvtest03;

import android.content.Context;

import com.example.cvtest03.conponent.CameraBridgeViewBase;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;


public class LoaderCallback extends BaseLoaderCallback {
    /// 필드
    CameraBridgeViewBase cameraBridgeView;


    public LoaderCallback(Context AppContext, CameraBridgeViewBase cameraBridgeView) {
        super(AppContext);
        this.cameraBridgeView = cameraBridgeView;
    }


    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS:
            {
                this.cameraBridgeView.enableView();
            } break;
            default:
            {
                super.onManagerConnected(status);
            } break;
        }
    }
}
