package com.rich.camera;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;

import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements View.OnClickListener,View.OnLongClickListener {

    private static final String TAG = "Cuihangchao";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    CameraManager mCameraManager;
    private String mCameraId;
    private CameraDevice mCameraDevice;
    private Size previewSize;
    private Size[] saveSize;
    private List<Size> outPutSize;
    CameraCaptureSession cameraCaptureSession;

    private ImageButton mImagerButton;
    private ImageView imagePhoto, mSwitchButton;


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private void front() {
        //前置时，照片竖直显示
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 90);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private void rear() {
        //后置时，照片竖直显示
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();

        mImagerButton = findViewById(R.id.picture);
        mImagerButton.setOnClickListener(this);

        imagePhoto = findViewById(R.id.imagephoto);
        front();
        initHander();

        mSwitchButton = findViewById(R.id.zhuanhuan);
        mSwitchButton.setOnClickListener(this);


        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else {
                    initCamera2(false); //初始化后摄
                    openCamera2();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    HandlerThread handlerThread;
    Handler cameraHandler;
    Handler handler;

    @SuppressLint("HandlerLeak")
    private void initHander() {
        if (handlerThread == null) {
            handlerThread = new HandlerThread("");
            handlerThread.start();
            cameraHandler = new Handler(handlerThread.getLooper());
        }
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    imagePhoto.setVisibility(View.VISIBLE);
                    imagePhoto.setImageBitmap(bitmap);
                }

            }
        };
    }

    CameraDevice.StateCallback mStatecallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "Camera open Success");
            CameraUtils.getInstance().setCameraDevice(camera);
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "Camera open onError");
        }
    };


    @SuppressLint("MissingPermission")
    private void openCamera2() {
        if (mCameraManager == null) return;
        try {
            mCameraManager.openCamera(mCameraId, mStatecallBack, cameraHandler);
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
    }

    ImageReader mImagerReader;

    private void startPreview() {
        Surface surface = mSurfaceHolder.getSurface();
        mImagerReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 2);
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImagerReader.getSurface()), new CameraCaptureSession.StateCallback() {   //创建绘画
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        cameraCaptureSession = session;
                        CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        builder.addTarget(mSurfaceHolder.getSurface());
                        CaptureRequest captureRequest = builder.build();
                        session.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
                                    @Override
                                    public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                                        super.onCaptureProgressed(session, request, partialResult);
                                    }

                                    @Override
                                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                        super.onCaptureCompleted(session, request, result);
                                    }
                                },
                                cameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    Rect activeArray;

    private void initCamera2(boolean isback) {
        CameraUtils.init(this);
        mCameraManager = CameraUtils.getInstance().getCameraManager();

        mCameraId = CameraUtils.getInstance().getCameraID(isback);


        StreamConfigurationMap map = CameraUtils.getInstance().getMapForIf(mCameraId);
        //打印大小
        printSize(mCameraId, map);

        /*map.getOutputSizes(ImageFormat.JPEG) //照片输出尺寸
        map.getOutputSizes(SurfaceTexture.class)//预览支持尺寸
        map.getOutputSizes(MediaRecorder.class)//录制的视频支持尺寸*/
        outPutSize = CameraUtils.getInstance().getCameraOutputSizes(mCameraId, SurfaceTexture.class);   //
        Log.d(TAG, "mSurfaceView.width=" + mSurfaceView.getWidth() + "\t mSurfaceView.Height" + mSurfaceView.getHeight());
        //设置最适合的大小 通过 获取出来的大小 和mSurfaceView 的宽高进行比较 设置最合适的的预览大小
        previewSize = chooseOptimalSize(outPutSize, mSurfaceView.getWidth(), mSurfaceView.getHeight());


        Log.d(TAG, "previewSize=" + previewSize);

    }


    private void printSize(String cameraId, StreamConfigurationMap mConfigurationMap) {
        Log.d(TAG, "cameraId=" + cameraId);
        Log.d(TAG, "mConfigurationMap =" + mConfigurationMap);
        Log.d(TAG, "outPutSize=" + mConfigurationMap.getOutputSizes(SurfaceTexture.class));
    }

    /**
     * @param choices
     * @param width
     * @param height
     * @return
     */
    private Size chooseOptimalSize(List<Size> choices
            , int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : choices) {

            Log.d(TAG, "chooseOptimalSize " + option);
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.max(sizeList, new CompareSizesByArea());
        }
        return choices.get(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
    }

    Bitmap bitmap;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zhuanhuan:
                resetCamera();
                break;
            case R.id.picture:
                picture();
                break;
        }

    }


    private void resetCamera() {
        closeCamera();


        if (mCameraId.equals("0")) {
            initCamera2(true);
        } else {
            initCamera2(false);
        }


        openCamera2();
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImagerReader != null) {
            mImagerReader.close();
            mImagerReader = null;
        }

    }

    private void picture() {
        if (mCameraDevice == null) return;
        try {
            cameraCaptureSession.stopRepeating();  //停止预览            //mCameraDevice.createCaptureSession(Arrays.asList(surface,mImagerReader.getSurface())
            Log.d(TAG, "imageReader=" + mImagerReader.getSurface());   //想要通过ImagerReader获取 图片，一定要将imagerReader的surface传入 创建的回话中
            mImagerReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    String name = String.valueOf(System.currentTimeMillis());
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);//由缓冲区存入字节数组
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (mCameraId.equals("0")) {   //后摄旋转了90度
                        Matrix m = new Matrix();
                        m.postRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                    } else {
                        Matrix m = new Matrix();
                        m.postRotate(270);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                    }
                    if (bitmap != null) {
                        handler.sendEmptyMessage(1);
                        ImageSaveUtil.saveBitmap2file(bitmap, MainActivity.this, name);
                    }
                }
            }, cameraHandler);
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(mImagerReader.getSurface());
            CaptureRequest request = builder.build();
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    startPreview();
                    Log.d(TAG, "onCaptureCompleted");
                }

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.d(TAG, "onCaptureFailed");
                }
            };
            cameraCaptureSession.capture(request, captureCallback, cameraHandler);
        } catch (CameraAccessException e) {
            Log.d(TAG, "err=" + e);
            e.printStackTrace();
        }
    }


    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    // 为Size定义一个比较器Comparator
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // 强转为long保证不会发生溢出
            return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
        }
    }
}
