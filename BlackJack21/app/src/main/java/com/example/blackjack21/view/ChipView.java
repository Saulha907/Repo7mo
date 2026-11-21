package com.example.blackjack21.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.blackjack21.R;
import com.example.blackjack21.view.paint.ChipPainter;

/**
 * Ficha seleccionable del selector de apuestas.
 */
public class ChipView extends View {

    private final ChipPainter painter;
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private int amount;
    private String label;

    public ChipView(Context context) {
        this(context, null);
    }

    public ChipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        painter = new ChipPainter(context);
        density = getResources().getDisplayMetrics().density;
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(ContextCompat.getColor(context, R.color.gold_light));

        int value = 5;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChipView);
            value = a.getInt(R.styleable.ChipView_chipAmount, 5);
            a.recycle();
        }
        setAmount(value);
        setClickable(true);
        setFocusable(true);
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.label = String.valueOf(amount);
        setContentDescription(getContext().getString(R.string.chip_description, amount));
        invalidate();
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void setSelected(boolean selected) {
        boolean changed = selected != isSelected();
        super.setSelected(selected);
        if (changed) {
            animate().translationY(selected ? -6f * density : 0f)
                    .scaleX(selected ? 1.06f : 1f).scaleY(selected ? 1.06f : 1f)
                    .setDuration(160L).start();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas c) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float r = Math.min(getWidth(), getHeight()) / 2f * 0.8f;
        painter.drawChip(c, cx, cy, r, painter.colorFor(amount), label);
        if (isSelected()) {
            ringPaint.setStrokeWidth(r * 0.1f);
            c.drawCircle(cx, cy, r * 1.12f, ringPaint);
        }
        if (isPressed()) {
            c.drawCircle(cx, cy, r, ringPaint);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}
