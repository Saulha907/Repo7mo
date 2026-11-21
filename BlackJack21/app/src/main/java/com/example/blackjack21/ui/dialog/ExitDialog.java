package com.example.blackjack21.ui.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;

import com.example.blackjack21.R;
import com.example.blackjack21.ui.BaseImmersiveActivity;

/**
 * Diálogo de confirmación de salida con estilo de casino.
 */
public final class ExitDialog {

    private ExitDialog() {
    }

    public static AlertDialog create(Activity activity, Runnable onConfirm) {
        View content = activity.getLayoutInflater().inflate(R.layout.dialog_exit, null, false);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(content)
                .setCancelable(true)
                .create();

        content.findViewById(R.id.button_exit_cancel).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.button_exit_confirm).setOnClickListener(v -> {
            dialog.dismiss();
            onConfirm.run();
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setOnShowListener(d -> BaseImmersiveActivity.hideSystemBars(dialog.getWindow()));
        return dialog;
    }
}
