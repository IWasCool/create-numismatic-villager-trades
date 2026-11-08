package com.ourcreation.numismaticvillagertrades.client.jei;

final class TradeListScrollState {
    private final int totalRows;
    private final int visibleRows;
    private double offset;

    TradeListScrollState(int totalRows, int visibleRows) {
        this.totalRows = totalRows;
        this.visibleRows = visibleRows;
    }

    int firstRow() {
        return (int) Math.round(offset);
    }

    int endRow() {
        return Math.min(firstRow() + visibleRows, totalRows);
    }

    boolean hasOverflow() {
        return totalRows > visibleRows;
    }

    double fraction() {
        return hasOverflow() ? offset / (totalRows - visibleRows) : 0;
    }

    void scroll(double wheelDelta) {
        setOffset(offset - wheelDelta);
    }

    void scrollToFraction(double fraction) {
        setOffset(fraction * Math.max(0, totalRows - visibleRows));
    }

    void reveal(int row) {
        setOffset(row);
    }

    private void setOffset(double offset) {
        this.offset = Math.max(0, Math.min(offset, Math.max(0, totalRows - visibleRows)));
    }
}
