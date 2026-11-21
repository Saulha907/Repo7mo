package com.example.blackjack21.view.paint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.example.blackjack21.R;

/**
 * Dibuja cartas tradicionales (anverso y reverso) sobre un Canvas.
 * Todos los objetos se reservan una sola vez para no generar basura en onDraw.
 */
public final class CardPainter {

    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint facePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint suitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goldStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goldFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path path = new Path();
    private final RectF tmp = new RectF();
    private final RectF orn = new RectF();

    private final int red;
    private final int black;
    /** Altura mínima legible del índice (px) en cartas pequeñas. */
    private final float minIndex;

    public CardPainter(Context context) {
        red = ContextCompat.getColor(context, R.color.card_red);
        black = ContextCompat.getColor(context, R.color.card_black);
        minIndex = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9.5f,
                context.getResources().getDisplayMetrics());

        shadowPaint.setColor(ContextCompat.getColor(context, R.color.shadow_soft));
        facePaint.setColor(ContextCompat.getColor(context, R.color.card_face));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(ContextCompat.getColor(context, R.color.card_border));
        innerPaint.setColor(ContextCompat.getColor(context, R.color.card_face_inner));
        goldStroke.setStyle(Paint.Style.STROKE);
        goldStroke.setColor(ContextCompat.getColor(context, R.color.gold_dark));
        goldFill.setColor(ContextCompat.getColor(context, R.color.gold));
        backPaint.setColor(ContextCompat.getColor(context, R.color.card_back));
        backLinePaint.setStyle(Paint.Style.STROKE);
        backLinePaint.setColor(ContextCompat.getColor(context, R.color.card_back_pattern));

        Typeface serifBold = Typeface.create(Typeface.SERIF, Typeface.BOLD);
        textPaint.setTypeface(serifBold);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /** Anverso de la carta. {@code court} = figura (J, Q, K) con marco central. */
    public void drawFace(Canvas c, RectF r, String rank, int suit, boolean court) {
        drawFace(c, r, rank, suit, court, false);
    }

    /**
     * @param compact variante para cartas pequeñas: índice grande en la esquina
     *                superior y palo central, sin marco de figura.
     */
    public void drawFace(Canvas c, RectF r, String rank, int suit, boolean court, boolean compact) {
        float w = r.width();
        float radius = w * 0.08f;
        int ink = SuitPaths.isRed(suit) ? red : black;

        drawShadow(c, r, radius);
        c.drawRoundRect(r, radius, radius, facePaint);
        borderPaint.setStrokeWidth(Math.max(1f, w * 0.012f));
        c.drawRoundRect(r, radius, radius, borderPaint);

        textPaint.setColor(ink);
        suitPaint.setColor(ink);

        if (compact) {
            drawCompact(c, r, rank, suit);
            return;
        }

        // Índices en esquinas opuestas.
        drawIndex(c, r, rank, suit);
        c.save();
        c.rotate(180f, r.centerX(), r.centerY());
        drawIndex(c, r, rank, suit);
        c.restore();

        if (court) {
            drawCourtCenter(c, r, rank, suit);
        } else {
            SuitPaths.buildCentered(suit, r.centerX(), r.centerY(), w * 0.46f, path);
            c.drawPath(path, suitPaint);
        }
    }

    private void drawCompact(Canvas c, RectF r, String rank, int suit) {
        float w = r.width();
        float cx = r.left + w * 0.2f;
        textPaint.setTextSize(w * 0.34f);
        float baseline = r.top + w * 0.36f;
        c.drawText(rank, cx, baseline, textPaint);
        float s = w * 0.22f;
        SuitPaths.buildCentered(suit, cx, baseline + w * 0.05f + s / 2f, s, path);
        c.drawPath(path, suitPaint);
        float big = w * 0.42f;
        SuitPaths.buildCentered(suit, r.centerX() + w * 0.1f, r.bottom - r.height() * 0.3f, big, path);
        c.drawPath(path, suitPaint);
    }

    /** Reverso clásico con trama diagonal y emblema dorado. */
    public void drawBack(Canvas c, RectF r) {
        float w = r.width();
        float radius = w * 0.08f;

        drawShadow(c, r, radius);
        c.drawRoundRect(r, radius, radius, facePaint);

        float inset = w * 0.06f;
        tmp.set(r.left + inset, r.top + inset, r.right - inset, r.bottom - inset);
        c.drawRoundRect(tmp, radius * 0.6f, radius * 0.6f, backPaint);

        c.save();
        c.clipRect(tmp);
        backLinePaint.setStrokeWidth(Math.max(1f, w * 0.022f));
        float step = w * 0.1f;
        float span = tmp.height() + tmp.width();
        for (float d = -span; d < span; d += step) {
            c.drawLine(tmp.left + d, tmp.top, tmp.left + d + tmp.height(), tmp.bottom, backLinePaint);
            c.drawLine(tmp.right - d, tmp.top, tmp.right - d - tmp.height(), tmp.bottom, backLinePaint);
        }
        c.restore();

        goldStroke.setStrokeWidth(Math.max(1f, w * 0.015f));
        c.drawRoundRect(tmp, radius * 0.6f, radius * 0.6f, goldStroke);
        SuitPaths.buildCentered(SuitPaths.DIAMOND, r.centerX(), r.centerY(), w * 0.3f, path);
        c.drawPath(path, goldFill);
        c.drawPath(path, goldStroke);

        borderPaint.setStrokeWidth(Math.max(1f, w * 0.012f));
        c.drawRoundRect(r, radius, radius, borderPaint);
    }

    private void drawIndex(Canvas c, RectF r, String rank, int suit) {
        float w = r.width();
        float size = Math.min(w * 0.3f, Math.max(w * 0.2f, minIndex));
        textPaint.setTextSize(size);
        // Centro del índice: se aparta del borde si el valor crece ("10").
        float cx = r.left + Math.max(w * 0.13f, w * 0.05f + textPaint.measureText(rank) / 2f);
        float baseline = r.top + w * 0.06f + size * 0.85f;
        c.drawText(rank, cx, baseline, textPaint);
        float s = size * 0.65f;
        SuitPaths.buildCentered(suit, cx, baseline + w * 0.03f + s / 2f, s, path);
        c.drawPath(path, suitPaint);
    }

    private void drawCourtCenter(Canvas c, RectF r, String rank, int suit) {
        float w = r.width();
        float h = r.height();
        tmp.set(r.left + w * 0.24f, r.top + h * 0.16f, r.right - w * 0.24f, r.bottom - h * 0.16f);
        c.drawRect(tmp, innerPaint);
        goldStroke.setStrokeWidth(Math.max(1f, w * 0.014f));
        c.drawRect(tmp, goldStroke);

        float letterSize = w * 0.36f;
        textPaint.setTextSize(letterSize);
        float letterCy = tmp.centerY() + w * 0.07f;
        float baseline = letterCy - (textPaint.ascent() + textPaint.descent()) / 2f;
        c.drawText(rank, tmp.centerX(), baseline, textPaint);
        float letterTop = baseline - letterSize * 0.72f;
        drawCourtOrnament(c, rank, tmp.centerX(), letterTop - w * 0.025f, w * 0.28f, w * 0.11f);

        float s = w * 0.13f;
        SuitPaths.buildCentered(suit, tmp.centerX(), tmp.top + s * 0.75f, s, path);
        c.drawPath(path, suitPaint);
        c.save();
        c.rotate(180f, tmp.centerX(), tmp.centerY());
        SuitPaths.buildCentered(suit, tmp.centerX(), tmp.top + s * 0.75f, s, path);
        c.drawPath(path, suitPaint);
        c.restore();
    }

    /**
     * Adorno dorado de la figura, apoyado en {@code bottom}: corona de tres puntas
     * (K), corona de cinco puntas con joya (Q) o gorro con pluma (J).
     */
    private void drawCourtOrnament(Canvas c, String rank, float cx, float bottom, float cw, float ch) {
        goldStroke.setStrokeWidth(Math.max(1f, cw * 0.05f));
        float l = cx - cw / 2f;
        float r = cx + cw / 2f;
        float top = bottom - ch;
        if ("J".equals(rank)) {
            path.reset();
            orn.set(l + cw * 0.12f, top + ch * 0.1f, r - cw * 0.12f, bottom + ch * 0.9f);
            path.arcTo(orn, 180f, 180f, false);
            path.close();
            c.drawPath(path, goldFill);
            c.drawPath(path, goldStroke);
            orn.set(l, bottom - ch * 0.22f, r, bottom + ch * 0.06f);
            c.drawRect(orn, goldFill);
            c.drawRect(orn, goldStroke);
            // Pluma.
            path.reset();
            path.moveTo(cx + cw * 0.18f, top + ch * 0.35f);
            path.quadTo(cx + cw * 0.62f, top - ch * 0.35f, cx + cw * 0.5f, top - ch * 0.05f);
            path.quadTo(cx + cw * 0.38f, top + ch * 0.2f, cx + cw * 0.18f, top + ch * 0.35f);
            c.drawPath(path, suitPaint);
            return;
        }
        int points = "K".equals(rank) ? 3 : 5;
        float valley = bottom - ch * 0.45f;
        path.reset();
        path.moveTo(l, bottom);
        path.lineTo(l, top);
        for (int i = 1; i < points; i++) {
            float x0 = l + cw * (i - 1) / (points - 1);
            float x1 = l + cw * i / (points - 1);
            path.lineTo((x0 + x1) / 2f, valley);
            path.lineTo(x1, top);
        }
        path.lineTo(r, bottom);
        path.close();
        c.drawPath(path, goldFill);
        c.drawPath(path, goldStroke);
        float ball = ch * 0.14f;
        for (int i = 0; i < points; i++) {
            float x = l + cw * i / (points - 1);
            c.drawCircle(x, top, ball, goldFill);
            c.drawCircle(x, top, ball, goldStroke);
        }
        // Joya central del color del palo.
        c.drawCircle(cx, bottom - ch * 0.22f, ch * 0.13f, suitPaint);
    }

    /** Sombra suave por capas (compatible con aceleración por hardware en API 24). */
    private void drawShadow(Canvas c, RectF r, float radius) {
        float step = r.width() * 0.012f;
        for (int i = 1; i <= 3; i++) {
            tmp.set(r);
            tmp.offset(step * i * 0.6f, step * i);
            c.drawRoundRect(tmp, radius, radius, shadowPaint);
        }
    }
}
