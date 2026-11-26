package com.example.blackjack21.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.blackjack21.R;

/**
 * Fondo de casino: tapete verde con degradado radial, trama sutil, viñeta
 * y marco dorado doble que respeta los recortes de pantalla.
 */
public class CasinoBackgroundView extends View {

    private final Paint feltPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint patternPaint = new Paint();
    private final Paint vignettePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint frameOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint frameInner = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ornamentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF outerRect = new RectF();
    private final RectF innerRect = new RectF();
    private final Path ornaments = new Path();
    private final Rect cutout = new Rect();

    private final float density;
    private final float frameMargin;
    private final int feltLight;
    private final int felt;
    private final int feltDark;
    private final int background;
    private final int goldLight;
    private final int gold;
    private final int goldDark;

    private Bitmap patternTile;

    public CasinoBackgroundView(Context context) {
        this(context, null);
    }

    public CasinoBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        frameMargin = getResources().getDimension(R.dimen.frame_margin);

        feltLight = ContextCompat.getColor(context, R.color.table_felt_light);
        felt = ContextCompat.getColor(context, R.color.table_felt);
        feltDark = ContextCompat.getColor(context, R.color.table_felt_dark);
        background = ContextCompat.getColor(context, R.color.background_dark);
        goldLight = ContextCompat.getColor(context, R.color.gold_light);
        gold = ContextCompat.getColor(context, R.color.gold);
        goldDark = ContextCompat.getColor(context, R.color.gold_dark);

        frameOuter.setStyle(Paint.Style.STROKE);
        frameOuter.setStrokeWidth(2f * density);
        frameInner.setStyle(Paint.Style.STROKE);
        frameInner.setStrokeWidth(1f * density);
        frameInner.setColor(gold);
        frameInner.setAlpha(150);
        ornamentPaint.setColor(gold);

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            Insets c = insets.getInsets(WindowInsetsCompat.Type.displayCutout());
            cutout.set(c.left, c.top, c.right, c.bottom);
            updateFrame(getWidth(), getHeight());
            invalidate();
            return insets;
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) {
            return;
        }
        float radius = Math.max(w, h) * 0.75f;
        feltPaint.setShader(new RadialGradient(w * 0.5f, h * 0.42f, radius,
                new int[]{feltLight, felt, feltDark, background},
                new float[]{0f, 0.4f, 0.75f, 1f}, Shader.TileMode.CLAMP));

        vignettePaint.setShader(new RadialGradient(w * 0.5f, h * 0.5f, radius,
                new int[]{0x00000000, 0x00000000, 0x8C000000},
                new float[]{0f, 0.6f, 1f}, Shader.TileMode.CLAMP));

        if (patternTile == null) {
            patternTile = createPatternTile();
            patternPaint.setShader(new BitmapShader(patternTile,
                    Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        }
        updateFrame(w, h);
    }

    private Bitmap createPatternTile() {
        int size = Math.max(8, Math.round(18 * density));
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1f, density * 0.6f));
        p.setColor(0x12FFFFFF);
        Path d = new Path();
        float m = size / 2f;
        d.moveTo(m, 0);
        d.lineTo(size, m);
        d.lineTo(m, size);
        d.lineTo(0, m);
        d.close();
        c.drawPath(d, p);
        return bmp;
    }

    private void updateFrame(int w, int h) {
        if (w == 0 || h == 0) {
            return;
        }
        outerRect.set(cutout.left + frameMargin, cutout.top + frameMargin,
                w - cutout.right - frameMargin, h - cutout.bottom - frameMargin);
        float gap = 5f * density;
        innerRect.set(outerRect.left + gap, outerRect.top + gap,
                outerRect.right - gap, outerRect.bottom - gap);

        frameOuter.setShader(new LinearGradient(outerRect.left, outerRect.top,
                outerRect.right, outerRect.bottom,
                new int[]{goldLight, gold, goldDark, gold, goldLight}, null,
                Shader.TileMode.CLAMP));

        ornaments.reset();
        float s = 6f * density;
        addDiamond(outerRect.left, outerRect.top, s);
        addDiamond(outerRect.right, outerRect.top, s);
        addDiamond(outerRect.left, outerRect.bottom, s);
        addDiamond(outerRect.right, outerRect.bottom, s);
        addDiamond(outerRect.centerX(), outerRect.top, s * 0.8f);
        addDiamond(outerRect.centerX(), outerRect.bottom, s * 0.8f);
    }

    private void addDiamond(float cx, float cy, float s) {
        ornaments.moveTo(cx, cy - s);
        ornaments.lineTo(cx + s, cy);
        ornaments.lineTo(cx, cy + s);
        ornaments.lineTo(cx - s, cy);
        ornaments.close();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        canvas.drawRect(0, 0, w, h, feltPaint);
        canvas.drawRect(0, 0, w, h, patternPaint);
        canvas.drawRect(0, 0, w, h, vignettePaint);

        float r = 14f * density;
        canvas.drawRoundRect(outerRect, r, r, frameOuter);
        canvas.drawRoundRect(innerRect, r * 0.7f, r * 0.7f, frameInner);
        canvas.drawPath(ornaments, ornamentPaint);
    }
}
