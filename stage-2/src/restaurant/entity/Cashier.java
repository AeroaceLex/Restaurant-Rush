package restaurant.entity;

import java.util.Optional;

/** The Cashier works automatically, charging at most one customer per turn, oldest first. */
public class Cashier extends Staff {

    public Cashier() {
        super("Cashier");
    }

    /** @return the customer who paid, if anyone was ready */
    public Optional<Customer> collectPayment(Restaurant restaurant, int turn) {
        if (!hasTaskAvailable()) {
            return Optional.empty();
        }
        Optional<Customer> paid = restaurant.collectNextPayment(turn);
        if (paid.isPresent()) {
            markTaskUsed();
        }
        return paid;
    }
}
