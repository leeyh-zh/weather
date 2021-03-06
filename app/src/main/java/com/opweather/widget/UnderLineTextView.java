package com.opweather.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.internal.view.SupportMenu;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.opweather.R;


public class UnderLineTextView extends TextView {
    private Paint mPaint;
    private Rect mRect;
    private float mStrokeWidth;

    public UnderLineTextView(Context context) {
        this(context, null, 0);
    }

    public UnderLineTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnderLineTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        float density = context.getResources().getDisplayMetrics().density;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.UnderlinedTextView, defStyleAttr, 0);
        int mColor = array.getColor(R.styleable.UnderlinedTextView_underlineColor, SupportMenu.CATEGORY_MASK);
        mStrokeWidth = array.getDimension(R.styleable.UnderlinedTextView_underlineWidth, 2.0f * density);
        array.recycle();
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();
        Layout layout = getLayout();
        for (int i = 0; i < count; i++) {
            int baseline = getLineBounds(i, mRect);
            int firstCharInLine = layout.getLineStart(i);
            int lastCharInLine = layout.getLineVisibleEnd(i);
            float f = (mStrokeWidth * 2.0f) + ((float) baseline);
            Canvas canvas2 = canvas;
            canvas2.drawLine(layout.getPrimaryHorizontal(firstCharInLine), f, layout.getPrimaryHorizontal
                    (lastCharInLine), (mStrokeWidth * 2.0f) + ((float) baseline), mPaint);
        }
        super.onDraw(canvas);
    }
}
