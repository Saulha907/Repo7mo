package com.example.blackjack21.view;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.example.blackjack21.R;

/**
 * Texto con relleno metálico dorado y sombra. Compatible con autoSize.
 */
public class GoldTextView extends AppCompatTextView {

    private final int[] colors;
    private final float[] stops = {0f, 0.45f, 0.55f, 1f};

    public GoldTextView(Context context) {
        this(context, null);
    }

    public GoldTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public GoldTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        colors = new int[]{
                ContextCompat.getColor(context, R.color.gold_light),
                ContextCompat.getColor(context, R.color.gold),
                ContextCompat.getColor(context, R.color.gold_dark),
                ContextCompat.getColor(context, R.color.gold_light)
        };
        float d = getResources().getDisplayMetrics().density;
        setShadowLayer(4f * d, 0f, 2.5f * d, ContextCompat.getColor(context, R.color.shadow));
        setTextColor(colors[1]);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float textTop = getTotalPaddingTop();
        float textBottom = getHeight() - getTotalPaddingBottom();
        if (textBottom <= textTop) {
            return;
        }
        getPaint().setShader(new LinearGradient(0f, textTop, 0f, textBottom,
                colors, stops, Shader.TileMode.CLAMP));
    }
}
