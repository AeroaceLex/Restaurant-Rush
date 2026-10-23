package restaurant.entity;

import java.util.List;

/**
 * Staff the manager gives a task to each turn. The controller works only through this
 * interface, so it does not need to know which roles exist. The Cashier works
 * automatically and does not implement it.
 */
public interface Assignable {

    String getName();

    void startTurn();

    /** Usage summary shown when the manager types something unrecognised. */
    String getCommandHelp();

    /** Every task this staff member could successfully perform right now. */
    List<TaskOption> availableTasks(Restaurant restaurant);

    /**
     * Performs the task, or throws with the reason it is not allowed. A rejected task
     * changes nothing and does not use up the staff member's turn.
     *
     * @return a sentence describing what happened
     */
    String perform(TaskCommand command, Restaurant restaurant, int turn) throws InvalidActionException;
}
