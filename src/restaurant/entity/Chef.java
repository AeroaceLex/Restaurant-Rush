package restaurant.entity;

import java.util.ArrayList;
import java.util.List;

/** The Chef advances one order by one preparation unit per turn. */
public class Chef extends Staff implements Assignable {

    private static final String HELP = "cook <customer>, wait";

    public Chef() {
        super("Chef");
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
        for (Customer customer : restaurant.getCustomersIn(CustomerState.ORDERED)) {
            Order order = customer.getOrder();
            if (order.getStatus().isCookable()) {
                options.add(new TaskOption("cook " + customer.getId(),
                        "work on " + customer.getId() + "'s " + order.getItem().getName()
                                + " (" + order.describeStatus() + ")"));
            }
        }
        return options;
    }

    @Override
    public String perform(TaskCommand command, Restaurant restaurant, int turn) throws InvalidActionException {
        if (!command.getVerb().equals("cook")) {
            throw new InvalidActionException("The Chef cannot '" + command.getVerb() + "'.");
        }
        command.requireArguments(1, 1, "cook <customer>");
        checkTaskAvailable();
        Customer customer = command.customer(restaurant);
        Order order = restaurant.prepare(customer);
        markTaskUsed();
        String result = "Chef works on " + customer.getId() + "'s " + order.getItem().getName()
                + ": unit " + order.getUnitsDone() + " of " + order.getItem().getPreparationUnits() + " done.";
        if (order.getStatus() == OrderStatus.READY) {
            result += " The order is READY (it can be served next turn at the earliest).";
        }
        return result;
    }
}
