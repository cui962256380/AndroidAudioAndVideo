package com.rich.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class CustomView extends View {

    public CustomView(Context context) {
        super(context);
        init(context);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureHeight(int heightMeasureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 200;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;

    }


    private int measureWidth(int widthMeasureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        //MeasureSpec共3种测量模式
        //EXACZTLY  精确值模式，layout_width或者layout_height属性设置为具体数值，或指定为match_parent时
        //AT_MOST 最大值模式，layout_width或者layout_height属性设置为wrap_content时，此时空间尺寸只要不超过父控件允许的最大尺寸即可
        //UNSPECIFIED 不限制View大小
        if (specMode == MeasureSpec.EXACTLY) {  //如果为EXACZTLY模式，直接使用指定specSize
            result = specSize;
        } else {
            result = 200;   //否则的话，specSize设置为200
            if (specMode == MeasureSpec.AT_MOST) {  //如果模式为AT_MOST模式，则取出我们指定的大小与specSize中小的那个座位最后的测量值
                result = Math.min(result, specSize);
            }
        }
        return result;

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    Paint paint =new Paint();
    Bitmap bitmap;
    private void init(Context context) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
         bitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.bear);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap,0,0,paint);
    }
}
