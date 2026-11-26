package com.example.blackjack21.view.table;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Geometría proporcional de la mesa. Se recalcula solo cuando cambia el tamaño;
 * todas las medidas derivan de {@link #base} para escalar en cualquier pantalla.
 */
final class TableGeometry {

    /** Ángulos (grados) de los asientos sobre la elipse de la mesa: 90 = abajo. */
    static final int SEAT_COUNT = 5;
    static final int SEAT_PLAYER = 2;
    /** Asientos para bots en el orden de {@code TableSnapshot.bots}. */
    static final int[] BOT_SEATS = {0, 1, 3};
    private static final float[] SEAT_ANGLES = {157f, 123f, 90f, 57f, 23f};

    static final float CARD_RATIO = 1.42f;
    /** Profundidad máxima de la mesa respecto a su anchura (evita una "U" alargada). */
    static final float MAX_DEPTH_RATIO = 0.9f;

    float width;
    float height;
    float base;
    float cx;
    float top;
    float railThickness;

    final RectF tableOval = new RectF();
    final RectF feltOval = new RectF();
    final RectF trimOval = new RectF();
    final Path tableShape = new Path();
    final Path feltShape = new Path();
    final Path feltArc = new Path();
    final Path trimArc = new Path();

    // Elementos del crupier.
    final RectF trayRect = new RectF();
    final PointF dealerHand = new PointF();
    final PointF shoeCenter = new PointF();

    // Texto curvo.
    final RectF textOval = new RectF();
    float textSize;

    // Asientos.
    final PointF[] seats = new PointF[SEAT_COUNT];
    final float[] seatTilt = new float[SEAT_COUNT];

    // Tamaños de elementos.
    float cardW;
    float cardH;
    float playerCardScale = 1.15f;
    float spotRadius;
    float plateW;
    float plateH;
    float gap;
    float botHandsMaxWidth;
    float playerHandsMaxWidth;
    float dealerHandsMaxWidth;

    TableGeometry() {
        for (int i = 0; i < SEAT_COUNT; i++) {
            seats[i] = new PointF();
        }
    }

    void update(int w, int h) {
        width = w;
        height = h;
        base = Math.min(w / 1.55f, h);
        float margin = base * 0.015f;
        cx = w / 2f;

        float left = margin;
        float right = w - margin;
        // En vertical (tabletas que ignoran el bloqueo de orientación) la mesa no se
        // estira: conserva la proporción semicircular y se centra en el alto disponible.
        float maxRy = (right - left) * MAX_DEPTH_RATIO;
        float ry = Math.min(h - margin * 2f, maxRy);
        top = margin + Math.max(0f, (h - margin * 2f - ry) / 2f);
        railThickness = base * 0.05f;
        float t = railThickness;

        tableOval.set(left, top - ry, right, top + ry);
        feltOval.set(left + t, top - (ry - t), right - t, top + (ry - t));
        float inset = base * 0.018f;
        trimOval.set(feltOval.left + inset, feltOval.top + inset,
                feltOval.right - inset, feltOval.bottom - inset);

        buildShape(tableShape, tableOval, left, right);
        buildShape(feltShape, feltOval, left + t, right - t);
        feltArc.reset();
        feltArc.arcTo(feltOval, 0f, 180f, true);
        trimArc.reset();
        trimArc.arcTo(trimOval, 0f, 180f, true);

        cardH = base * 0.16f;
        cardW = cardH / CARD_RATIO;
        spotRadius = base * 0.045f;
        plateW = base * 0.25f;
        plateH = base * 0.135f;
        gap = base * 0.012f;

        // Bandeja de fichas y cartas del crupier.
        float trayW = base * 0.6f;
        float trayH = base * 0.075f;
        trayRect.set(cx - trayW / 2f, top + base * 0.012f, cx + trayW / 2f, top + base * 0.012f + trayH);
        dealerHand.set(cx, trayRect.bottom + base * 0.025f + cardH / 2f);
        float feltRx = (right - left) / 2f - t;
        shoeCenter.set(cx + feltRx * 0.62f, top + base * 0.1f);

        // Texto "BLACKJACK PAYS 3 TO 2" bajo las cartas del crupier.
        textSize = base * 0.035f;
        float textRy = (dealerHand.y + cardH / 2f + textSize * 1.9f) - top;
        float textRx = textRy * 2.3f;
        textOval.set(cx - textRx, top - textRy, cx + textRx, top + textRy);

        // Asientos sobre una elipse interior.
        float a = feltRx * 0.82f;
        float b = (ry - t) * 0.70f;
        for (int i = 0; i < SEAT_COUNT; i++) {
            double rad = Math.toRadians(SEAT_ANGLES[i]);
            seats[i].set(cx + (float) (a * Math.cos(rad)), top + (float) (b * Math.sin(rad)));
            seatTilt[i] = (SEAT_ANGLES[i] - 90f) * 0.22f;
        }

        // Anchos máximos para las manos (se escalan si no caben).
        botHandsMaxWidth = plateW * 1.4f;
        float between = seats[BOT_SEATS[2]].x - seats[BOT_SEATS[1]].x;
        // Entre las cartas de los bots vecinos (cada una ocupa como máximo ~4 cartas).
        float botNatural = HandRenderer.widthFor(4, cardW);
        playerHandsMaxWidth = Math.max(cardW * 1.6f, between - botNatural - gap * 4f);
        dealerHandsMaxWidth = trayRect.width() * 0.85f;
    }

    private void buildShape(Path path, RectF oval, float left, float right) {
        path.reset();
        path.moveTo(left, top);
        path.lineTo(right, top);
        path.arcTo(oval, 0f, 180f, false);
        path.close();
    }
}
