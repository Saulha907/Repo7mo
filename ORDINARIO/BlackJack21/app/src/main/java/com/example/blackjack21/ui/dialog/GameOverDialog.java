package com.example.blackjack21.ui.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.blackjack21.R;
import com.example.blackjack21.ui.BaseImmersiveActivity;

import java.util.Locale;

/**
 * Fin de partida por falta de fichas: REINICIAR o VOLVER AL MENÚ.
 */
public final class GameOverDialog {

    private GameOverDialog() {
    }

    public static AlertDialog create(Activity activity, int balance, Runnable onRestart, Runnable onMenu) {
        View content = activity.getLayoutInflater().inflate(R.layout.dialog_game_over, null, false);
        TextView message = content.findViewById(R.id.game_over_message);
        message.setText(activity.getString(R.string.game_over_message,
                String.format(Locale.US, "%,d", balance)));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(content)
                .setCancelable(false)
                .create();

        content.findViewById(R.id.button_game_over_restart).setOnClickListener(v -> {
            dialog.dismiss();
            onRestart.run();
        });
        content.findViewById(R.id.button_game_over_menu).setOnClickListener(v -> {
            dialog.dismiss();
            onMenu.run();
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setOnShowListener(d -> BaseImmersiveActivity.hideSystemBars(dialog.getWindow()));
        return dialog;
    }
}
