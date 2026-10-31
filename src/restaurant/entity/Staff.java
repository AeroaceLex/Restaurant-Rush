package restaurant.entity;

/**
 * A member of staff. Every staff member can do a limited amount of work per turn;
 * this class keeps that budget so no subclass can exceed it.
 */
public abstract class Staff {

    private final String name;
    private boolean taskUsed;

    protected Staff(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Gives the staff member a fresh task for the new turn. */
    public void startTurn() {
        taskUsed = false;
    }

    public boolean hasTaskAvailable() {
        return !taskUsed;
    }

    /** Call before attempting a task, so an exhausted budget is reported clearly. */
    protected void checkTaskAvailable() throws InvalidActionException {
        if (taskUsed) {
            throw new InvalidActionException(name + " has already completed a task this turn.");
        }
    }

    /** Call only after the task succeeded, so a rejected task never uses up the turn. */
    protected void markTaskUsed() {
        taskUsed = true;
    }
}
