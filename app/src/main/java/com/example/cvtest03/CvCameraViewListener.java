package com.example.cvtest03;

import com.example.cvtest03.conponent.CameraBridgeViewBase;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.polylines;


/*
* @see CameraBridgeViewBase.CvCameraViewListener2
* */
public class CvCameraViewListener implements CameraBridgeViewBase.CvCameraViewListener2 , Runnable{
    @Override
    public void run() {
        final String s = qrRead(this.inputFrame.getNativeObjAddr(), this.box.getNativeObjAddr()); // box 크기 1 * 2 * 2(채널) 을 가지게 만들어야 함.
        if(s.length() > 0){
            this.box.get(0,0, this._data);
            String[] s1 = s.split(" ");
            this.handler.setData(this._data, s1);
            this.handler.sendEmptyMessage(0);

        }
    }
    public native void startCamera();
    public native  void startCamera(long number);
    public native void stopCamera();
    public native String qrRead(long matAddressInput, long matAdressBox);

    public Mat inputFrame = null;
    public Mat box = null;
    public float[] _data;
    public float width;
    public float height;
    public DrawingQRHandler handler;

    public CvCameraViewListener(int number, DrawingQRHandler handler){
        this.box = Mat.zeros(number, 2, CvType.CV_32FC2);
        this.handler = handler;
        this._data = new float[number * 2 * 2];
        this.startCamera(number);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.handler.setInputSize(height, width);
    }

    @Override
    public void onCameraViewStopped() {
        // input을 릴리즈 해야 하는지는 아직 잘 모르지만 이렇게 해봄
        if( this.inputFrame != null)    this.inputFrame.release();
        stopCamera();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        this.inputFrame = inputFrame.rgba();
        Thread thread = new Thread(this);
        thread.start();
        try {
            thread.join(75);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return this.inputFrame;
    }
}
