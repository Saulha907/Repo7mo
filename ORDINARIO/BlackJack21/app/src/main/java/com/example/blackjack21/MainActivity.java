package com.example.blackjack21;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.blackjack21.audio.SoundEffect;
import com.example.blackjack21.audio.Sounds;
import com.example.blackjack21.navigation.AppNavigator;
import com.example.blackjack21.ui.BaseImmersiveActivity;
import com.example.blackjack21.ui.anim.MenuAnimator;
import com.example.blackjack21.ui.dialog.ExitDialog;

/**
 * Punto de entrada: menú principal.
 */
public class MainActivity extends BaseImmersiveActivity {

    @Nullable
    private AlertDialog exitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View play = findViewById(R.id.button_play);
        View stats = findViewById(R.id.button_stats);
        View settings = findViewById(R.id.button_settings);
        View exit = findViewById(R.id.button_exit);

        play.setOnClickListener(v -> {
            clickSound();
            AppNavigator.openGame(this);
        });
        stats.setOnClickListener(v -> {
            clickSound();
            AppNavigator.openStats(this);
        });
        settings.setOnClickListener(v -> {
            clickSound();
            AppNavigator.openSettings(this);
        });
        exit.setOnClickListener(v -> {
            clickSound();
            showExitDialog();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });

        if (savedInstanceState == null) {
            MenuAnimator.playIntro(
                    findViewById(R.id.menu_title),
                    findViewById(R.id.menu_subtitle_row),
                    findViewById(R.id.menu_cards),
                    play, stats, settings, exit);
        }
    }

    private void clickSound() {
        Sounds.play(this, SoundEffect.BUTTON);
    }

    private void showExitDialog() {
        if (exitDialog == null) {
            exitDialog = ExitDialog.create(this, () -> AppNavigator.exitApp(this));
        }
        if (!exitDialog.isShowing()) {
            exitDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        if (exitDialog != null) {
            exitDialog.dismiss();
            exitDialog = null;
        }
        super.onDestroy();
    }
}
