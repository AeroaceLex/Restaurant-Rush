package restaurant.entity;

import java.util.ArrayList;
import java.util.List;

/** The Waiter seats a queued customer, takes an order or serves a READY dish: one task per turn. */
public class Waiter extends Staff implements Assignable {

    private static final String HELP = "seat <customer> [table], order <customer>, serve <customer>, wait";

    public Waiter() {
        super("Waiter");
    }

    @Override
    public String getCommandHelp() {
        return HELP;
    }

    @Override
    public List<TaskOption> availableTasks(Restaurant restaurant) {
        List<TaskOption> options = new ArrayList<>();
        if (!hasTaskAvailable()) {
            return options;
        }
        for (Customer customer : restaurant.getWaitingQueue()) {
            for (Table table : restaurant.getFreeTables()) {
                options.add(new TaskOption("seat " + customer.getId() + " " + table.getId(),
                        "seat " + customer.describe() + " at " + table.getId()));
            }
        }
        for (Customer customer : restaurant.getCustomersIn(CustomerState.SEATED)) {
            options.add(new TaskOption("order " + customer.getId(),
                    "take the order of " + customer.describe()));
        }
        for (Customer customer : restaurant.getCustomersIn(CustomerState.ORDERED)) {
            Order order = customer.getOrder();
            if (order.getStatus() == OrderStatus.READY) {
                options.add(new TaskOption("serve " + customer.getId(),
                        "serve " + order.getItem().getName() + " to " + customer.describe()));
            }
        }
        return options;
    }

    @Override
    public String perform(TaskCommand command, Restaurant restaurant, int turn) throws InvalidActionException {
        switch (command.getVerb()) {
            case "seat":
                return seat(command, restaurant);
            case "order":
                return takeOrder(command, restaurant, turn);
            case "serve":
                return serve(command, restaurant, turn);
            default:
                throw new InvalidActionException("The Waiter cannot '" + command.getVerb() + "'.");
        }
    }

    private String seat(TaskCommand command, Restaurant restaurant) throws InvalidActionException {
        command.requireArguments(1, 2, "seat <customer> [table]");
        checkTaskAvailable();
        Customer customer = command.customer(restaurant);
        Table table = command.tableOrFirstFree(restaurant);
        restaurant.seat(customer, table);
        markTaskUsed();
        return "Waiter seats " + customer.getId() + " at " + table.getId() + ".";
    }

    private String takeOrder(TaskCommand command, Restaurant restaurant, int turn) throws InvalidActionException {
        command.requireArguments(1, 1, "order <customer>");
        checkTaskAvailable();
        Customer customer = command.customer(restaurant);
        Order order = restaurant.takeOrder(customer, turn);
        markTaskUsed();
        return "Waiter takes " + customer.getId() + "'s order: " + customer.getId() + " chooses "
                + order.getItem() + ". Bill locked at " + Money.format(order.getBillCents()) + ".";
    }

    private String serve(TaskCommand command, Restaurant restaurant, int turn) throws InvalidActionException {
        command.requireArguments(1, 1, "serve <customer>");
        checkTaskAvailable();
        Customer customer = command.customer(restaurant);
        Order order = restaurant.serve(customer, turn);
        markTaskUsed();
        return "Waiter serves " + order.getItem().getName() + " to " + customer.getId()
                + ": order SERVED. " + customer.getId() + " starts eating.";
    }
}
