package com.example.blackjack21.view.table;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.example.blackjack21.view.paint.CardPainter;
import com.example.blackjack21.view.table.model.TableCard;

import java.util.List;

/**
 * Dibuja una mano de cartas superpuestas. Las manos largas se comprimen para
 * no ocupar más que una mano de cuatro cartas.
 */
final class HandRenderer {

    static final float OVERLAP = 0.42f;
    /** Solape de manos divididas en modo compacto (el índice ocupa menos). */
    static final float COMPACT_OVERLAP = 0.34f;
    private static final float MIN_OVERLAP = 0.14f;

    private final CardPainter cardPainter;
    private final RectF cardRect = new RectF();

    HandRenderer(CardPainter cardPainter) {
        this.cardPainter = cardPainter;
    }

    /** Desplazamiento horizontal entre cartas consecutivas. */
    static float stepFor(int count, float cardW) {
        return stepFor(count, cardW, OVERLAP);
    }

    /** Las manos largas se comprimen hasta ocupar como una de cuatro cartas. */
    static float stepFor(int count, float cardW, float overlap) {
        if (count <= 1) {
            return 0f;
        }
        float step = cardW * overlap;
        float cap = cardW * 3 * overlap;
        if ((count - 1) * step > cap) {
            step = Math.max(cardW * MIN_OVERLAP, cap / (count - 1));
        }
        return step;
    }

    /** Ancho total de una mano de {@code count} cartas. */
    static float widthFor(int count, float cardW) {
        return widthFor(count, cardW, OVERLAP);
    }

    static float widthFor(int count, float cardW, float overlap) {
        return count <= 0 ? 0f : cardW + (count - 1) * stepFor(count, cardW, overlap);
    }

    /**
     * @param left borde izquierdo de la mano.
     * @param out  recibe el rectángulo (sin rotar) que ocupa la mano.
     */
    void draw(Canvas c, List<TableCard> cards, float left, float cy,
              float cardW, float cardH, float tilt, RectF out) {
        draw(c, cards, left, cy, cardW, cardH, tilt, false, out);
    }

    /** @param compact cartas pequeñas de manos divididas (índice grande, más solape). */
    void draw(Canvas c, List<TableCard> cards, float left, float cy,
              float cardW, float cardH, float tilt, boolean compact, RectF out) {
        draw(c, cards, left, cy, cardW, cardH, tilt, compact, -1, null, out);
    }

    /**
     * @param hiddenIndex carta que no se dibuja (está en plena animación); -1 ninguna.
     * @param hiddenOut   recibe el rectángulo (sin rotar) donde iría esa carta.
     */
    void draw(Canvas c, List<TableCard> cards, float left, float cy,
              float cardW, float cardH, float tilt, boolean compact,
              int hiddenIndex, RectF hiddenOut, RectF out) {
        int n = cards.size();
        float overlap = compact ? COMPACT_OVERLAP : OVERLAP;
        float step = stepFor(n, cardW, overlap);
        float total = widthFor(n, cardW, overlap);
        out.set(left, cy - cardH / 2f, left + total, cy + cardH / 2f);
        if (n == 0) {
            return;
        }
        c.save();
        c.rotate(tilt, out.centerX(), cy);
        for (int i = 0; i < n; i++) {
            TableCard card = cards.get(i);
            float l = left + i * step;
            float lift = -Math.min(i, 4) * cardH * 0.02f;
            cardRect.set(l, cy - cardH / 2f + lift, l + cardW, cy + cardH / 2f + lift);
            if (i == hiddenIndex) {
                if (hiddenOut != null) {
                    hiddenOut.set(cardRect);
                }
                continue;
            }
            if (card.faceDown) {
                cardPainter.drawBack(c, cardRect);
            } else {
                cardPainter.drawFace(c, cardRect, card.rank, card.suit, card.isCourt(), compact);
            }
        }
        c.restore();
    }
}
