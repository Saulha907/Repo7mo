package com.example.blackjack21.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.example.blackjack21.R;
import com.example.blackjack21.audio.SoundEffect;
import com.example.blackjack21.audio.SoundManager;
import com.example.blackjack21.audio.Sounds;
import com.example.blackjack21.ui.anim.MenuAnimator;

/** Configuración: activar efectos de sonido y ajustar su volumen (se guarda al instante). */
public class SettingsActivity extends BaseImmersiveActivity {

    private SoundManager sound;
    private SwitchCompat soundSwitch;
    private SeekBar volumeBar;
    private TextView volumeText;
    private View volumeRow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sound = Sounds.get(this);

        soundSwitch = findViewById(R.id.switch_sound);
        volumeBar = findViewById(R.id.seek_volume);
        volumeText = findViewById(R.id.text_volume);
        volumeRow = findViewById(R.id.row_volume);
        View back = findViewById(R.id.button_back);

        // Estado guardado antes de conectar los listeners.
        soundSwitch.setChecked(sound.isEnabled());
        volumeBar.setProgress(sound.getVolume());
        refresh();

        soundSwitch.setOnCheckedChangeListener((button, checked) -> {
            sound.setEnabled(checked);
            refresh();
            sound.play(SoundEffect.BUTTON);
        });
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (fromUser) {
                    sound.setVolume(progress);
                    refresh();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                sound.play(SoundEffect.CHIP_SELECT); // muestra del nuevo volumen
            }
        });
        back.setOnClickListener(v -> {
            sound.play(SoundEffect.BUTTON);
            finish();
        });

        if (savedInstanceState == null) {
            MenuAnimator.playIntro(findViewById(R.id.settings_title),
                    findViewById(R.id.settings_panel), null, back);
        }
    }

    private void refresh() {
        boolean on = sound.isEnabled();
        volumeBar.setEnabled(on);
        volumeRow.setAlpha(on ? 1f : 0.45f);
        volumeText.setText(getString(R.string.settings_volume_value, sound.getVolume()));
    }
}
