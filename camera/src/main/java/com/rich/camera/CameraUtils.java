package com.rich.camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraUtils {

    private static CameraUtils ourInstance = new CameraUtils();


    private static Context appContext;
    private static CameraManager cameraManager;
    private CameraDevice cameraDevice;

    public CameraDevice getCameraDevice() {
        return cameraDevice;
    }

    public void setCameraDevice(CameraDevice cameraDevice) {
        this.cameraDevice = cameraDevice;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
            cameraManager = (CameraManager) appContext.getSystemService(Context.CAMERA_SERVICE);
        }
    }

    public static CameraUtils getInstance() {
        return ourInstance;
    }


    public CameraManager getCameraManager() {
        return cameraManager;
    }



    public String getCameraID(boolean isBack) {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

                int cameraFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (isBack) {
                    if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                        return cameraId;
                    }
                } else {
                    if (cameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        return cameraId;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void CameraUtile(){

    }
    public StreamConfigurationMap getMapForIf(String cameraId) {
        if (cameraId == null) return null;
        CameraCharacteristics characteristics = null;
        try {
            characteristics = cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        return configs;
    }

    public List<Size> getCameraOutputSizes(String cameraId, Class clz) {
        if (cameraId == null) return null;
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            return Arrays.asList(configs.getOutputSizes(clz));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void saveImage(final Image image){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(image==null) return;
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();

                byte[] data = new byte[buffer.remaining()];
                File file = new File(Environment.getExternalStorageDirectory()+ "/DCIM/1.jpg");
                FileOutputStream fileOutputStream=null;
                try {

                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(data, 0 ,data.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        fileOutputStream.close();
                        fileOutputStream =null;
                        image.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
}
