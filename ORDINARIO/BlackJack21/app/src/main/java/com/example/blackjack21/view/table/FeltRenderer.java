package com.example.blackjack21.view.table;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import androidx.core.content.ContextCompat;

import com.example.blackjack21.R;
import com.example.blackjack21.view.paint.CardPainter;

/**
 * Parte estática de la mesa: madera, tapete, ribetes dorados, bandeja de
 * fichas, zapato de cartas y texto curvo. Se dibuja una vez en un bitmap.
 */
final class FeltRenderer {

    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint railPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint railHighlight = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint feltPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint patternPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goldStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goldThin = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goldFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint slotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shoePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF tmp = new RectF();
    private final Path path = new Path();
    private final CardPainter cardPainter;

    private final int feltLight;
    private final int felt;
    private final int feltDark;
    private final int rail;
    private final int railDark;
    private final int goldLight;
    private final int gold;
    private final int goldDark;
    private final int[] trayColors;

    FeltRenderer(Context context, CardPainter cardPainter) {
        this.cardPainter = cardPainter;
        feltLight = ContextCompat.getColor(context, R.color.table_felt_light);
        felt = ContextCompat.getColor(context, R.color.table_felt);
        feltDark = ContextCompat.getColor(context, R.color.table_felt_dark);
        rail = ContextCompat.getColor(context, R.color.table_rail);
        railDark = ContextCompat.getColor(context, R.color.table_rail_dark);
        goldLight = ContextCompat.getColor(context, R.color.gold_light);
        gold = ContextCompat.getColor(context, R.color.gold);
        goldDark = ContextCompat.getColor(context, R.color.gold_dark);
        int purple = ContextCompat.getColor(context, R.color.chip_purple);
        int black = ContextCompat.getColor(context, R.color.chip_black);
        int green = ContextCompat.getColor(context, R.color.chip_green);
        int red = ContextCompat.getColor(context, R.color.chip_red);
        trayColors = new int[]{purple, black, green, red, red, green, black, purple};

        shadowPaint.setColor(0x40000000);
        railHighlight.setStyle(Paint.Style.STROKE);
        railHighlight.setColor(0x33FFFFFF);
        innerShadow.setStyle(Paint.Style.STROKE);
        innerShadow.setColor(0x59000000);
        goldStroke.setStyle(Paint.Style.STROKE);
        goldThin.setStyle(Paint.Style.STROKE);
        goldThin.setColor(gold);
        goldThin.setAlpha(140);
        goldFill.setColor(gold);
        textPaint.setColor(goldLight);
        textPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        textPaint.setLetterSpacing(0.1f);
        textPaint.setShadowLayer(3f, 0f, 2f, 0x99000000);
        trayPaint.setColor(0xFF1A0E07);
        linePaint.setStyle(Paint.Style.STROKE);
        shoePaint.setColor(0xFF2A1A12);
    }

    void draw(Canvas c, TableGeometry g, String paysText, float density) {
        prepareShaders(g, density);

        // Sombra exterior de la mesa.
        for (int i = 1; i <= 4; i++) {
            c.save();
            c.translate(0f, g.base * 0.006f * i);
            c.drawPath(g.tableShape, shadowPaint);
            c.restore();
        }

        // Baranda de madera acolchada.
        c.drawPath(g.tableShape, railPaint);
        railHighlight.setStrokeWidth(g.railThickness * 0.18f);
        tmp.set(g.tableOval);
        float hi = g.railThickness * 0.35f;
        tmp.inset(hi, hi);
        c.drawArc(tmp, 10f, 160f, false, railHighlight);

        // Tapete con textura y sombra interior.
        c.drawPath(g.feltShape, feltPaint);
        c.drawPath(g.feltShape, patternPaint);
        c.save();
        c.clipPath(g.feltShape);
        innerShadow.setStrokeWidth(g.railThickness * 0.7f);
        c.drawPath(g.feltArc, innerShadow);
        c.drawLine(0f, g.top, g.width, g.top, innerShadow);
        c.restore();

        // Ribetes dorados.
        goldStroke.setStrokeWidth(Math.max(2f, g.base * 0.008f));
        c.drawPath(g.feltArc, goldStroke);
        goldThin.setStrokeWidth(Math.max(1f, g.base * 0.0035f));
        c.drawPath(g.trimArc, goldThin);
        c.drawLine(g.tableOval.left, g.top, g.tableOval.right, g.top, goldStroke);

        drawTray(c, g);
        drawShoe(c, g);
        drawPaysText(c, g, paysText);
    }

    private void prepareShaders(TableGeometry g, float density) {
        railPaint.setShader(new RadialGradient(g.cx, g.top, Math.max(g.tableOval.width() / 2f, 1f),
                new int[]{rail, rail, railDark}, new float[]{0f, 0.85f, 1f}, Shader.TileMode.CLAMP));
        float radius = Math.max(g.feltOval.width() * 0.6f, 1f);
        feltPaint.setShader(new RadialGradient(g.cx, g.top + g.feltOval.height() * 0.22f, radius,
                new int[]{feltLight, felt, feltDark}, new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        goldStroke.setShader(new LinearGradient(g.tableOval.left, 0f, g.tableOval.right, 0f,
                new int[]{goldDark, goldLight, gold, goldLight, goldDark}, null, Shader.TileMode.CLAMP));
        patternPaint.setShader(createPattern(density));
    }

    private static BitmapShader createPattern(float density) {
        int size = Math.max(6, Math.round(6 * density));
        Bitmap tile = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(tile);
        Paint p = new Paint();
        p.setColor(0x0DFFFFFF);
        c.drawPoint(0, 0, p);
        c.drawPoint(size / 2f, size / 2f, p);
        p.setColor(0x0A000000);
        c.drawPoint(size / 2f, 0, p);
        c.drawPoint(0, size / 2f, p);
        return new BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    }

    private void drawTray(Canvas c, TableGeometry g) {
        RectF r = g.trayRect;
        float radius = r.height() * 0.18f;
        tmp.set(r);
        tmp.offset(0f, g.base * 0.006f);
        c.drawRoundRect(tmp, radius, radius, shadowPaint);
        c.drawRoundRect(r, radius, radius, trayPaint);

        int n = trayColors.length;
        float pad = r.height() * 0.14f;
        float slotW = (r.width() - pad * 2f) / n;
        for (int i = 0; i < n; i++) {
            float l = r.left + pad + i * slotW + slotW * 0.08f;
            float rr = l + slotW * 0.84f;
            tmp.set(l, r.top + pad, rr, r.bottom - pad);
            slotPaint.setColor(trayColors[i]);
            c.drawRoundRect(tmp, slotW * 0.2f, slotW * 0.2f, slotPaint);
            // Cantos de fichas apiladas.
            linePaint.setColor(0x55000000);
            linePaint.setStrokeWidth(Math.max(1f, g.base * 0.002f));
            float step = tmp.width() / 7f;
            for (float x = tmp.left + step; x < tmp.right - step * 0.5f; x += step) {
                c.drawLine(x, tmp.top, x, tmp.bottom, linePaint);
            }
            linePaint.setColor(0xCCF5F1E6);
            float midY = tmp.centerY();
            c.drawLine(tmp.left + step * 0.5f, midY, tmp.right - step * 0.5f, midY, linePaint);
        }
        goldThin.setStrokeWidth(Math.max(1f, g.base * 0.003f));
        c.drawRoundRect(r, radius, radius, goldThin);
    }

    private void drawShoe(Canvas c, TableGeometry g) {
        float w = g.cardW * 1.3f;
        float h = g.cardH * 0.95f;
        c.save();
        c.translate(g.shoeCenter.x, g.shoeCenter.y);
        c.rotate(-12f);

        // Carta asomando por la boca del zapato.
        tmp.set(-w / 2f - g.cardW * 0.42f, -g.cardH * 0.42f,
                -w / 2f + g.cardW * 0.58f, g.cardH * 0.58f);
        cardPainter.drawBack(c, tmp);

        tmp.set(-w / 2f, -h / 2f, w / 2f, h / 2f);
        float radius = w * 0.08f;
        RectF shadow = new RectF(tmp);
        shadow.offset(g.base * 0.005f, g.base * 0.008f);
        c.drawRoundRect(shadow, radius, radius, shadowPaint);
        shoePaint.setShader(new LinearGradient(0f, tmp.top, 0f, tmp.bottom,
                0xFF4A2E1E, 0xFF1E120B, Shader.TileMode.CLAMP));
        c.drawRoundRect(tmp, radius, radius, shoePaint);
        goldThin.setStrokeWidth(Math.max(1f, g.base * 0.004f));
        c.drawRoundRect(tmp, radius, radius, goldThin);
        // Placa frontal.
        tmp.inset(w * 0.18f, h * 0.3f);
        c.drawRoundRect(tmp, radius * 0.5f, radius * 0.5f, goldThin);
        c.restore();
    }

    private void drawPaysText(Canvas c, TableGeometry g, String text) {
        textPaint.setTextSize(g.textSize);
        path.reset();
        path.arcTo(g.textOval, 160f, -140f, true);
        PathMeasure measure = new PathMeasure(path, false);
        float length = measure.getLength();
        float textWidth = textPaint.measureText(text);
        float hOffset = (length - textWidth) / 2f;
        c.drawTextOnPath(text, path, hOffset, 0f, textPaint);

        // Línea decorativa bajo el texto, con rombos en los extremos.
        float sweep = Math.min(140f, (textWidth / length) * 140f * 1.08f);
        tmp.set(g.textOval);
        float off = g.textSize * 0.55f;
        tmp.inset(-off * 1.3f, -off);
        goldThin.setStrokeWidth(Math.max(1f, g.base * 0.003f));
        c.drawArc(tmp, 90f - sweep / 2f, sweep, false, goldThin);
        for (int s = -1; s <= 1; s += 2) {
            double rad = Math.toRadians(90f + s * sweep / 2f);
            float x = tmp.centerX() + (float) (tmp.width() / 2f * Math.cos(rad));
            float y = tmp.centerY() + (float) (tmp.height() / 2f * Math.sin(rad));
            float d = g.base * 0.008f;
            path.reset();
            path.moveTo(x, y - d);
            path.lineTo(x + d, y);
            path.lineTo(x, y + d);
            path.lineTo(x - d, y);
            path.close();
            c.drawPath(path, goldFill);
        }
    }
}
