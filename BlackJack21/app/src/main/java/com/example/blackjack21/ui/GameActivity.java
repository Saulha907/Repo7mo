package com.example.blackjack21.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.blackjack21.R;
import com.example.blackjack21.audio.GameSounds;
import com.example.blackjack21.audio.SoundEffect;
import com.example.blackjack21.audio.SoundManager;
import com.example.blackjack21.audio.Sounds;
import com.example.blackjack21.data.GameSession;
import com.example.blackjack21.data.PrefsKeyValueStore;
import com.example.blackjack21.engine.GamePhase;
import com.example.blackjack21.engine.HandState;
import com.example.blackjack21.engine.PlayerState;
import com.example.blackjack21.game.Frame;
import com.example.blackjack21.game.GameController;
import com.example.blackjack21.game.GameDirector;
import com.example.blackjack21.ui.anim.TableAnimationPlayer;
import com.example.blackjack21.ui.dialog.GameOverDialog;
import com.example.blackjack21.ui.dialog.InsuranceDialog;
import com.example.blackjack21.view.ChipView;
import com.example.blackjack21.view.table.TableView;

import java.util.List;
import java.util.Locale;

/**
 * Pantalla de la mesa. Traduce pulsaciones a llamadas del {@link GameDirector}
 * (que ejecuta en el {@link GameController} y presenta con animaciones) y
 * refleja el estado; no contiene reglas de juego.
 */
public class GameActivity extends BaseImmersiveActivity implements GameDirector.Listener {

    private static final int[] CHIP_IDS = {R.id.chip_5, R.id.chip_25, R.id.chip_100, R.id.chip_500};

    private GameDirector director;
    private GameController controller;
    private TableAnimationPlayer animationPlayer;
    private GameSounds sounds;
    private TableView table;
    private TextView status;
    private Button hit;
    private Button stand;
    private Button doubleDown;
    private Button split;
    private Button insurance;
    private Button deal;
    private Button clearBet;
    private final ChipView[] chips = new ChipView[CHIP_IDS.length];

    @Nullable
    private Toast notice;
    @Nullable
    private AlertDialog insuranceDialog;
    @Nullable
    private AlertDialog gameOverDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        GameViewModel model = new ViewModelProvider(this).get(GameViewModel.class);
        director = model.getOrCreate(PrefsKeyValueStore.forGame(this), this::openGame);
        controller = director.getController();
        SoundManager soundManager = Sounds.get(this);
        sounds = model.getSounds(soundManager::play);

        table = findViewById(R.id.table_view);
        status = findViewById(R.id.game_status);
        hit = findViewById(R.id.button_hit);
        stand = findViewById(R.id.button_stand);
        doubleDown = findViewById(R.id.button_double);
        split = findViewById(R.id.button_split);
        insurance = findViewById(R.id.button_insurance);
        deal = findViewById(R.id.button_deal);
        clearBet = findViewById(R.id.button_clear_bet);
        animationPlayer = new TableAnimationPlayer(this, table, controller.getBotIds());
        animationPlayer.setFrameStartListener(sounds::onFrameStarted);

        findViewById(R.id.button_menu).setOnClickListener(v -> {
            soundManager.play(SoundEffect.BUTTON);
            finish();
        });
        // Plantarse, doblar, dividir y seguro suenan con su fotograma; pedir, al aceptarse.
        hit.setOnClickListener(v -> sounds.onAction(SoundEffect.HIT, director.hit()));
        stand.setOnClickListener(v -> director.stand());
        doubleDown.setOnClickListener(v -> director.doubleDown());
        split.setOnClickListener(v -> director.split());
        insurance.setOnClickListener(v -> director.takeInsurance());
        clearBet.setOnClickListener(v -> sounds.onAction(SoundEffect.BUTTON, director.clearBet()));
        deal.setOnClickListener(v -> {
            if (!director.deal()) {
                sounds.onAction(SoundEffect.BUTTON, director.startNextRound());
            }
        });

        for (int i = 0; i < CHIP_IDS.length; i++) {
            ChipView chip = findViewById(CHIP_IDS[i]);
            chips[i] = chip;
            chip.setOnClickListener(v -> onChipPressed((ChipView) v));
        }
    }

    /** Recupera la partida guardada o crea una nueva. La depuración puede sustituirlo. */
    protected GameController openGame(GameSession session) {
        return session.open(this::createController, getString(R.string.player_you),
                getString(R.string.dealer_tag));
    }

    /** Crea una partida nueva. Las variantes de depuración pueden sustituirla. */
    protected GameController createController() {
        return GameController.createDefault(getString(R.string.player_you),
                getString(R.string.dealer_tag), System.nanoTime());
    }

    @Override
    protected void onStart() {
        super.onStart();
        director.attach(animationPlayer, animationPlayer, this);
        render();
    }

    @Override
    protected void onStop() {
        // Interrumpe la presentación y deja la mesa en el estado real del motor.
        director.detach();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        animationPlayer.release();
        dismissInsuranceDialog();
        dismissGameOverDialog();
        super.onDestroy();
    }

    private void onChipPressed(ChipView chip) {
        boolean added = sounds.onAction(SoundEffect.CHIP_SELECT, director.addChip(chip.getAmount()));
        if (!added && director.canAct() && controller.canChangeBet()) {
            showNotice(R.string.notice_insufficient);
        }
    }

    @Override
    public void onPresentationChanged() {
        render();
    }

    /** Vuelca el estado en los controles. Durante la presentación la mesa la anima el reproductor. */
    private void render() {
        boolean idle = director.canAct();
        if (idle) {
            table.setSnapshot(controller.snapshot());
        }

        hit.setEnabled(idle && controller.canHit());
        stand.setEnabled(idle && controller.canStand());
        doubleDown.setEnabled(idle && controller.canDouble());
        split.setEnabled(idle && controller.canSplit());
        insurance.setEnabled(idle && controller.canTakeInsurance());

        // Mientras se presenta la ronda no se anuncia todavía "Nueva ronda".
        boolean roundOver = idle && controller.canStartNextRound();
        deal.setText(roundOver ? R.string.action_new_round : R.string.action_deal);
        deal.setEnabled(idle && (roundOver || controller.canDeal()));

        boolean betting = idle && controller.canChangeBet();
        clearBet.setEnabled(idle && controller.canClearBet());
        for (ChipView chip : chips) {
            chip.setEnabled(betting);
            chip.setAlpha(betting ? 1f : 0.45f);
        }

        status.setText(idle ? statusText() : presentationText());
        updateInsuranceDialog(idle);
        updateGameOverDialog(idle);
    }

    /** Texto de estado mientras se reproduce la presentación. */
    private String presentationText() {
        Frame frame = director.getCurrentFrame();
        if (frame == null) {
            if (controller.getPhase() == GamePhase.INSURANCE_DECISION) {
                return getString(R.string.status_bots_insurance);
            }
            String name = controller.getCurrentTurnName();
            return name == null ? getString(R.string.status_dealing)
                    : getString(R.string.status_bot_turn, name);
        }
        switch (frame.kind) {
            case SETTLED:
                return getString(R.string.status_results);
            case HOLE_REVEAL:
                return getString(R.string.status_dealer_turn);
            case BET:
                return getString(R.string.status_dealing);
            case DEALER_CARD:
                return frame.cardIndex >= 2 ? getString(R.string.status_dealer_turn)
                        : getString(R.string.status_dealing);
            case BOT_ACTION:
                return getString(R.string.status_bot_action, nameOf(frame.playerId),
                        actionText(frame));
            default:
                if (frame.snapshot != null && frame.snapshot.dealer.hands.get(0).cards.size() < 2) {
                    return getString(R.string.status_dealing);
                }
                if (GameController.HUMAN_ID.equals(frame.playerId)) {
                    return getString(R.string.status_your_move);
                }
                return getString(R.string.status_bot_turn, nameOf(frame.playerId));
        }
    }

    private String nameOf(@Nullable String playerId) {
        if (playerId == null) {
            return getString(R.string.dealer_tag);
        }
        PlayerState p = controller.getEngine().getPlayer(playerId);
        return p.getName();
    }

    private String actionText(Frame frame) {
        if (frame.action == null) {
            return getString(R.string.callout_stand);
        }
        switch (frame.action) {
            case HIT:
                return getString(R.string.callout_hit);
            case DOUBLE:
                return getString(R.string.callout_double);
            case SPLIT:
                return getString(R.string.callout_split);
            default:
                return getString(R.string.callout_stand);
        }
    }

    private String statusText() {
        switch (controller.getStatus()) {
            case BET_READY:
                return getString(R.string.status_bet_ready, format(controller.getPendingBet()));
            case GAME_OVER:
                return getString(R.string.status_game_over);
            case INSURANCE:
                return getString(R.string.status_insurance);
            case YOUR_TURN:
                return yourTurnText();
            case BOT_TURN:
                String name = controller.getCurrentTurnName();
                return getString(R.string.status_bot_turn, name == null ? "…" : name);
            case ROUND_OVER:
                if (!controller.humanPlayedRound()) {
                    return getString(R.string.status_round_watched);
                }
                int net = controller.getHumanRoundNet();
                if (net > 0) {
                    return getString(R.string.status_round_win, format(net));
                }
                if (net < 0) {
                    return getString(R.string.status_round_lose, format(-net));
                }
                return getString(R.string.status_round_push);
            default:
                return getString(R.string.status_place_bet);
        }
    }

    private String yourTurnText() {
        List<HandState> hands = controller.getHumanHands();
        HandState current = controller.getEngine().getCurrentHand();
        String score = current == null ? "" : String.valueOf(current.getTotal());
        if (hands.size() > 1 && current != null) {
            return getString(R.string.status_your_turn_split,
                    hands.indexOf(current) + 1, hands.size(), score);
        }
        return getString(R.string.status_your_turn, score);
    }

    /** Un único diálogo de seguro, solo cuando ha terminado la presentación. */
    private void updateInsuranceDialog(boolean idle) {
        if (idle && controller.isInsuranceDecisionPending()) {
            if (insuranceDialog == null || !insuranceDialog.isShowing()) {
                insuranceDialog = InsuranceDialog.create(this, controller.getInsuranceAmount(),
                        controller.canTakeInsurance(),
                        director::takeInsurance, director::declineInsurance);
                insuranceDialog.show();
            }
        } else if (!controller.isInsuranceDecisionPending()) {
            dismissInsuranceDialog();
        }
    }

    /** Un único diálogo de fin de partida, solo sin manos en juego ni animaciones. */
    private void updateGameOverDialog(boolean idle) {
        if (idle && controller.isGameOver()) {
            if (gameOverDialog == null || !gameOverDialog.isShowing()) {
                gameOverDialog = GameOverDialog.create(this, controller.getHuman().getBalance(),
                        director::restartGame, this::finish);
                gameOverDialog.show();
            }
        } else if (!controller.isGameOver()) {
            dismissGameOverDialog();
        }
    }

    private void dismissGameOverDialog() {
        if (gameOverDialog != null) {
            gameOverDialog.dismiss();
            gameOverDialog = null;
        }
    }

    private void dismissInsuranceDialog() {
        if (insuranceDialog != null) {
            insuranceDialog.dismiss();
            insuranceDialog = null;
        }
    }

    private void showNotice(int textRes) {
        if (notice != null) {
            notice.cancel();
        }
        notice = Toast.makeText(this, textRes, Toast.LENGTH_SHORT);
        notice.show();
    }

    private static String format(int amount) {
        return String.format(Locale.US, "%,d", amount);
    }
}
