package com.example.blackjack21.ui.anim;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;

/**
 * Animaciones de entrada de las pantallas de menú: título, subtítulo,
 * decoración y botones escalonados.
 */
public final class MenuAnimator {

    private static final Interpolator EASE = new DecelerateInterpolator(1.6f);
    private static final long TITLE_MS = 700L;
    private static final long BUTTON_MS = 450L;
    private static final long BUTTON_START_MS = 380L;
    private static final long BUTTON_STAGGER_MS = 90L;

    private MenuAnimator() {
    }

    public static void playIntro(View title, @Nullable View subtitle,
                                 @Nullable View decoration, View... buttons) {
        float d = title.getResources().getDisplayMetrics().density;

        title.setAlpha(0f);
        title.setTranslationY(-20f * d);
        title.setScaleX(0.94f);
        title.setScaleY(0.94f);
        title.animate().alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
                .setDuration(TITLE_MS).setInterpolator(EASE).start();

        if (subtitle != null) {
            subtitle.setAlpha(0f);
            subtitle.animate().alpha(1f).setStartDelay(TITLE_MS / 2)
                    .setDuration(TITLE_MS).setInterpolator(EASE).start();
        }

        if (decoration != null) {
            decoration.setAlpha(0f);
            decoration.setTranslationY(16f * d);
            decoration.animate().alpha(1f).translationY(0f).setStartDelay(150L)
                    .setDuration(900L).setInterpolator(EASE).start();
        }

        for (int i = 0; i < buttons.length; i++) {
            View b = buttons[i];
            b.setAlpha(0f);
            b.setTranslationX(40f * d);
            b.animate().alpha(1f).translationX(0f)
                    .setStartDelay(BUTTON_START_MS + i * BUTTON_STAGGER_MS)
                    .setDuration(BUTTON_MS).setInterpolator(EASE).start();
        }
    }
}
