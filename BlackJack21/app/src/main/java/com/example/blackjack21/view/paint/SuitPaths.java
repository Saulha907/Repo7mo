package com.example.blackjack21.view.paint;

import android.graphics.Matrix;
import android.graphics.Path;

/**
 * Siluetas vectoriales de los cuatro palos, definidas en un cuadrado unitario
 * y escaladas al tamaño solicitado. Uso exclusivo desde el hilo de UI.
 */
public final class SuitPaths {

    public static final int SPADE = 0;
    public static final int HEART = 1;
    public static final int DIAMOND = 2;
    public static final int CLUB = 3;

    private static final Path[] UNIT = {spade(), heart(), diamond(), club()};
    private static final Matrix MATRIX = new Matrix();

    private SuitPaths() {
    }

    public static boolean isRed(int suit) {
        return suit == HEART || suit == DIAMOND;
    }

    /** Copia en {@code out} el palo escalado a un cuadrado de lado {@code size}. */
    public static void build(int suit, float left, float top, float size, Path out) {
        out.set(UNIT[suit]);
        MATRIX.setScale(size, size);
        MATRIX.postTranslate(left, top);
        out.transform(MATRIX);
    }

    /** Igual que {@link #build} pero centrado en (cx, cy). */
    public static void buildCentered(int suit, float cx, float cy, float size, Path out) {
        build(suit, cx - size / 2f, cy - size / 2f, size, out);
    }

    private static Path heart() {
        Path p = new Path();
        p.moveTo(0.5f, 0.95f);
        p.cubicTo(0.1f, 0.66f, -0.04f, 0.36f, 0.18f, 0.15f);
        p.cubicTo(0.33f, 0.02f, 0.47f, 0.1f, 0.5f, 0.27f);
        p.cubicTo(0.53f, 0.1f, 0.67f, 0.02f, 0.82f, 0.15f);
        p.cubicTo(1.04f, 0.36f, 0.9f, 0.66f, 0.5f, 0.95f);
        p.close();
        return p;
    }

    private static Path spade() {
        Path p = new Path();
        p.moveTo(0.5f, 0.02f);
        p.cubicTo(0.9f, 0.33f, 1.04f, 0.58f, 0.82f, 0.73f);
        p.cubicTo(0.67f, 0.84f, 0.53f, 0.78f, 0.5f, 0.66f);
        p.cubicTo(0.47f, 0.78f, 0.33f, 0.84f, 0.18f, 0.73f);
        p.cubicTo(-0.04f, 0.58f, 0.1f, 0.33f, 0.5f, 0.02f);
        p.close();
        addStem(p);
        return p;
    }

    private static Path diamond() {
        Path p = new Path();
        p.moveTo(0.5f, 0.02f);
        p.quadTo(0.66f, 0.3f, 0.88f, 0.5f);
        p.quadTo(0.66f, 0.7f, 0.5f, 0.98f);
        p.quadTo(0.34f, 0.7f, 0.12f, 0.5f);
        p.quadTo(0.34f, 0.3f, 0.5f, 0.02f);
        p.close();
        return p;
    }

    private static Path club() {
        Path p = new Path();
        p.addCircle(0.5f, 0.26f, 0.21f, Path.Direction.CW);
        p.addCircle(0.27f, 0.56f, 0.21f, Path.Direction.CW);
        p.addCircle(0.73f, 0.56f, 0.21f, Path.Direction.CW);
        p.addCircle(0.5f, 0.5f, 0.12f, Path.Direction.CW);
        addStem(p);
        return p;
    }

    private static void addStem(Path p) {
        p.moveTo(0.5f, 0.58f);
        p.quadTo(0.53f, 0.86f, 0.7f, 0.98f);
        p.lineTo(0.3f, 0.98f);
        p.quadTo(0.47f, 0.86f, 0.5f, 0.58f);
        p.close();
    }
}
