package restaurant.entity;

import java.util.ArrayList;
import java.util.List;

/** Seating logic shared by every staff member who can seat customers (Host and Waiter). */
final class Seating {

    static final String USAGE = "seat <customer> [table]";

    private Seating() {
    }

    /** One option per queued customer and free table, first in line first. */
    static List<TaskOption> options(Restaurant restaurant) {
        List<TaskOption> options = new ArrayList<>();
        for (Customer customer : restaurant.getWaitingQueue()) {
            for (Table table : restaurant.getFreeTables()) {
                options.add(new TaskOption("seat " + customer.getId() + " " + table.getId(),
                        "seat " + customer.describe() + " at " + table.getId()));
            }
        }
        return options;
    }

    /** Seats the customer named in the command; returns the table used. */
    static Table seat(TaskCommand command, Restaurant restaurant) throws InvalidActionException {
        command.requireArguments(1, 2, USAGE);
        Customer customer = command.customer(restaurant);
        Table table = command.tableOrFirstFree(restaurant);
        restaurant.seat(customer, table);
        return table;
    }
}
