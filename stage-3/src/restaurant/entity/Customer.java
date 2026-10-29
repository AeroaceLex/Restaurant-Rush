package restaurant.entity;

import java.util.List;
import java.util.Random;

/**
 * A customer visiting the restaurant.
 *
 * <p>The visit itself (waiting, seated, ordered, eating, ready to pay, paid or left)
 * is the same for everyone and is kept private here. What differs between kinds of
 * customer (how fast they lose patience, what they order and how they are billed)
 * is supplied by subclasses, so the game never needs to check a customer's type.
 */
public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;

    private final int number;
    private int satisfaction = MAX_SATISFACTION;
    private CustomerState state = CustomerState.WAITING;
    private int arrivalTurn;
    private Table table;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;
    private int recordedSatisfaction = -1;

    protected Customer(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("Customer numbers start at 1");
        }
        this.number = number;
    }

    // ----- Behaviour that differs between kinds of customer -----

    /** A short label for the kind of customer, e.g. "Regular". */
    public abstract String getTypeName();

    /** Satisfaction lost at the end of each turn spent waiting for service. */
    public abstract int getSatisfactionLossPerTurn();

    /**
     * Picks what this customer wants from the menu; the manager never chooses. By default
     * every item is equally likely, drawn from the seeded random source.
     */
    public MenuItem chooseItem(Menu menu, Random random) {
        List<MenuItem> items = menu.getItems();
        return items.get(random.nextInt(items.size()));
    }

    /** The customer's bill for an item with the given list price. Full price by default. */
    public int priceFor(int listPriceCents) {
        return listPriceCents;
    }

    // ----- Queries -----

    public String getId() {
        return "C" + number;
    }

    public int getNumber() {
        return number;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerState getState() {
        return state;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    /** The table the customer sits at, or null if they are not seated. */
    public Table getTable() {
        return table;
    }

    /** The customer's order, or null if none has been taken. */
    public Order getOrder() {
        return order;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    /** Satisfaction recorded at payment, or -1 if the customer has not paid. */
    public int getRecordedSatisfaction() {
        return recordedSatisfaction;
    }

    /** e.g. "C2 (Regular, satisfaction 84)". */
    public String describe() {
        return getId() + " (" + getTypeName() + ", satisfaction " + satisfaction + ")";
    }

    // ----- Visit life cycle, driven by Restaurant -----

    void arrive(int turn) {
        arrivalTurn = turn;
    }

    void seatAt(Table assignedTable) {
        requireState(CustomerState.WAITING);
        table = assignedTable;
        state = CustomerState.SEATED;
    }

    void placeOrder(Order newOrder) {
        requireState(CustomerState.SEATED);
        order = newOrder;
        state = CustomerState.ORDERED;
    }

    void startEating(int turn) {
        requireState(CustomerState.ORDERED);
        servedTurn = turn;
        state = CustomerState.EATING;
    }

    void finishEating(int turn) {
        requireState(CustomerState.EATING);
        readyToPayTurn = turn;
        state = CustomerState.READY_TO_PAY;
    }

    void pay() {
        requireState(CustomerState.READY_TO_PAY);
        recordedSatisfaction = satisfaction;
        table = null;
        state = CustomerState.PAID;
    }

    void leaveUnhappy() {
        if (!state.isWaitingForService()) {
            throw new IllegalStateException(getId() + " is " + state + " and cannot walk out");
        }
        table = null;
        state = CustomerState.LEFT;
    }

    /** Applies one turn of lost patience. Returns true if satisfaction has reached 0. */
    boolean loseSatisfaction() {
        satisfaction = Math.max(0, satisfaction - getSatisfactionLossPerTurn());
        return satisfaction == 0;
    }

    void recoverSatisfaction(int points) {
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + points);
    }

    private void requireState(CustomerState expected) {
        if (state != expected) {
            throw new IllegalStateException(getId() + " is " + state + ", expected " + expected);
        }
    }
}
