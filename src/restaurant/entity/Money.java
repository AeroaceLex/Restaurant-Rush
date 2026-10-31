package restaurant.entity;

/**
 * Helpers for money. All amounts in the game are stored as integer cents so that
 * discounts never introduce floating-point rounding errors.
 */
public final class Money {

    private Money() {
    }

    /** Formats an amount of cents as dollars with two decimals, e.g. 2430 -> "$24.30". */
    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    /**
     * Returns {@code percent}% of {@code cents}, rounded half-up to the nearest cent.
     * For example {@code percentOf(1500, 90)} is 1350.
     */
    public static int percentOf(int cents, int percent) {
        if (cents < 0 || percent < 0) {
            throw new IllegalArgumentException("Amounts and percentages must not be negative");
        }
        return (int) ((cents * (long) percent + 50) / 100);
    }
}
