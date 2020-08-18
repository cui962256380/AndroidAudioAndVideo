package com.rich.imageview;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {


    private ImageView mImageView;

    private SurfaceView mSurfaceView;
    float h;
    float w;
    WindowManager wm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initImageView();
        initSurfaceView();
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        h = wm.getDefaultDisplay().getHeight();
        w = wm.getDefaultDisplay().getWidth();

    }

    private void initSurfaceView() {
        mSurfaceView =findViewById(R.id.surfaceview);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                   if(holder==null) return;
                Paint paint=new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE); //描边

                Canvas canvas=holder.lockCanvas(); // 先锁定当前surfaceView的画布
                Bitmap bitmap=BitmapFactory.decodeResource(MainActivity.this.getResources(),R.drawable.bear);
                Matrix matrix=new Matrix();
                h = wm.getDefaultDisplay().getHeight();
                w = wm.getDefaultDisplay().getWidth();

                matrix.setScale(w/bitmap.getWidth(),h/bitmap.getHeight());
                canvas.drawBitmap(bitmap,0,0,paint);//执行绘制操作
                holder.unlockCanvasAndPost(canvas); //解锁并且绘制

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    void initImageView() {
        mImageView = findViewById(R.id.imageview);
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bear);
        mImageView.setImageBitmap(bitmap);
    }
}
