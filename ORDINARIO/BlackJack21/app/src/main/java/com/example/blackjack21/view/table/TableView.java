package com.example.blackjack21.view.table;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.blackjack21.R;
import com.example.blackjack21.view.paint.CardPainter;
import com.example.blackjack21.view.paint.ChipPainter;
import com.example.blackjack21.view.table.model.HandView;
import com.example.blackjack21.view.table.model.SeatInfo;
import com.example.blackjack21.view.table.model.TableCard;
import com.example.blackjack21.view.table.model.TableSnapshot;

import java.util.List;
import java.util.Locale;

/**
 * Mesa de Blackjack dibujada con Canvas. Solo representa un
 * {@link TableSnapshot}; no contiene reglas ni lógica de juego.
 *
 * <p>La parte estática (madera, tapete, textos) se cachea en un bitmap al
 * cambiar de tamaño; en onDraw solo se dibujan cartas, fichas y placas.
 * Encima se dibuja, si la hay, una única animación en curso (carta que vuela
 * desde el zapato, volteo de la carta oculta, ficha que se desplaza o destello
 * de una mano) y la animación de resultados.</p>
 */
public class TableView extends View {

    /** Posiciones de la mesa para las animaciones. */
    public static final int SLOT_DEALER = -1;
    public static final int SLOT_HUMAN = TableGeometry.SEAT_PLAYER;

    public static final int CHIP_BET = 0;
    public static final int CHIP_DOUBLE = 1;
    public static final int CHIP_INSURANCE = 2;

    private static final int M_NONE = 0;
    private static final int M_DEAL = 1;
    private static final int M_FLIP = 2;
    private static final int M_CHIP = 3;
    private static final int M_FLASH = 4;

    private static final int[] DENOMINATIONS = {500, 100, 25, 5};
    private static final int MAX_STACK = 6;
    private static final float SHOE_TILT = -12f;

    private final TableGeometry geometry = new TableGeometry();
    private final CardPainter cardPainter;
    private final ChipPainter chipPainter;
    private final FeltRenderer feltRenderer;
    private final HandRenderer handRenderer;
    private final SeatRenderer seatRenderer;
    private final RectF handBounds = new RectF();
    private final RectF groupBounds = new RectF();
    private final RectF plateRect = new RectF();
    private final RectF motionRect = new RectF();
    private final RectF motionTarget = new RectF();

    private final String paysText;
    private final String balanceLabel;
    private final String betLabel;
    private final String netLabel;
    private final String sittingOutLabel;
    private final String outOfChipsLabel;
    private final String[] resultTexts = new String[HandView.Result.values().length];
    private final int[] resultFill = new int[HandView.Result.values().length];
    private final int[] resultInk = new int[HandView.Result.values().length];
    private final int calloutFill;
    private final int calloutInk;
    private final int insuranceChipColor;

    @Nullable
    private Bitmap staticLayer;
    @Nullable
    private TableSnapshot snapshot;
    /** Textos y fichas precalculados por asiento (índice de TableGeometry). */
    private final SeatCache[] seatCache = new SeatCache[TableGeometry.SEAT_COUNT];
    @Nullable
    private SeatCache dealerCache;

    private ValueAnimator pulseAnimator;
    private float pulse;

    // Animación puntual en curso.
    private int motion = M_NONE;
    private int motionSlot;
    private int motionHand;
    private int motionCard;
    private int motionChip;
    private float motionP;
    private boolean motionTargetValid;
    private float motionTilt;
    private float motionPivotX;
    private float motionPivotY;
    @Nullable
    private TableCard motionCardModel;
    @Nullable
    private ValueAnimator motionAnimator;

    // Resultados de la ronda.
    @Nullable
    private ValueAnimator resultsAnimator;
    private boolean resultsActive;
    private float resultsP = 1f;

    // Anuncio de la acción de un bot.
    private int calloutSlot = Integer.MIN_VALUE;
    @Nullable
    private String calloutText;
    private long calloutUntil;

    public TableView(Context context) {
        this(context, null);
    }

    public TableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        cardPainter = new CardPainter(context);
        chipPainter = new ChipPainter(context);
        feltRenderer = new FeltRenderer(context, cardPainter);
        handRenderer = new HandRenderer(cardPainter);
        seatRenderer = new SeatRenderer(context, chipPainter);
        paysText = context.getString(R.string.table_pays);
        balanceLabel = context.getString(R.string.label_balance);
        betLabel = context.getString(R.string.label_bet);
        netLabel = context.getString(R.string.label_net);
        sittingOutLabel = context.getString(R.string.label_sitting_out);
        outOfChipsLabel = context.getString(R.string.label_out_of_chips);
        setContentDescription(context.getString(R.string.table_description));

        int white = ContextCompat.getColor(context, R.color.white);
        int dark = ContextCompat.getColor(context, R.color.button_primary_text);
        calloutFill = ContextCompat.getColor(context, R.color.gold);
        calloutInk = dark;
        insuranceChipColor = ContextCompat.getColor(context, R.color.chip_blue);
        setResult(HandView.Result.BLACKJACK, R.string.result_blackjack,
                ContextCompat.getColor(context, R.color.gold), dark);
        setResult(HandView.Result.WIN, R.string.result_win,
                ContextCompat.getColor(context, R.color.result_win), white);
        setResult(HandView.Result.PUSH, R.string.result_push,
                ContextCompat.getColor(context, R.color.result_push), white);
        setResult(HandView.Result.LOSE, R.string.result_lose,
                ContextCompat.getColor(context, R.color.button_danger), white);
        setResult(HandView.Result.BUST, R.string.result_bust,
                ContextCompat.getColor(context, R.color.button_danger), white);
    }

    private void setResult(HandView.Result result, int textRes, int fill, int ink) {
        resultTexts[result.ordinal()] = getContext().getString(textRes);
        resultFill[result.ordinal()] = fill;
        resultInk[result.ordinal()] = ink;
    }

    /** Posición en la mesa del bot número {@code botIndex} (orden de asiento). */
    public static int slotForBot(int botIndex) {
        return TableGeometry.BOT_SEATS[botIndex];
    }

    // --- Estado mostrado ------------------------------------------------------

    /** Sustituye el estado mostrado. Llamar desde el hilo de UI. */
    public void setSnapshot(@Nullable TableSnapshot snapshot) {
        stopResults();
        this.snapshot = snapshot;
        for (int i = 0; i < seatCache.length; i++) {
            seatCache[i] = null;
        }
        dealerCache = null;
        if (snapshot != null) {
            dealerCache = new SeatCache(snapshot.dealer, null, null, null, null, null);
            for (int i = 0; i < snapshot.bots.size(); i++) {
                seatCache[TableGeometry.BOT_SEATS[i]] = buildCache(snapshot.bots.get(i));
            }
            seatCache[TableGeometry.SEAT_PLAYER] = buildCache(snapshot.player);
        }
        updatePulse();
        invalidate();
    }

    private SeatCache buildCache(SeatInfo seat) {
        String line1 = balanceLine(seat.balance);
        String line2;
        if (seat.net != null) {
            line2 = netLabel + " " + formatSigned(seat.net);
        } else if (seat.outOfChips) {
            line2 = outOfChipsLabel;
        } else if (seat.sittingOut) {
            line2 = sittingOutLabel;
        } else {
            line2 = betLabel + " " + formatAmount(seat.bet);
        }
        int k = seat.hands.size();
        String[] bets = new String[k];
        String[] nets = new String[k];
        for (int i = 0; i < k; i++) {
            HandView h = seat.hands.get(i);
            bets[i] = formatAmount(h.bet);
            nets[i] = h.net == null ? null : formatSigned(h.net);
        }
        String insurance = seat.insuranceBet > 0 ? formatAmount(seat.insuranceBet) : null;
        return new SeatCache(seat, line1, line2, bets, nets, insurance);
    }

    private String balanceLine(int balance) {
        return balanceLabel + " " + formatAmount(balance);
    }

    private static String formatSigned(int amount) {
        String sign = amount > 0 ? "+" : amount < 0 ? "−" : "±";
        return sign + formatAmount(Math.abs(amount));
    }

    private static String formatAmount(int amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    // --- Animaciones públicas ---------------------------------------------------

    /** Una carta vuela desde el zapato hasta su sitio en la mano indicada. */
    public void animateCardDeal(int slot, int hand, int card, long durationMs, @Nullable Runnable done) {
        startMotion(M_DEAL, slot, hand, card, 0, durationMs, done);
    }

    /** Volteo de la carta oculta del crupier (segunda carta). */
    public void animateHoleFlip(long durationMs, @Nullable Runnable done) {
        startMotion(M_FLIP, SLOT_DEALER, 0, 1, 0, durationMs, done);
    }

    /** Una ficha se desplaza desde la placa del jugador a la mesa. */
    public void animateChip(int slot, int chipKind, long durationMs, @Nullable Runnable done) {
        startMotion(M_CHIP, slot, 0, -1, chipKind, durationMs, done);
    }

    /** Destello breve alrededor de una mano (mano terminada, división). */
    public void animateHandFlash(int slot, int hand, long durationMs, @Nullable Runnable done) {
        startMotion(M_FLASH, slot, hand, -1, 0, durationMs, done);
    }

    /** Muestra la acción anunciada de un asiento durante un tiempo. */
    public void showCallout(int slot, String text, long durationMs) {
        calloutSlot = slot;
        calloutText = text;
        calloutUntil = SystemClock.uptimeMillis() + durationMs;
        invalidate();
        postInvalidateDelayed(durationMs + 16);
    }

    /**
     * Muestra el estado final de la ronda resaltando los resultados y contando
     * los saldos desde los valores que se veían hasta los reales.
     */
    public void animateResults(TableSnapshot finalSnapshot, long durationMs, @Nullable Runnable done) {
        int[] from = new int[TableGeometry.SEAT_COUNT];
        boolean[] known = new boolean[TableGeometry.SEAT_COUNT];
        for (int i = 0; i < seatCache.length; i++) {
            if (seatCache[i] != null) {
                from[i] = seatCache[i].shownBalance;
                known[i] = true;
            }
        }
        setSnapshot(finalSnapshot);
        for (int i = 0; i < seatCache.length; i++) {
            SeatCache cache = seatCache[i];
            if (cache != null) {
                cache.fromBalance = known[i] ? from[i] : cache.seat.balance;
                setShownBalance(cache, cache.fromBalance);
            }
        }
        resultsActive = true;
        resultsP = 0f;
        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(durationMs);
        animator.addUpdateListener(a -> {
            resultsP = (float) a.getAnimatedValue();
            for (SeatCache cache : seatCache) {
                if (cache != null) {
                    int to = cache.seat.balance;
                    setShownBalance(cache, Math.round(cache.fromBalance + (to - cache.fromBalance) * resultsP));
                }
            }
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (cancelled) {
                    return;
                }
                resultsActive = false;
                resultsAnimator = null;
                for (SeatCache cache : seatCache) {
                    if (cache != null) {
                        setShownBalance(cache, cache.seat.balance);
                    }
                }
                invalidate();
                if (done != null) {
                    done.run();
                }
            }
        });
        resultsAnimator = animator;
        animator.start();
    }

    private void setShownBalance(SeatCache cache, int value) {
        if (cache.shownBalance != value) {
            cache.shownBalance = value;
            cache.line1 = balanceLine(value);
        }
    }

    /** Detiene la animación puntual en curso sin ejecutar su callback. */
    public void cancelMotion() {
        if (motionAnimator != null) {
            ValueAnimator a = motionAnimator;
            motionAnimator = null;
            a.cancel();
        }
        motion = M_NONE;
        calloutSlot = Integer.MIN_VALUE;
        invalidate();
    }

    /** Detiene todas las animaciones (motivo: pausa o destrucción). */
    public void cancelAllAnimations() {
        cancelMotion();
        stopResults();
    }

    private void stopResults() {
        if (resultsAnimator != null) {
            ValueAnimator a = resultsAnimator;
            resultsAnimator = null;
            a.cancel();
        }
        resultsActive = false;
        resultsP = 1f;
    }

    private void startMotion(int type, int slot, int hand, int card, int chip, long durationMs,
                             @Nullable Runnable done) {
        cancelMotion();
        motion = type;
        motionSlot = slot;
        motionHand = hand;
        motionCard = card;
        motionChip = chip;
        motionP = 0f;
        motionCardModel = card >= 0 ? lookupCard(slot, hand, card) : null;

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(durationMs);
        animator.setInterpolator(type == M_FLIP ? new AccelerateDecelerateInterpolator()
                : new DecelerateInterpolator(1.4f));
        animator.addUpdateListener(a -> {
            motionP = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (cancelled || motionAnimator != animator) {
                    return;
                }
                motionAnimator = null;
                motion = M_NONE;
                invalidate();
                if (done != null) {
                    done.run();
                }
            }
        });
        motionAnimator = animator;
        animator.start();
    }

    @Nullable
    private TableCard lookupCard(int slot, int hand, int card) {
        SeatInfo seat = seatAt(slot);
        if (seat == null || hand < 0 || hand >= seat.hands.size()) {
            return null;
        }
        List<TableCard> cards = seat.hands.get(hand).cards;
        return card < cards.size() ? cards.get(card) : null;
    }

    @Nullable
    private SeatInfo seatAt(int slot) {
        if (slot == SLOT_DEALER) {
            return dealerCache == null ? null : dealerCache.seat;
        }
        if (slot < 0 || slot >= seatCache.length || seatCache[slot] == null) {
            return null;
        }
        return seatCache[slot].seat;
    }

    // --- Tamaño y capa estática ---------------------------------------------

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (staticLayer != null) {
            staticLayer.recycle();
            staticLayer = null;
        }
        if (w <= 0 || h <= 0) {
            return;
        }
        geometry.update(w, h);
        staticLayer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        feltRenderer.draw(new Canvas(staticLayer), geometry, paysText,
                getResources().getDisplayMetrics().density);
    }

    // --- Dibujo dinámico ----------------------------------------------------

    @Override
    protected void onDraw(@NonNull Canvas c) {
        if (staticLayer != null) {
            c.drawBitmap(staticLayer, 0f, 0f, null);
        }
        if (geometry.base <= 0f) {
            return;
        }
        motionTargetValid = false;
        for (int i = 0; i < TableGeometry.SEAT_COUNT; i++) {
            drawSeat(c, i);
        }
        if (dealerCache != null) {
            drawDealer(c, dealerCache);
        }
        drawMotionOverlay(c);
    }

    private void drawDealer(Canvas c, SeatCache cache) {
        TableGeometry g = geometry;
        SeatInfo dealer = cache.seat;
        float bottom = g.dealerHand.y + g.cardH / 2f;
        boolean hasCards = drawHands(c, cache, SLOT_DEALER, g.dealerHand.x, bottom, g.cardW, g.cardH,
                g.dealerHandsMaxWidth, 0f, false, false);

        float minHalf = HandRenderer.widthFor(2, g.cardW) / 2f;
        float left = hasCards ? Math.min(groupBounds.left, g.dealerHand.x - minHalf)
                : g.dealerHand.x - minHalf;
        float right = hasCards ? Math.max(groupBounds.right, g.dealerHand.x + minHalf)
                : g.dealerHand.x + minHalf;

        float plateW = g.plateW * 0.8f;
        float plateH = g.base * 0.06f;
        plateRect.set(left - g.gap * 2f - plateW, g.dealerHand.y - plateH / 2f,
                left - g.gap * 2f, g.dealerHand.y + plateH / 2f);
        seatRenderer.drawPlate(c, plateRect, dealer.name, null, null, false, dealer.active, pulse, g.base);

        String score = dealer.hands.isEmpty() ? null : dealer.hands.get(0).score;
        boolean scoreHidden = (motion == M_DEAL || motion == M_FLIP) && motionSlot == SLOT_DEALER;
        if (score != null && !scoreHidden) {
            seatRenderer.drawBadge(c, right + g.gap * 2f + g.base * 0.03f, g.dealerHand.y,
                    score, g.base, false);
        }
    }

    private void drawSeat(Canvas c, int index) {
        TableGeometry g = geometry;
        float x = g.seats[index].x;
        float y = g.seats[index].y;
        SeatCache cache = seatCache[index];
        boolean human = index == TableGeometry.SEAT_PLAYER;
        float scale = human ? g.playerCardScale : 1f;
        float spotR = spotRadius(index);
        boolean active = cache != null && cache.seat.active;

        seatRenderer.drawSpot(c, x, y, spotR, cache != null && !cache.seat.sittingOut, active, pulse);
        if (cache == null) {
            return;
        }
        SeatInfo seat = cache.seat;

        boolean chipMoving = motion == M_CHIP && motionSlot == index;
        if (!(chipMoving && motionChip == CHIP_BET)) {
            seatRenderer.drawBetChips(c, x, y, spotR, cache.chipValues, cache.chipCounts,
                    cache.stacks, cache.chipLabels);
        }
        if (cache.insuranceLabel != null && !(chipMoving && motionChip == CHIP_INSURANCE)) {
            float ix = insuranceX(index);
            float iy = insuranceY(index);
            chipPainter.drawChip(c, ix, iy, spotR * 0.42f, insuranceChipColor, cache.insuranceLabel);
        }

        float handsBottom = y - spotR - g.gap * 1.5f;
        float maxWidth = human ? g.playerHandsMaxWidth : g.botHandsMaxWidth;
        drawHands(c, cache, index, x, handsBottom, g.cardW * scale, g.cardH * scale, maxWidth,
                g.seatTilt[index], true, true);

        float plateW = g.plateW * (human ? 1.08f : 1f);
        float plateTop = y + spotR + g.gap;
        plateRect.set(x - plateW / 2f, plateTop, x + plateW / 2f, plateTop + g.plateH);
        seatRenderer.drawPlate(c, plateRect, seat.name, cache.line1, cache.line2,
                human, active, pulse, g.base);

        if (calloutSlot == index && calloutText != null
                && SystemClock.uptimeMillis() < calloutUntil) {
            seatRenderer.drawResult(c, x, plateRect.top, calloutText, calloutFill, calloutInk,
                    g.base, 1f);
        }
    }

    private float spotRadius(int index) {
        return geometry.spotRadius * (index == TableGeometry.SEAT_PLAYER ? 1.1f : 1f);
    }

    private float insuranceX(int index) {
        return geometry.seats[index].x - spotRadius(index) * 1.75f;
    }

    private float insuranceY(int index) {
        return geometry.seats[index].y + spotRadius(index) * 0.15f;
    }

    /**
     * Dibuja las manos de un asiento una junto a otra, centradas en {@code centerX}
     * y apoyadas en {@code bottom}. Si no caben en {@code maxWidth} se escalan.
     *
     * Con varias manos (división) se usan cartas compactas, la apuesta de cada
     * mano encima y, al liquidar, su resultado neto.
     *
     * @return true si había cartas; {@link #groupBounds} recibe el área ocupada.
     */
    private boolean drawHands(Canvas c, SeatCache cache, int slot, float centerX, float bottom,
                              float cardW, float cardH, float maxWidth, float tilt,
                              boolean drawScores, boolean drawResults) {
        List<HandView> hands = cache.seat.hands;
        int k = hands.size();
        boolean split = k > 1;
        float overlap = split ? HandRenderer.COMPACT_OVERLAP : HandRenderer.OVERLAP;
        float gap = cardW * (split ? 0.18f : 0.28f);
        float total = 0f;
        int nonEmpty = 0;
        for (int i = 0; i < k; i++) {
            int n = hands.get(i).cards.size();
            if (n > 0) {
                total += HandRenderer.widthFor(n, cardW, overlap);
                nonEmpty++;
            }
        }
        if (nonEmpty == 0) {
            return false;
        }
        total += gap * (nonEmpty - 1);
        float s = total > maxWidth ? maxWidth / total : 1f;
        float cw = cardW * s;
        float ch = cardH * s;
        float g = gap * s;
        float x = centerX - total * s / 2f;
        float cy = bottom - ch / 2f;
        float labelScale = Math.max(0.75f, s);
        float pop = resultsActive ? popScale(resultsP) : 1f;
        groupBounds.setEmpty();

        for (int i = 0; i < k; i++) {
            HandView hand = hands.get(i);
            int n = hand.cards.size();
            if (n == 0) {
                continue;
            }
            float w = HandRenderer.widthFor(n, cw, overlap);
            if (split && hand.active) {
                handBounds.set(x, cy - ch / 2f, x + w, cy + ch / 2f);
                seatRenderer.drawHandFrame(c, handBounds, pulse, geometry.base);
            }
            float handTilt = split ? 0f : tilt;
            boolean targeted = (motion == M_DEAL || motion == M_FLIP)
                    && motionSlot == slot && motionHand == i;
            int hidden = targeted ? motionCard : -1;
            handRenderer.draw(c, hand.cards, x, cy, cw, ch, handTilt, split, hidden, motionTarget,
                    handBounds);
            if (targeted && hidden < n) {
                motionTargetValid = true;
                motionTilt = handTilt;
                motionPivotX = handBounds.centerX();
                motionPivotY = cy;
            }
            groupBounds.union(handBounds);

            if (motion == M_FLASH && motionSlot == slot && motionHand == i) {
                seatRenderer.drawHandFrame(c, handBounds, 1f - motionP, geometry.base);
            }
            boolean winner = hand.result == HandView.Result.WIN
                    || hand.result == HandView.Result.BLACKJACK;
            if (resultsActive && winner) {
                seatRenderer.drawHandFrame(c, handBounds,
                        (float) Math.sin(Math.PI * resultsP), geometry.base);
            }

            boolean scoreHidden = targeted && motion == M_DEAL;
            int r = hand.result.ordinal();
            // Mano única liquidada: el resultado va abajo, junto a la puntuación, para
            // no tapar los índices de las cartas.
            boolean bottomResult = drawResults && !split && hand.result != HandView.Result.NONE;
            float scoreX = handBounds.centerX();
            float resultX = handBounds.centerX();
            if (bottomResult) {
                float resultW = seatRenderer.resultWidth(resultTexts[r], geometry.base, labelScale);
                float badgeW = hand.score != null && drawScores
                        ? seatRenderer.badgeWidth(hand.score, geometry.base * labelScale) : 0f;
                float spacing = badgeW > 0f ? geometry.gap : 0f;
                float start = handBounds.centerX() - (badgeW + spacing + resultW) / 2f;
                scoreX = start + badgeW / 2f;
                resultX = start + badgeW + spacing + resultW / 2f;
            }
            if (drawScores && hand.score != null && !scoreHidden) {
                seatRenderer.drawBadge(c, scoreX, handBounds.bottom,
                        hand.score, geometry.base * labelScale, hand.active && split,
                        split ? w * 0.85f : Float.MAX_VALUE);
            }
            if (drawResults) {
                // En manos divididas la etiqueta va justo encima, limitada a su hueco.
                float tagY = handBounds.top - geometry.base * 0.026f * labelScale;
                float slotW = w + g * 0.9f;
                if (split && hand.result != HandView.Result.NONE && cache.handNets[i] != null) {
                    seatRenderer.drawResult(c, handBounds.centerX(), tagY,
                            cache.handNets[i], resultFill[r], resultInk[r], geometry.base,
                            labelScale * pop, slotW);
                } else if (split) {
                    seatRenderer.drawBetTag(c, handBounds.centerX(), tagY,
                            cache.handBets[i], chipPainter.colorFor(hand.bet), geometry.base, labelScale,
                            slotW);
                } else if (bottomResult) {
                    seatRenderer.drawResult(c, resultX, handBounds.bottom, resultTexts[r],
                            resultFill[r], resultInk[r], geometry.base, labelScale * pop);
                }
            }
            x += w + g;
        }
        return true;
    }

    /** Escala de "rebote" para las etiquetas de resultado. */
    private static float popScale(float p) {
        if (p < 0.35f) {
            return 0.4f + 0.8f * (p / 0.35f);         // 0.4 → 1.2
        }
        if (p < 0.6f) {
            return 1.2f - 0.2f * ((p - 0.35f) / 0.25f); // 1.2 → 1.0
        }
        return 1f;
    }

    // --- Capa de animación --------------------------------------------------------

    private void drawMotionOverlay(Canvas c) {
        TableGeometry g = geometry;
        if (motion == M_DEAL && motionTargetValid && motionCardModel != null) {
            float p = motionP;
            float startCx = g.shoeCenter.x - g.cardW * 0.45f;
            float startCy = g.shoeCenter.y;
            float w = motionTarget.width();
            float h = motionTarget.height();
            float cx = startCx + (motionTarget.centerX() - startCx) * p;
            float cy = startCy + (motionTarget.centerY() - startCy) * p;
            motionRect.set(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
            c.save();
            c.rotate(motionTilt * p, motionPivotX, motionPivotY);
            c.rotate(SHOE_TILT * (1f - p), cx, cy);
            drawCard(c, motionCardModel, motionRect);
            c.restore();
        } else if (motion == M_FLIP && motionTargetValid && motionCardModel != null) {
            float p = motionP;
            float sx = Math.max(0.02f, Math.abs(1f - 2f * p));
            c.save();
            c.rotate(motionTilt, motionPivotX, motionPivotY);
            c.scale(sx, 1f, motionTarget.centerX(), motionTarget.centerY());
            if (p < 0.5f) {
                cardPainter.drawBack(c, motionTarget);
            } else {
                drawCard(c, motionCardModel, motionTarget);
            }
            c.restore();
        } else if (motion == M_CHIP && motionSlot >= 0 && motionSlot < TableGeometry.SEAT_COUNT) {
            int slot = motionSlot;
            float x = g.seats[slot].x;
            float y = g.seats[slot].y;
            float spotR = spotRadius(slot);
            float fromX = x;
            float fromY = y + spotR + g.gap + g.plateH * 0.5f;
            float toX = motionChip == CHIP_INSURANCE ? insuranceX(slot) : x;
            float toY = motionChip == CHIP_INSURANCE ? insuranceY(slot) : y;
            float p = motionP;
            float px = fromX + (toX - fromX) * p;
            float py = fromY + (toY - fromY) * p - (float) Math.sin(Math.PI * p) * spotR;
            int color = motionChip == CHIP_INSURANCE ? insuranceChipColor
                    : chipPainter.colorFor(motionChip == CHIP_DOUBLE ? 100 : 25);
            chipPainter.drawChip(c, px, py, spotR * 0.45f, color, "");
        }
    }

    private void drawCard(Canvas c, TableCard card, RectF rect) {
        if (card.faceDown) {
            cardPainter.drawBack(c, rect);
        } else {
            boolean compact = rect.width() < geometry.cardW * 0.8f;
            cardPainter.drawFace(c, rect, card.rank, card.suit, card.isCourt(), compact);
        }
    }

    // --- Pulso del turno activo ---------------------------------------------

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updatePulse();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopPulse();
        cancelAllAnimations();
        super.onDetachedFromWindow();
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (pulseAnimator == null) {
            return;
        }
        if (isVisible) {
            pulseAnimator.resume();
        } else {
            pulseAnimator.pause();
        }
    }

    private void updatePulse() {
        boolean anyActive = false;
        for (SeatCache cache : seatCache) {
            if (cache != null && cache.seat.active) {
                anyActive = true;
                break;
            }
        }
        if (!anyActive || !isAttachedToWindow()) {
            stopPulse();
            pulse = anyActive ? 1f : 0f;
            return;
        }
        if (pulseAnimator != null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ValueAnimator.areAnimatorsEnabled()) {
            pulse = 1f;
            return;
        }
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(1100L);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(a -> {
            pulse = (float) a.getAnimatedValue();
            postInvalidateOnAnimation();
        });
        pulseAnimator.start();
    }

    private void stopPulse() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
    }

    /** Datos derivados de un asiento, calculados fuera de onDraw. */
    private static final class SeatCache {
        final SeatInfo seat;
        @Nullable
        String line1;
        @Nullable
        final String line2;
        final int[] chipValues = new int[DENOMINATIONS.length];
        final int[] chipCounts = new int[DENOMINATIONS.length];
        final String[] chipLabels = new String[DENOMINATIONS.length];
        int stacks;
        /** Apuesta y resultado neto formateados por mano (manos divididas). */
        final String[] handBets;
        final String[] handNets;
        @Nullable
        final String insuranceLabel;
        /** Saldo que se está mostrando (puede ir por detrás del real durante la animación). */
        int shownBalance;
        int fromBalance;

        SeatCache(SeatInfo seat, @Nullable String line1, @Nullable String line2,
                  @Nullable String[] handBets, @Nullable String[] handNets,
                  @Nullable String insuranceLabel) {
            this.seat = seat;
            this.line1 = line1;
            this.line2 = line2;
            this.insuranceLabel = insuranceLabel;
            this.shownBalance = seat.balance;
            int k = seat.hands.size();
            this.handBets = handBets != null ? handBets : new String[k];
            this.handNets = handNets != null ? handNets : new String[k];
            // Representación visual de la apuesta en fichas (no es cálculo de juego).
            int remaining = seat.bet;
            for (int value : DENOMINATIONS) {
                int count = remaining / value;
                if (count > 0 && stacks < 3) {
                    chipValues[stacks] = value;
                    chipCounts[stacks] = Math.min(count, MAX_STACK);
                    chipLabels[stacks] = String.valueOf(value);
                    stacks++;
                    remaining -= count * value;
                }
            }
        }
    }
}
