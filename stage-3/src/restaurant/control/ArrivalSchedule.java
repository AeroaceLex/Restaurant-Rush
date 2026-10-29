package restaurant.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.IntFunction;

import restaurant.entity.Customer;

/**
 * Which kind of customer arrives on which turn. Each entry is a factory such as
 * {@code RegularCustomer::new}, so new kinds of customer only need registering here.
 */
public class ArrivalSchedule {

    private final Map<Integer, IntFunction<? extends Customer>> arrivals = new TreeMap<>();
    private int nextCustomerNumber = 1;

    public ArrivalSchedule add(int turn, IntFunction<? extends Customer> factory) {
        if (arrivals.containsKey(turn)) {
            throw new IllegalArgumentException("Only one customer arrives per turn (turn " + turn + ")");
        }
        arrivals.put(turn, factory);
        return this;
    }

    /** Creates the customer arriving on this turn, numbered in arrival order. Call once per turn. */
    public Optional<Customer> arrivalFor(int turn) {
        IntFunction<? extends Customer> factory = arrivals.get(turn);
        if (factory == null) {
            return Optional.empty();
        }
        return Optional.of(factory.apply(nextCustomerNumber++));
    }

    public List<Integer> getArrivalTurns() {
        return new ArrayList<>(arrivals.keySet());
    }
}
