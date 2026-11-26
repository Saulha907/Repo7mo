package com.example.blackjack21.demo;

import com.example.blackjack21.view.paint.SuitPaths;
import com.example.blackjack21.view.table.model.SeatInfo;
import com.example.blackjack21.view.table.model.TableCard;
import com.example.blackjack21.view.table.model.TableSnapshot;

import java.util.Arrays;

/**
 * DATOS FICTICIOS solo para visualizar la mesa. No forman parte del juego:
 * se sustituirán por el estado real cuando exista el motor de Blackjack.
 */
public final class DemoTableData {

    private DemoTableData() {
    }

    public static TableSnapshot create(String dealerName, String playerName) {
        SeatInfo dealer = new SeatInfo(dealerName, 0, 0,
                Arrays.asList(TableCard.up("K", SuitPaths.DIAMOND), TableCard.down()),
                "10", false, false);

        SeatInfo alex = new SeatInfo("Alex", 1000, 50,
                Arrays.asList(TableCard.up("7", SuitPaths.CLUB),
                        TableCard.up("4", SuitPaths.HEART),
                        TableCard.up("9", SuitPaths.SPADE)),
                "20", false, false);

        SeatInfo max = new SeatInfo("Max", 1000, 100,
                Arrays.asList(TableCard.up("Q", SuitPaths.SPADE), TableCard.up("6", SuitPaths.DIAMOND)),
                "16", false, false);

        SeatInfo sam = new SeatInfo("Sam", 1000, 25,
                Arrays.asList(TableCard.up("A", SuitPaths.HEART), TableCard.up("8", SuitPaths.CLUB)),
                "19", false, false);

        SeatInfo player = new SeatInfo(playerName, 1000, 25,
                Arrays.asList(TableCard.up("10", SuitPaths.SPADE), TableCard.up("8", SuitPaths.HEART)),
                "18", true, true);

        return new TableSnapshot(dealer, Arrays.asList(alex, max, sam), player);
    }
}
