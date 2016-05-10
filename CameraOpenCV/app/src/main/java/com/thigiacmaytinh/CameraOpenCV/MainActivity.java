package com.thigiacmaytinh.CameraOpenCV;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity implements CvCameraViewListener2, View.OnTouchListener{

    CameraView mOpenCvCameraView;
    private int cameraIndex = CameraBridgeViewBase.CAMERA_ID_BACK;
    private CascadeClassifier cascadeClassifier = null;
    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;
    private boolean isFlashEnable = false;
    private ImageButton btnFlash;
    private ImageButton btnCamera;
    private Button btnScale;
    private TextView txtCascades;
    private Mat mRgba;
    private Mat mGray;
    RelativeLayout mContainerView;
    LayoutInflater inflater;
    String log = ">>>>>>>>";
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void initializeOpenCVDependencies() {
        try {
            // Copy the resource into a temp file so OpenCV can load it
            int cascades = getIntent().getExtras().getInt(FirstActivity.TAG_CASCADES);
            InputStream is;
            if (cascades == FirstActivity.TAG_CASCADES_HAAR){
                is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                txtCascades.setText(getText(R.string.haar_cascades));
            } else {
                is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                txtCascades.setText(getText(R.string.lbp_cascades));
            }

            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "cascade.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();


            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            cascadeClassifier.load(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }


        // And we are ready to go
        mOpenCvCameraView.enableView();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        CheckFlashlight();

        mContainerView = (RelativeLayout)findViewById(R.id.mainview);
        inflater =(LayoutInflater)getSystemService(this.LAYOUT_INFLATER_SERVICE);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    void initView(){
        mOpenCvCameraView = (CameraView) findViewById(R.id.surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setFocusable(true);
        mOpenCvCameraView.setCameraIndex(cameraIndex);
        btnFlash = (ImageButton) findViewById(R.id.btnFlash);
        btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        btnScale = (Button) findViewById(R.id.btnScale);
        txtCascades = (TextView) findViewById(R.id.txtCascades);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onCameraViewStopped()
    {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }



        MatOfRect faces = new MatOfRect();

        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mGray, faces, 1.1, 2, 2,
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }


        // If there are any faces found, draw a rectangle around it
        Rect[] facesArray = faces.toArray();
        Log.d("HUNG", String.valueOf(facesArray.length));
        for (int i = 0; i < facesArray.length; i++){
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
            Log.d("HUNG", facesArray[i].toString());
        }

        return mRgba;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean onTouch(View v, MotionEvent event)
    {
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onCameraViewStarted(int width, int height)
    {
        mGray = new Mat();
        mRgba = new Mat();

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void btnFlash_onClick(View v)
    {
        if(isFlashEnable)
        {
            btnFlash.setImageResource(R.mipmap.ic_flash_on_white_24dp);
        } else {
            btnFlash.setImageResource(R.mipmap.ic_flash_off_white_24dp);
        }
        mOpenCvCameraView.setupCameraFlashLight();
        isFlashEnable = !isFlashEnable;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void CheckFlashlight()
    {
        if(!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            btnFlash.setVisibility(View.GONE);
    }

    public void btnScale_onClick(View view) {
        if (mRelativeFaceSize == 0.2f) {
            setMinFaceSize(0.3f);
            btnScale.setText("30%");
        } else if (mRelativeFaceSize == 0.3f) {
            setMinFaceSize(0.4f);
            btnScale.setText("40%");
        } else if (mRelativeFaceSize == 0.4f) {
            setMinFaceSize(0.5f);
            btnScale.setText("50%");
        } else if (mRelativeFaceSize == 0.5f) {
            setMinFaceSize(0.2f);
            btnScale.setText("20%");
        }
    }

    public void btnCame_onClick(View view) {
        if (cameraIndex == CameraBridgeViewBase.CAMERA_ID_BACK){
            cameraIndex = CameraBridgeViewBase.CAMERA_ID_FRONT;
            btnCamera.setImageResource(R.mipmap.ic_camera_front_white_24dp);
        } else {
            cameraIndex = CameraBridgeViewBase.CAMERA_ID_BACK;
            btnCamera.setImageResource(R.mipmap.ic_camera_rear_white_24dp);
        }
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(cameraIndex);
        mOpenCvCameraView.enableView();

    }
}
