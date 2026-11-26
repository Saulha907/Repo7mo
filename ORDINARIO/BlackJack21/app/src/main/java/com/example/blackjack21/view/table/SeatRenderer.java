package com.example.blackjack21.view.table;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.blackjack21.R;
import com.example.blackjack21.view.paint.ChipPainter;

/**
 * Elementos de un asiento: círculo de apuesta, fichas apostadas, placa con
 * nombre/saldo/apuesta, marcador de puntuación y resaltado de turno.
 */
final class SeatRenderer {

    private final Paint spotFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spotStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint plateFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint plateStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nameText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint infoText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint badgeFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint badgeText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tmp = new RectF();

    private final ChipPainter chipPainter;
    /** Tamaños mínimos legibles (px) para pantallas pequeñas. */
    private final float minName;
    private final float minInfo;
    private final float minBadge;
    private final float minLabel;
    private final int gold;
    private final int goldLight;
    private final int white;
    private final int secondary;

    SeatRenderer(Context context, ChipPainter chipPainter) {
        this.chipPainter = chipPainter;
        gold = ContextCompat.getColor(context, R.color.gold);
        goldLight = ContextCompat.getColor(context, R.color.gold_light);
        white = ContextCompat.getColor(context, R.color.text_primary);
        secondary = ContextCompat.getColor(context, R.color.text_secondary);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        minName = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10.5f, dm);
        minInfo = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9f, dm);
        minBadge = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9.5f, dm);
        minLabel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8.5f, dm);

        spotFill.setColor(0x26000000);
        spotStroke.setStyle(Paint.Style.STROKE);
        spotStroke.setColor(gold);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setColor(goldLight);
        plateStroke.setStyle(Paint.Style.STROKE);

        Typeface serifBold = Typeface.create(Typeface.SERIF, Typeface.BOLD);
        nameText.setTypeface(serifBold);
        nameText.setTextAlign(Paint.Align.CENTER);
        infoText.setTextAlign(Paint.Align.CENTER);
        infoText.setColor(secondary);
        badgeFill.setColor(0xE6101010);
        badgeText.setTypeface(Typeface.DEFAULT_BOLD);
        badgeText.setTextAlign(Paint.Align.CENTER);
        badgeText.setColor(white);
    }

    /** Círculo de apuesta con resaltado pulsante opcional. */
    void drawSpot(Canvas c, float x, float y, float r, boolean occupied, boolean active, float pulse) {
        c.drawCircle(x, y, r, spotFill);
        spotStroke.setStrokeWidth(Math.max(1.5f, r * 0.07f));
        spotStroke.setAlpha(occupied ? 230 : 110);
        c.drawCircle(x, y, r, spotStroke);
        spotStroke.setStrokeWidth(Math.max(1f, r * 0.03f));
        c.drawCircle(x, y, r * 0.8f, spotStroke);

        if (active) {
            glowPaint.setStrokeWidth(r * 0.14f);
            glowPaint.setAlpha((int) (90 + 140 * pulse));
            c.drawCircle(x, y, r * (1.18f + 0.1f * pulse), glowPaint);
        }
    }

    /** Fichas apostadas sobre el círculo: una pila por denominación. */
    void drawBetChips(Canvas c, float x, float y, float spotR, int[] values, int[] counts, int stacks,
                      String[] labels) {
        if (stacks == 0) {
            return;
        }
        float r = spotR * (stacks > 1 ? 0.5f : 0.66f);
        float spacing = r * 1.9f;
        float start = x - (stacks - 1) * spacing / 2f;
        for (int i = 0; i < stacks; i++) {
            chipPainter.drawStack(c, start + i * spacing, y + r * 0.45f, r, counts[i],
                    chipPainter.colorFor(values[i]), labels[i]);
        }
    }

    /** Placa con nombre y datos; {@code line1}/{@code line2} pueden ser null. */
    void drawPlate(Canvas c, RectF plate, String name, @Nullable String line1, @Nullable String line2,
                   boolean human, boolean active, float pulse, float base) {
        float radius = plate.height() * 0.2f;

        if (active) {
            float grow = base * 0.006f * (1f + pulse);
            tmp.set(plate);
            tmp.inset(-grow, -grow);
            glowPaint.setStrokeWidth(base * 0.006f);
            glowPaint.setAlpha((int) (60 + 120 * pulse));
            c.drawRoundRect(tmp, radius + grow, radius + grow, glowPaint);
        }

        plateFill.setColor(human ? 0xE0221B08 : 0xD9081A10);
        c.drawRoundRect(plate, radius, radius, plateFill);
        plateStroke.setColor(gold);
        plateStroke.setAlpha(human ? 255 : 150);
        plateStroke.setStrokeWidth(Math.max(1f, base * (active ? 0.005f : 0.0028f)));
        c.drawRoundRect(plate, radius, radius, plateStroke);

        int lines = (line1 != null ? 1 : 0) + (line2 != null ? 1 : 0);
        float nameSize = Math.max(base * 0.033f, minName);
        float infoSize = Math.max(base * 0.026f, minInfo);
        float maxW = plate.width() * 0.88f;
        float contentH = nameSize + lines * infoSize * 1.25f;
        float limit = plate.height() * 0.9f;
        if (contentH > limit) {
            float k = limit / contentH;
            nameSize *= k;
            infoSize *= k;
            contentH = limit;
        }
        float y = plate.centerY() - contentH / 2f;

        nameText.setColor(human ? goldLight : white);
        fitText(nameText, name, nameSize, maxW);
        y += nameSize;
        c.drawText(name, plate.centerX(), y - nameText.descent() * 0.6f, nameText);

        if (line1 != null) {
            fitText(infoText, line1, infoSize, maxW);
            y += infoSize * 1.25f;
            c.drawText(line1, plate.centerX(), y - infoText.descent() * 0.5f, infoText);
        }
        if (line2 != null) {
            fitText(infoText, line2, infoSize, maxW);
            y += infoSize * 1.25f;
            c.drawText(line2, plate.centerX(), y - infoText.descent() * 0.5f, infoText);
        }
    }

    /** Marcador redondeado con la puntuación. */
    void drawBadge(Canvas c, float cx, float cy, String text, float base, boolean highlight) {
        drawBadge(c, cx, cy, text, base, highlight, Float.MAX_VALUE);
    }

    /** @param maxW ancho máximo (manos divididas contiguas); se reduce si no cabe. */
    void drawBadge(Canvas c, float cx, float cy, String text, float base, boolean highlight,
                   float maxW) {
        float size = badgeSize(base);
        float w = badgeWidth(text, base);
        if (w > maxW) {
            size *= maxW / w;
            w = maxW;
        }
        badgeText.setTextSize(size);
        float h = size * 1.5f;
        tmp.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
        c.drawRoundRect(tmp, h / 2f, h / 2f, badgeFill);
        plateStroke.setColor(highlight ? goldLight : gold);
        plateStroke.setAlpha(255);
        plateStroke.setStrokeWidth(Math.max(1f, base * 0.003f));
        c.drawRoundRect(tmp, h / 2f, h / 2f, plateStroke);
        float baseline = cy - (badgeText.ascent() + badgeText.descent()) / 2f;
        c.drawText(text, cx, baseline, badgeText);
    }

    float badgeWidth(String text, float base) {
        float size = badgeSize(base);
        badgeText.setTextSize(size);
        return Math.max(badgeText.measureText(text) + size, size * 1.83f);
    }

    private float badgeSize(float base) {
        return Math.max(base * 0.03f, minBadge);
    }

    /** Ancho de una etiqueta de resultado (para colocarla junto a otros elementos). */
    float resultWidth(String text, float base, float scale) {
        float size = Math.max(base * 0.028f, minLabel) * scale;
        badgeText.setTextSize(size);
        return badgeText.measureText(text) + size * 1.35f;
    }

    /** Etiqueta de resultado (GANA, PIERDE…) con color de fondo. */
    void drawResult(Canvas c, float cx, float cy, String text, int fillColor, int textColor,
                    float base, float scale) {
        drawResult(c, cx, cy, text, fillColor, textColor, base, scale, Float.MAX_VALUE);
    }

    /** @param maxW ancho máximo (manos divididas contiguas); el texto se reduce si no cabe. */
    void drawResult(Canvas c, float cx, float cy, String text, int fillColor, int textColor,
                    float base, float scale, float maxW) {
        float size = Math.max(base * 0.028f, minLabel) * scale;
        badgeText.setTextSize(size);
        float w = badgeText.measureText(text) + size * 1.35f;
        if (w > maxW) {
            size *= maxW / w;
            badgeText.setTextSize(size);
            w = maxW;
        }
        float h = size * 1.5f;
        tmp.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
        int previous = badgeFill.getColor();
        badgeFill.setColor(fillColor);
        c.drawRoundRect(tmp, h * 0.3f, h * 0.3f, badgeFill);
        badgeFill.setColor(previous);
        plateStroke.setColor(goldLight);
        plateStroke.setAlpha(220);
        plateStroke.setStrokeWidth(Math.max(1f, base * 0.0025f));
        c.drawRoundRect(tmp, h * 0.3f, h * 0.3f, plateStroke);
        int previousText = badgeText.getColor();
        badgeText.setColor(textColor);
        float baseline = cy - (badgeText.ascent() + badgeText.descent()) / 2f;
        c.drawText(text, cx, baseline, badgeText);
        badgeText.setColor(previousText);
    }

    /** Apuesta de una mano dividida: ficha en miniatura + importe. */
    void drawBetTag(Canvas c, float cx, float cy, String text, int chipColor, float base, float scale,
                    float maxW) {
        float size = Math.max(base * 0.026f, minLabel) * scale;
        badgeText.setTextSize(size);
        float h = size * 1.55f;
        float chipR = h * 0.36f;
        float textW = badgeText.measureText(text);
        float w = textW + chipR * 2f + h * 0.8f;
        boolean showChip = true;
        if (w > maxW) {
            // Primero se prescinde de la ficha; si aún no cabe, se reduce el texto.
            showChip = false;
            w = textW + h * 0.7f;
            if (w > maxW) {
                size *= maxW / w;
                badgeText.setTextSize(size);
                textW = badgeText.measureText(text);
                w = maxW;
            }
        }
        tmp.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
        c.drawRoundRect(tmp, h / 2f, h / 2f, badgeFill);
        plateStroke.setColor(gold);
        plateStroke.setAlpha(200);
        plateStroke.setStrokeWidth(Math.max(1f, base * 0.0025f));
        c.drawRoundRect(tmp, h / 2f, h / 2f, plateStroke);
        float baseline = cy - (badgeText.ascent() + badgeText.descent()) / 2f;
        if (showChip) {
            float chipX = tmp.left + h * 0.3f + chipR;
            chipPainter.drawFace(c, chipX, cy, chipR, chipColor, "");
            c.drawText(text, chipX + chipR + h * 0.15f + textW / 2f, baseline, badgeText);
        } else {
            c.drawText(text, cx, baseline, badgeText);
        }
    }

    /** Marco dorado pulsante alrededor de la mano activa (manos divididas). */
    void drawHandFrame(Canvas c, RectF hand, float pulse, float base) {
        float pad = base * 0.01f;
        tmp.set(hand.left - pad, hand.top - pad, hand.right + pad, hand.bottom + pad);
        glowPaint.setStrokeWidth(Math.max(2f, base * 0.006f));
        glowPaint.setAlpha((int) (120 + 120 * pulse));
        float r = base * 0.015f;
        c.drawRoundRect(tmp, r, r, glowPaint);
    }

    private static void fitText(Paint p, String text, float size, float maxW) {
        p.setTextSize(size);
        float w = p.measureText(text);
        if (w > maxW) {
            p.setTextSize(size * maxW / w);
        }
    }
}
