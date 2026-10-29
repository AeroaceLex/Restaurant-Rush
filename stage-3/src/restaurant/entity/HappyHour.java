package restaurant.entity;

/**
 * A pricing policy the manager may switch on once per game, at the start of a turn. It
 * lasts for that turn and the next. Orders taken while it is active get 20% off (after
 * any customer discount), and customers still waiting for a table or food recover 15
 * satisfaction at the start of each active turn.
 */
public class HappyHour {

    public static final int DURATION_TURNS = 2;
    public static final int DISCOUNT_PERCENT = 20;
    public static final int SATISFACTION_RECOVERY = 15;

    private boolean used;
    private int remainingTurns;

    public boolean isAvailable() {
        return !used;
    }

    public boolean isActive() {
        return remainingTurns > 0;
    }

    /** Active turns left, counting the current one. */
    public int getRemainingTurns() {
        return remainingTurns;
    }

    public void activate() throws InvalidActionException {
        if (used) {
            throw new InvalidActionException("Happy Hour can only be used once per game.");
        }
        used = true;
        remainingTurns = DURATION_TURNS;
    }

    /** The bill after Happy Hour, if it is active; unchanged otherwise. */
    public int applyTo(int priceCents) {
        return isActive() ? Money.percentOf(priceCents, 100 - DISCOUNT_PERCENT) : priceCents;
    }

    /** Called once at the end of every turn. */
    public void endTurn() {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
    }

    /** e.g. "ACTIVE - 2 turns left (this turn and next)". */
    public String describe() {
        if (remainingTurns > 1) {
            return "ACTIVE - " + remainingTurns + " turns left (this turn and next)";
        }
        if (remainingTurns == 1) {
            return "ACTIVE - 1 turn left (ends after this turn)";
        }
        return used ? "used" : "available (once per game; 20% off new orders, +15 satisfaction for waiting customers, 2 turns)";
    }
}
