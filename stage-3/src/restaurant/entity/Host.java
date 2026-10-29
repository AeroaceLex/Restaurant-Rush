package restaurant.entity;

import java.util.List;

/** The Host seats at most one queued customer per turn, in addition to the Waiter's task. */
public class Host extends Staff implements Assignable {

    private static final String HELP = Seating.USAGE + ", wait";

    public Host() {
        super("Host");
    }

    @Override
    public String getCommandHelp() {
        return HELP;
    }

    @Override
    public List<TaskOption> availableTasks(Restaurant restaurant) {
        return hasTaskAvailable() ? Seating.options(restaurant) : List.of();
    }

    @Override
    public String perform(TaskCommand command, Restaurant restaurant, int turn) throws InvalidActionException {
        if (!command.getVerb().equals("seat")) {
            throw new InvalidActionException("The Host can only seat customers.");
        }
        checkTaskAvailable();
        Table table = Seating.seat(command, restaurant);
        markTaskUsed();
        return "Host seats " + table.getOccupant().getId() + " at " + table.getId() + ".";
    }
}
