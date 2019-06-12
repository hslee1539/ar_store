package com.example.cvtest03;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.example.cvtest03.conponent.CameraBridgeViewBase;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private final int numberOfDectection = 3;
    private Button[] buttons = null;
    RelativeLayout layout = null;
    private static final String TAG = "opencv";
    private LoaderCallback loaderCallback = null;
    private CameraBridgeViewBase cameraBridgeViewBase = null;
    private CvCameraViewListener cameraViewListener = null;
    private DrawingQRHandler handler = null;
    private QR2Information qr2Information = new QR2Information();
    private ButtonClick buttonClick;
    private ArrayList<String> list;


    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
        RecyclerView recyclerView = findViewById(R.id.recycle) ;
        recyclerView.bringToFront();
        this.list = new ArrayList<>();
        this.buttonClick = new ButtonClick(recyclerView);
        layout = findViewById(R.id.layout);
        this.buttons = new Button[numberOfDectection];
        for(int i = 0; i < numberOfDectection; i ++){
            this.buttons[i] = new com.example.cvtest03.Button(this);//Button(this);
            this.buttons[i].setId(i);
            this.buttons[i].setX(-10);
            this.buttons[i].setY(-10);
            this.buttons[i].setBackgroundColor(Color.argb(200,255,255,255));
            this.buttons[i].setText("");
            this.buttons[i].setOnClickListener(this.buttonClick);
            this.layout.addView(this.buttons[i], new RelativeLayout.LayoutParams(1,1));
        }

        this.handler = new DrawingQRHandler(this.buttons, this.layout, qr2Information);
        this.cameraViewListener = new CvCameraViewListener(this.numberOfDectection, this.handler);
        this.cameraBridgeViewBase = findViewById(R.id.activity_surface_view);
        this.cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        this.cameraBridgeViewBase.setCvCameraViewListener(this.cameraViewListener);
        this.cameraBridgeViewBase.setCameraIndex(0); // front-camera(1),  back-camera(0)
        this.loaderCallback = new LoaderCallback(this, this.cameraBridgeViewBase);
        this.loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);


        recyclerView.setLayoutManager(new LinearLayoutManager(this)) ;
        RecycleAdapter adapter = new RecycleAdapter() ;
        recyclerView.setAdapter(adapter) ;
        recyclerView.invalidateItemDecorations();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (this.cameraBridgeViewBase != null)
            this.cameraBridgeViewBase.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, this.loaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            this.loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (this.cameraBridgeViewBase != null)
            this.cameraBridgeViewBase.disableView();
    }




    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA"};


    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted)
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }

}