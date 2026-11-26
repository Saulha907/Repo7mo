package com.example.blackjack21.view.paint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import androidx.core.content.ContextCompat;

import com.example.blackjack21.R;

/**
 * Dibuja fichas de casino: cara superior con franjas y pilas en perspectiva.
 * La cara se dibuja a un radio unitario fijo y se escala con el Canvas, de modo
 * que los efectos de trazo se crean una sola vez.
 */
public final class ChipPainter {

    /** Relación vertical de la elipse vista en perspectiva. */
    private static final float PERSPECTIVE = 0.42f;
    /** Grosor de cada ficha respecto al radio. */
    private static final float THICKNESS = 0.17f;
    private static final int STRIPES = 8;
    private static final float UNIT = 100f;
    /** Aplastamiento vertical del valor escrito sobre una pila. */
    private static final float LABEL_SQUASH = 0.72f;

    private final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint seamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stripeFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stripeRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stackText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stackOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    private final int red;
    private final int green;
    private final int black;
    private final int purple;
    private final int blue;

    public ChipPainter(Context context) {
        int stripe = ContextCompat.getColor(context, R.color.chip_stripe);
        red = ContextCompat.getColor(context, R.color.chip_red);
        green = ContextCompat.getColor(context, R.color.chip_green);
        black = ContextCompat.getColor(context, R.color.chip_black);
        purple = ContextCompat.getColor(context, R.color.chip_purple);
        blue = ContextCompat.getColor(context, R.color.chip_blue);

        stripeFill.setColor(stripe);

        float stripeRadius = UNIT * 0.86f;
        float seg = (float) (2 * Math.PI * stripeRadius) / (STRIPES * 2);
        stripeRing.setStyle(Paint.Style.STROKE);
        stripeRing.setColor(stripe);
        stripeRing.setStrokeWidth(UNIT * 0.2f);
        stripeRing.setPathEffect(new DashPathEffect(new float[]{seg, seg}, 0f));

        innerRing.setStyle(Paint.Style.STROKE);
        innerRing.setColor(stripe);
        innerRing.setStrokeWidth(UNIT * 0.04f);
        innerRing.setPathEffect(new DashPathEffect(new float[]{UNIT * 0.08f, UNIT * 0.06f}, 0f));

        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(UNIT * 0.03f);
        seamPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setColor(ContextCompat.getColor(context, R.color.shadow));
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        textPaint.setTextSize(UNIT * 0.5f);
        // Contorno oscuro bajo el valor: legible sobre cualquier color de ficha.
        textOutline.set(textPaint);
        textOutline.setStyle(Paint.Style.STROKE);
        textOutline.setStrokeJoin(Paint.Join.ROUND);
        textOutline.setStrokeWidth(UNIT * 0.07f);
        textOutline.setColor(0x99000000);

        glossPaint.setStyle(Paint.Style.STROKE);
        glossPaint.setStrokeCap(Paint.Cap.ROUND);
        glossPaint.setStrokeWidth(UNIT * 0.07f);
        glossPaint.setColor(0x40FFFFFF);

        stackText.setColor(Color.WHITE);
        stackText.setTextAlign(Paint.Align.CENTER);
        stackText.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        stackOutline.set(stackText);
        stackOutline.setStyle(Paint.Style.STROKE);
        stackOutline.setStrokeJoin(Paint.Join.ROUND);
        stackOutline.setColor(0xB3000000);
    }

    /** Color clásico según la denominación (5, 25, 100, 500). */
    public int colorFor(int value) {
        switch (value) {
            case 5:
                return red;
            case 25:
                return green;
            case 100:
                return black;
            case 500:
                return purple;
            default:
                return blue;
        }
    }

    /**
     * Pila de fichas en perspectiva.
     *
     * @param baseY coordenada Y del centro de la base de la pila.
     */
    public void drawStack(Canvas c, float cx, float baseY, float r, int count, int color, String label) {
        float t = r * THICKNESS;
        float ry = r * PERSPECTIVE;
        boolean dim = isDark(color);
        int dark = dim ? lighten(color, 0.22f) : shade(color, 0.62f);

        oval.set(cx - r * 1.04f, baseY - ry * 0.8f, cx + r * 1.14f, baseY + ry * 1.2f);
        c.drawOval(oval, shadowPaint);

        sidePaint.setColor(dark);
        seamPaint.setColor(dim ? lighten(color, 0.45f) : shade(color, 0.4f));
        seamPaint.setStrokeWidth(Math.max(1f, r * 0.02f));

        float top = baseY;
        for (int i = 0; i < count; i++) {
            top = baseY - (i + 1) * t;
            oval.set(cx - r, top + t - ry, cx + r, top + t + ry);
            c.drawOval(oval, sidePaint);
            c.drawRect(cx - r, top, cx + r, top + t, sidePaint);
            c.drawArc(oval, 0f, 180f, false, seamPaint);

            for (int k = -2; k <= 2; k++) {
                double ang = k * 0.62;
                float sx = cx + (float) (r * Math.sin(ang));
                float sw = (float) (r * 0.17f * Math.cos(ang));
                float dy = (float) (ry * Math.cos(ang));
                c.drawRect(sx - sw / 2f, top + dy + t * 0.12f, sx + sw / 2f, top + dy + t * 0.92f, stripeFill);
            }
        }

        c.save();
        c.translate(cx, top);
        c.scale(1f, PERSPECTIVE);
        drawFace(c, 0f, 0f, r, color, "");
        c.restore();

        // El valor se escribe aparte con un aplastamiento leve: en perspectiva
        // completa (0.42) resultaría ilegible en fichas pequeñas.
        if (label != null && !label.isEmpty()) {
            float size = r * (label.length() >= 3 ? 0.5f : 0.62f);
            stackText.setTextSize(size);
            stackOutline.setTextSize(size);
            stackOutline.setStrokeWidth(Math.max(1f, size * 0.16f));
            c.save();
            c.translate(cx, top);
            c.scale(1f, LABEL_SQUASH);
            float baseline = -(stackText.ascent() + stackText.descent()) / 2f;
            c.drawText(label, 0f, baseline, stackOutline);
            c.drawText(label, 0f, baseline, stackText);
            c.restore();
        }
    }

    /** Ficha suelta vista desde arriba, con sombra. */
    public void drawChip(Canvas c, float cx, float cy, float r, int color, String label) {
        c.drawCircle(cx + r * 0.06f, cy + r * 0.1f, r * 1.02f, shadowPaint);
        drawFace(c, cx, cy, r, color, label);
    }

    /** Cara superior de una ficha vista de frente. */
    public void drawFace(Canvas c, float cx, float cy, float r, int color, String label) {
        c.save();
        c.translate(cx, cy);
        float s = r / UNIT;
        c.scale(s, s);

        bodyPaint.setColor(color);
        c.drawCircle(0f, 0f, UNIT, bodyPaint);
        c.drawCircle(0f, 0f, UNIT * 0.86f, stripeRing);

        boolean dim = isDark(color);
        bodyPaint.setColor(dim ? lighten(color, 0.12f) : shade(color, 1.12f));
        c.drawCircle(0f, 0f, UNIT * 0.62f, bodyPaint);
        c.drawCircle(0f, 0f, UNIT * 0.62f, innerRing);

        edgePaint.setColor(dim ? 0xFFB8B8B8 : shade(color, 0.55f));
        c.drawCircle(0f, 0f, UNIT, edgePaint);

        oval.set(-UNIT * 0.93f, -UNIT * 0.93f, UNIT * 0.93f, UNIT * 0.93f);
        c.drawArc(oval, 200f, 75f, false, glossPaint);

        if (label != null && !label.isEmpty()) {
            float baseline = -(textPaint.ascent() + textPaint.descent()) / 2f;
            c.drawText(label, 0f, baseline, textOutline);
            c.drawText(label, 0f, baseline, textPaint);
        }
        c.restore();
    }

    /** Fichas muy oscuras (la negra de 100) necesitan detalles claros para destacar. */
    private static boolean isDark(int color) {
        float lum = (0.299f * Color.red(color) + 0.587f * Color.green(color)
                + 0.114f * Color.blue(color)) / 255f;
        return lum < 0.18f;
    }

    private static int lighten(int color, float f) {
        int r = Color.red(color) + (int) ((255 - Color.red(color)) * f);
        int g = Color.green(color) + (int) ((255 - Color.green(color)) * f);
        int b = Color.blue(color) + (int) ((255 - Color.blue(color)) * f);
        return Color.argb(Color.alpha(color), r, g, b);
    }

    private static int shade(int color, float f) {
        int r = Math.min(255, (int) (Color.red(color) * f));
        int g = Math.min(255, (int) (Color.green(color) * f));
        int b = Math.min(255, (int) (Color.blue(color) * f));
        return Color.argb(Color.alpha(color), r, g, b);
    }
}
