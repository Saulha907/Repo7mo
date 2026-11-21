package com.example.blackjack21.navigation;

import android.app.Activity;
import android.content.Intent;

import androidx.core.app.ActivityOptionsCompat;

import com.example.blackjack21.ui.GameActivity;
import com.example.blackjack21.ui.SettingsActivity;
import com.example.blackjack21.ui.StatsActivity;

/**
 * Punto único de navegación entre pantallas.
 */
public final class AppNavigator {

    private AppNavigator() {
    }

    public static void openGame(Activity from) {
        open(from, GameActivity.class);
    }

    public static void openStats(Activity from) {
        open(from, StatsActivity.class);
    }

    public static void openSettings(Activity from) {
        open(from, SettingsActivity.class);
    }

    /** Cierra todas las pantallas de la aplicación. */
    public static void exitApp(Activity from) {
        from.finishAffinity();
    }

    private static void open(Activity from, Class<? extends Activity> target) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                from, android.R.anim.fade_in, android.R.anim.fade_out);
        from.startActivity(new Intent(from, target), options.toBundle());
    }
}
