package restaurant.entity;

/** Running totals used for the end-of-turn report and the final result. */
public class Ledger {

    private int revenueCents;
    private int paidCount;
    private int recordedSatisfactionTotal;
    private int servedCount;
    private int unhappyDepartures;

    void recordServed() {
        servedCount++;
    }

    void recordPayment(int billCents, int satisfactionAtPayment) {
        revenueCents += billCents;
        paidCount++;
        recordedSatisfactionTotal += satisfactionAtPayment;
    }

    void recordUnhappyDeparture() {
        unhappyDepartures++;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    public boolean hasPayments() {
        return paidCount > 0;
    }

    /** Average satisfaction recorded at payment. Only meaningful when {@link #hasPayments()}. */
    public double getAverageSatisfaction() {
        return hasPayments() ? (double) recordedSatisfactionTotal / paidCount : 0.0;
    }

    /** Exact integer comparison, so 60.0 counts as meeting a target of 60. */
    public boolean averageSatisfactionAtLeast(int target) {
        return hasPayments() && recordedSatisfactionTotal >= target * paidCount;
    }

    /** e.g. "76.0", or "N/A" if nobody has paid. */
    public String formatAverageSatisfaction() {
        return hasPayments() ? String.format("%.1f", getAverageSatisfaction()) : "N/A";
    }
}
