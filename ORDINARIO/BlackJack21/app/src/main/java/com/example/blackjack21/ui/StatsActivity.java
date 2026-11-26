package com.example.blackjack21.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.blackjack21.R;
import com.example.blackjack21.audio.SoundEffect;
import com.example.blackjack21.audio.Sounds;
import com.example.blackjack21.data.GameSession;
import com.example.blackjack21.data.PlayerStatistics;
import com.example.blackjack21.data.PrefsKeyValueStore;
import com.example.blackjack21.ui.anim.MenuAnimator;

import java.util.Locale;

/** Estadísticas históricas del jugador en tarjetas (se leen al mostrarse). */
public class StatsActivity extends BaseImmersiveActivity {

    private LinearLayout highlightRow;
    private LinearLayout roundsRow;
    private LinearLayout chipsRow;
    private TextView handsNote;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        highlightRow = findViewById(R.id.row_highlight);
        roundsRow = findViewById(R.id.row_rounds);
        chipsRow = findViewById(R.id.row_chips);
        handsNote = findViewById(R.id.stats_hands_note);
        View back = findViewById(R.id.button_back);
        back.setOnClickListener(v -> {
            Sounds.play(this, SoundEffect.BUTTON);
            finish();
        });
        if (savedInstanceState == null) {
            MenuAnimator.playIntro(findViewById(R.id.stats_title),
                    findViewById(R.id.stats_scroll), null, back);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bind(GameSession.readStatistics(PrefsKeyValueStore.forGame(this)));
    }

    private void bind(PlayerStatistics s) {
        highlightRow.removeAllViews();
        roundsRow.removeAllViews();
        chipsRow.removeAllViews();

        addCard(highlightRow, R.string.stat_balance, format(s.getCurrentBalance()), true);
        long net = s.getNetChips();
        addCard(highlightRow, R.string.stat_net, (net > 0 ? "+" : "") + format(net), true);
        addCard(highlightRow, R.string.stat_win_rate,
                getString(R.string.stat_percent, s.getWinRate()), true);

        addCard(roundsRow, R.string.stat_rounds_played, format(s.getRoundsPlayed()), false);
        addCard(roundsRow, R.string.stat_rounds_won, format(s.getRoundsWon()), false);
        addCard(roundsRow, R.string.stat_rounds_lost, format(s.getRoundsLost()), false);
        addCard(roundsRow, R.string.stat_rounds_pushed, format(s.getRoundsPushed()), false);
        addCard(roundsRow, R.string.stat_blackjacks, format(s.getBlackjacks()), false);

        addCard(chipsRow, R.string.stat_chips_won, format(s.getChipsWon()), false);
        addCard(chipsRow, R.string.stat_chips_lost, format(s.getChipsLost()), false);
        addCard(chipsRow, R.string.stat_biggest_win, format(s.getBiggestWin()), false);
        addCard(chipsRow, R.string.stat_restarts, format(s.getRestarts()), false);

        handsNote.setText(getString(R.string.stats_hands_note, format(s.getHandsWon()),
                format(s.getHandsLost()), format(s.getHandsPushed())));
    }

    private void addCard(LinearLayout row, @StringRes int label, String value, boolean highlight) {
        View card = LayoutInflater.from(this).inflate(R.layout.view_stat_card, row, false);
        TextView valueView = card.findViewById(R.id.stat_value);
        valueView.setText(value);
        ((TextView) card.findViewById(R.id.stat_label)).setText(label);
        if (highlight) {
            card.setBackgroundResource(R.drawable.bg_stat_card_highlight);
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.stat_value_highlight_text));
        }
        row.addView(card);
    }

    private static String format(long value) {
        return String.format(Locale.US, "%,d", value);
    }
}
