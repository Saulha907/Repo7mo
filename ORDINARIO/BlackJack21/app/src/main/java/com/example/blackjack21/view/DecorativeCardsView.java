package com.example.blackjack21.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.blackjack21.R;
import com.example.blackjack21.view.paint.CardPainter;
import com.example.blackjack21.view.paint.ChipPainter;
import com.example.blackjack21.view.paint.SuitPaths;

/**
 * Composición decorativa del menú: abanico de cartas (A♠ + K♥ = 21) y pilas
 * de fichas, con un balanceo lento y discreto.
 */
public class DecorativeCardsView extends View {

    private static final float CARD_RATIO = 1.42f;
    private static final long CYCLE_MS = 7000L;
    private static final float TWO_PI = (float) (Math.PI * 2);

    private final CardPainter cardPainter;
    private final ChipPainter chipPainter;
    private final RectF cardRect = new RectF();

    private final int chipRed;
    private final int chipBlack;
    private final int chipGreen;
    private final int chipBlue;

    private float cardW;
    private float cardH;
    private float centerX;
    private float centerY;
    private float chipR;
    private float phase;

    private ValueAnimator animator;

    public DecorativeCardsView(Context context) {
        this(context, null);
    }

    public DecorativeCardsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        cardPainter = new CardPainter(context);
        chipPainter = new ChipPainter(context);
        chipRed = ContextCompat.getColor(context, R.color.chip_red);
        chipBlack = ContextCompat.getColor(context, R.color.chip_black);
        chipGreen = ContextCompat.getColor(context, R.color.chip_green);
        chipBlue = ContextCompat.getColor(context, R.color.chip_blue);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cardH = Math.min(h * 0.72f, (w / 3.7f) * CARD_RATIO);
        cardW = cardH / CARD_RATIO;
        chipR = cardW * 0.3f;
        centerX = w / 2f;
        centerY = h * 0.48f;
    }

    @Override
    protected void onDraw(@NonNull Canvas c) {
        if (cardW <= 0f) {
            return;
        }
        float bob = cardH * 0.014f;
        float base = centerY + cardH * 0.42f;

        // Fichas traseras.
        chipPainter.drawStack(c, centerX - cardW * 1.42f, base, chipR, 6, chipRed, "5");
        chipPainter.drawStack(c, centerX + cardW * 1.4f, base - chipR * 0.2f, chipR, 4, chipBlack, "100");

        // Abanico de cartas.
        drawCard(c, -0.44f, 0.05f, -17f + wave(0f) * 1.0f, wave(0.4f) * bob, null, 0, false);
        drawCard(c, -0.08f, 0f, -5f + wave(1.3f) * 1.3f, wave(1.7f) * bob, "A", SuitPaths.SPADE, false);
        drawCard(c, 0.38f, 0.03f, 11f + wave(2.4f) * 1.3f, wave(2.9f) * bob, "K", SuitPaths.HEART, true);

        // Fichas delanteras.
        chipPainter.drawStack(c, centerX + cardW * 0.92f, base + chipR * 0.55f, chipR * 0.9f, 2, chipGreen, "25");
        chipPainter.drawStack(c, centerX - cardW * 0.95f, base + chipR * 0.6f, chipR * 0.85f, 1, chipBlue, "10");
    }

    private float wave(float offset) {
        return (float) Math.sin(phase + offset);
    }

    private void drawCard(Canvas c, float dxFactor, float dyFactor, float rotation, float bob,
                          @Nullable String rank, int suit, boolean court) {
        c.save();
        c.translate(centerX + cardW * dxFactor, centerY + cardH * dyFactor + bob);
        c.rotate(rotation);
        cardRect.set(-cardW / 2f, -cardH / 2f, cardW / 2f, cardH / 2f);
        if (rank == null) {
            cardPainter.drawBack(c, cardRect);
        } else {
            cardPainter.drawFace(c, cardRect, rank, suit, court);
        }
        c.restore();
    }

    // --- Animación ligera -------------------------------------------------

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startIdle();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopIdle();
        super.onDetachedFromWindow();
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (animator == null) {
            return;
        }
        if (isVisible) {
            animator.resume();
        } else {
            animator.pause();
        }
    }

    private void startIdle() {
        if (animator != null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ValueAnimator.areAnimatorsEnabled()) {
            return;
        }
        animator = ValueAnimator.ofFloat(0f, TWO_PI);
        animator.setDuration(CYCLE_MS);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(a -> {
            phase = (float) a.getAnimatedValue();
            postInvalidateOnAnimation();
        });
        animator.start();
    }

    private void stopIdle() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}
