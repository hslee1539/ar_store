package com.example.cvtest03;

import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class DrawingQRHandler extends Handler {
    private com.example.cvtest03.Button[] buttons;
    private ViewGroup layout;
    private float inWidth;
    private float inHeight;
    private float[] rect;
    private String[] strs;
    private QR2Information qr2Information;

    public DrawingQRHandler(Button[] buttons, ViewGroup layout, QR2Information qr2Information){
        super();
        this.buttons = buttons;
        this.layout = layout;
        this.qr2Information = qr2Information;
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        float outWidth = this.layout.getWidth();
        float outHeight = this.layout.getHeight();
        for(int i = 0; i < strs.length; i++) {
            buttons[i].setX(outWidth * (1 - rect[3 + i * 4] / this.inWidth));
            buttons[i].setY(outHeight * rect[2 + i * 4] / this.inHeight - outWidth * (rect[3 + i * 4] - rect[1 + i * 4]) / this.inWidth);
            buttons[i].setLayoutParams(new RelativeLayout.LayoutParams((int) (outWidth * (rect[3 + i * 4] - rect[1 + i * 4]) / this.inWidth), (int)(outWidth * (rect[3 + i * 4] - rect[1 + i * 4]) / this.inWidth)));
            //buttons[i].setLayoutParams(new RelativeLayout.LayoutParams((int) (outWidth * (rect[3] - rect[1]) / this.inWidth), (int) (outHeight * (rect[2] - rect[0]) / this.inHeight)));
            buttons[i].setText(qr2Information.getInformStringScale(strs[i], (int) (outWidth * (rect[3 + i * 4] - rect[1 + i * 4]) / this.inWidth)));
            buttons[i].summary = qr2Information.getInformation(strs[i], 0);
            buttons[i].invalidate();
        }
        for(int i =  strs.length; i < buttons.length; i++){
            buttons[i].setX(-outWidth);
            buttons[i].setY(-outHeight);
            buttons[i].invalidate();
        }
    }
    public void setData(float[] rect, String[] strs){
        this.rect = rect;
        this.strs = strs;
    }
    public void setInputSize(int width, int height){
        this.inWidth = width;
        this.inHeight = height;
    }
}
