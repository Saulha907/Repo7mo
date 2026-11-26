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
 * Decisión explícita de seguro: aceptar o rechazar. No se puede cerrar sin decidir.
 */
public final class InsuranceDialog {

    private InsuranceDialog() {
    }

    public static AlertDialog create(Activity activity, int amount, boolean canAccept,
                                     Runnable onAccept, Runnable onDecline) {
        View content = activity.getLayoutInflater().inflate(R.layout.dialog_insurance, null, false);
        TextView message = content.findViewById(R.id.insurance_message);
        message.setText(activity.getString(R.string.insurance_message,
                String.format(Locale.US, "%,d", amount)));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(content)
                .setCancelable(false)
                .create();

        View accept = content.findViewById(R.id.button_insurance_accept);
        accept.setEnabled(canAccept);
        accept.setOnClickListener(v -> {
            dialog.dismiss();
            onAccept.run();
        });
        content.findViewById(R.id.button_insurance_decline).setOnClickListener(v -> {
            dialog.dismiss();
            onDecline.run();
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setOnShowListener(d -> BaseImmersiveActivity.hideSystemBars(dialog.getWindow()));
        return dialog;
    }
}
