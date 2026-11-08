package com.ourcreation.numismaticvillagertrades.client.jei;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TradeListScrollStateTest {
    @Test
    void longListsReachTheLastOfferAndStopAtBothEnds() {
        var scroll = new TradeListScrollState(64, 6);
        assertEquals(0, scroll.firstRow());
        assertEquals(6, scroll.endRow());
        assertTrue(scroll.hasOverflow());

        scroll.scroll(-1000);
        assertEquals(58, scroll.firstRow());
        assertEquals(64, scroll.endRow());
        assertEquals(1, scroll.fraction());

        scroll.scroll(1000);
        assertEquals(0, scroll.firstRow());
        assertEquals(6, scroll.endRow());
        assertEquals(0, scroll.fraction());
    }

    @Test
    void shortListsKeepAllTradesVisibleWithoutScrolling() {
        for (int count : new int[] {0, 1, 5, 6}) {
            var scroll = new TradeListScrollState(count, 6);
            scroll.scroll(-20);
            scroll.scrollToFraction(1);
            assertFalse(scroll.hasOverflow());
            assertEquals(0, scroll.firstRow());
            assertEquals(count, scroll.endRow());
            assertEquals(0, scroll.fraction());
        }
    }

    @Test
    void focusedItemsAreVisibleEvenAtTheEndOfALongList() {
        var scroll = new TradeListScrollState(64, 6);
        for (int focusedRow : new int[] {0, 7, 31, 63}) {
            scroll.reveal(focusedRow);
            assertTrue(scroll.firstRow() <= focusedRow);
            assertTrue(scroll.endRow() > focusedRow);
        }
        scroll.reveal(-1);
        assertEquals(0, scroll.firstRow());
    }

    @Test
    void partialWheelDeltasAccumulateAndScrollbarPositionsStayBounded() {
        var scroll = new TradeListScrollState(30, 6);
        scroll.scroll(-0.2);
        scroll.scroll(-0.2);
        scroll.scroll(-0.2);
        assertEquals(1, scroll.firstRow());

        scroll.scrollToFraction(0.5);
        assertEquals(12, scroll.firstRow());
        assertEquals(18, scroll.endRow());
        scroll.scrollToFraction(2);
        assertEquals(24, scroll.firstRow());
        scroll.scrollToFraction(-1);
        assertEquals(0, scroll.firstRow());
    }
}
