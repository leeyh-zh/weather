package com.opweather.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.oneplus.lib.widget.OPProgressBar;
import com.opweather.R;

import net.oneplus.weather.util.StringUtils;

import java.text.NumberFormat;

public class OPProgressDialog extends OPAlertDialog {
    public static final int STYLE_HORIZONTAL = 1;
    public static final int STYLE_SPINNER = 0;
    private boolean mHasStarted;
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private int mMax;
    private CharSequence mMessage;
    private TextView mMessageView;
    private OPProgressBar mProgress;
    private Drawable mProgressDrawable;
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;
    private int mProgressStyle;
    private int mProgressVal;
    private int mSecondaryProgressVal;
    private Handler mViewUpdateHandler;

    public OPProgressDialog(Context context) {
        super(context);
        this.mProgressStyle = 0;
        initFormats();
    }

    public OPProgressDialog(Context context, int theme) {
        super(context, theme);
        this.mProgressStyle = 0;
        initFormats();
    }

    private void initFormats() {
        this.mProgressNumberFormat = "%1d/%2d";
        this.mProgressPercentFormat = NumberFormat.getPercentInstance();
        this.mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    public static OPProgressDialog show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    public static OPProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static OPProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static OPProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        OPProgressDialog dialog = new OPProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        View view;
        if (this.mProgressStyle == 1) {
            this.mViewUpdateHandler = new Handler() {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    int progress = OPProgressDialog.this.mProgress.getProgress();
                    int max = OPProgressDialog.this.mProgress.getMax();
                    if (OPProgressDialog.this.mProgressNumberFormat != null) {
                        String format = OPProgressDialog.this.mProgressNumberFormat;
                        OPProgressDialog.this.mProgressNumber.setText(String.format(format, new Object[]{Integer.valueOf(progress), Integer.valueOf(max)}));
                    } else {
                        OPProgressDialog.this.mProgressNumber.setText(StringUtils.EMPTY_STRING);
                    }
                    if (OPProgressDialog.this.mProgressPercentFormat != null) {
                        SpannableString tmp = new SpannableString(OPProgressDialog.this.mProgressPercentFormat.format(((double) progress) / ((double) max)));
                        tmp.setSpan(new StyleSpan(1), 0, tmp.length(), R.styleable.OneplusTheme_op_borderWidth);
                        OPProgressDialog.this.mProgressPercent.setText(tmp);
                        return;
                    }
                    OPProgressDialog.this.mProgressPercent.setText(StringUtils.EMPTY_STRING);
                }
            };
            view = inflater.inflate(com.oneplus.commonctrl.R.layout.op_alert_dialog_progress_material, null);
            this.mProgress = (OPProgressBar) view.findViewById(16908301);
            this.mProgressNumber = (TextView) view.findViewById(com.oneplus.commonctrl.R.id.progress_number);
            this.mProgressPercent = (TextView) view.findViewById(com.oneplus.commonctrl.R.id.progress_percent);
            setView(view);
        } else {
            view = inflater.inflate(com.oneplus.commonctrl.R.layout.op_progress_dialog_material, null);
            this.mProgress = (OPProgressBar) view.findViewById(16908301);
            this.mMessageView = (TextView) view.findViewById(16908299);
            setView(view);
        }
        if (this.mMax > 0) {
            setMax(this.mMax);
        }
        if (this.mProgressVal > 0) {
            setProgress(this.mProgressVal);
        }
        if (this.mSecondaryProgressVal > 0) {
            setSecondaryProgress(this.mSecondaryProgressVal);
        }
        if (this.mIncrementBy > 0) {
            incrementProgressBy(this.mIncrementBy);
        }
        if (this.mIncrementSecondaryBy > 0) {
            incrementSecondaryProgressBy(this.mIncrementSecondaryBy);
        }
        if (this.mProgressDrawable != null) {
            setProgressDrawable(this.mProgressDrawable);
        }
        if (this.mIndeterminateDrawable != null) {
            setIndeterminateDrawable(this.mIndeterminateDrawable);
        }
        if (this.mMessage != null) {
            setMessage(this.mMessage);
        }
        setIndeterminate(this.mIndeterminate);
        onProgressChanged();
        super.onCreate(savedInstanceState);
    }

    public void onStart() {
        super.onStart();
        this.mHasStarted = true;
    }

    protected void onStop() {
        super.onStop();
        this.mHasStarted = false;
    }

    public void setProgress(int value) {
        if (this.mHasStarted) {
            this.mProgress.setProgress(value);
            onProgressChanged();
            return;
        }
        this.mProgressVal = value;
    }

    public void setSecondaryProgress(int secondaryProgress) {
        if (this.mProgress != null) {
            this.mProgress.setSecondaryProgress(secondaryProgress);
            onProgressChanged();
            return;
        }
        this.mSecondaryProgressVal = secondaryProgress;
    }

    public int getProgress() {
        return this.mProgress != null ? this.mProgress.getProgress() : this.mProgressVal;
    }

    public int getSecondaryProgress() {
        return this.mProgress != null ? this.mProgress.getSecondaryProgress() : this.mSecondaryProgressVal;
    }

    public int getMax() {
        return this.mProgress != null ? this.mProgress.getMax() : this.mMax;
    }

    public void setMax(int max) {
        if (this.mProgress != null) {
            this.mProgress.setMax(max);
            onProgressChanged();
            return;
        }
        this.mMax = max;
    }

    public void incrementProgressBy(int diff) {
        if (this.mProgress != null) {
            this.mProgress.incrementProgressBy(diff);
            onProgressChanged();
            return;
        }
        this.mIncrementBy += diff;
    }

    public void incrementSecondaryProgressBy(int diff) {
        if (this.mProgress != null) {
            this.mProgress.incrementSecondaryProgressBy(diff);
            onProgressChanged();
            return;
        }
        this.mIncrementSecondaryBy += diff;
    }

    public void setProgressDrawable(Drawable d) {
        if (this.mProgress != null) {
            this.mProgress.setProgressDrawable(d);
        } else {
            this.mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (this.mProgress != null) {
            this.mProgress.setIndeterminateDrawable(d);
        } else {
            this.mIndeterminateDrawable = d;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (this.mProgress != null) {
            this.mProgress.setIndeterminate(indeterminate);
        } else {
            this.mIndeterminate = indeterminate;
        }
    }

    public boolean isIndeterminate() {
        return this.mProgress != null ? this.mProgress.isIndeterminate() : this.mIndeterminate;
    }

    public void setMessage(CharSequence message) {
        if (this.mProgress == null) {
            this.mMessage = message;
        } else if (this.mProgressStyle == 1) {
            super.setMessage(message);
        } else {
            this.mMessageView.setText(message);
        }
    }

    public void setProgressStyle(int style) {
        this.mProgressStyle = style;
    }

    public void setProgressNumberFormat(String format) {
        this.mProgressNumberFormat = format;
        onProgressChanged();
    }

    public void setProgressPercentFormat(NumberFormat format) {
        this.mProgressPercentFormat = format;
        onProgressChanged();
    }

    private void onProgressChanged() {
        if (this.mProgressStyle == 1 && this.mViewUpdateHandler != null && !this.mViewUpdateHandler.hasMessages(0)) {
            this.mViewUpdateHandler.sendEmptyMessage(0);
        }
    }
}
