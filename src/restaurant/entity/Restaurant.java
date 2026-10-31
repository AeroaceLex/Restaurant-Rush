package restaurant.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * The restaurant floor: tables, the waiting queue, every customer who has arrived and
 * the payment queue. It is the only object allowed to move a customer through the
 * visit, and it checks each request against the rules before changing anything, so a
 * rejected request leaves the game exactly as it was.
 */
public class Restaurant {

    private final Menu menu;
    private final List<Table> tables = new ArrayList<>();
    private final Deque<Customer> waitingQueue = new ArrayDeque<>();
    private final List<Customer> customers = new ArrayList<>();
    private final Deque<Customer> paymentQueue = new ArrayDeque<>();
    private final Ledger ledger = new Ledger();
    private final long choiceSeed;

    /**
     * @param choiceSeed seed for customers' food choices; the same seed always gives the
     *                   same choices, whatever order the manager takes orders in
     */
    public Restaurant(Menu menu, int tableCount, long choiceSeed) {
        if (tableCount <= 0) {
            throw new IllegalArgumentException("A restaurant needs at least one table");
        }
        this.menu = menu;
        this.choiceSeed = choiceSeed;
        for (int i = 1; i <= tableCount; i++) {
            tables.add(new Table(i));
        }
    }

    // ----- Queries -----

    public Menu getMenu() {
        return menu;
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public List<Table> getFreeTables() {
        List<Table> free = new ArrayList<>();
        for (Table table : tables) {
            if (table.isFree()) {
                free.add(table);
            }
        }
        return free;
    }

    /** Customers waiting for a table, first in line first. */
    public List<Customer> getWaitingQueue() {
        return new ArrayList<>(waitingQueue);
    }

    /** Everyone who has arrived so far, in arrival order. */
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public List<Customer> getCustomersIn(CustomerState state) {
        List<Customer> result = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getState() == state) {
                result.add(customer);
            }
        }
        return result;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public Customer findCustomer(String id) throws InvalidActionException {
        for (Customer customer : customers) {
            if (customer.getId().equalsIgnoreCase(id)) {
                return customer;
            }
        }
        throw new InvalidActionException("There is no customer " + id + ".");
    }

    public Table findTable(String id) throws InvalidActionException {
        for (Table table : tables) {
            if (table.getId().equalsIgnoreCase(id)) {
                return table;
            }
        }
        throw new InvalidActionException("There is no table " + id + ".");
    }

    public Table firstFreeTable() throws InvalidActionException {
        List<Table> free = getFreeTables();
        if (free.isEmpty()) {
            throw new InvalidActionException("No table is free.");
        }
        return free.get(0);
    }

    /** The customer the Cashier would charge this turn, if any. */
    public Optional<Customer> peekNextPayer(int turn) {
        Customer next = paymentQueue.peekFirst();
        if (next == null || next.getReadyToPayTurn() >= turn) {
            return Optional.empty();
        }
        return Optional.of(next);
    }

    // ----- Step 1: arrivals -----

    public void admit(Customer customer, int turn) {
        customer.arrive(turn);
        customers.add(customer);
        waitingQueue.addLast(customer);
    }

    // ----- Steps 2-3: staff tasks -----

    public void seat(Customer customer, Table table) throws InvalidActionException {
        if (customer.getState() != CustomerState.WAITING) {
            throw new InvalidActionException(customer.getId() + " cannot be seated: they are "
                    + customer.getState().getDescription() + ".");
        }
        if (!table.isFree()) {
            throw new InvalidActionException(table.getId() + " is occupied by "
                    + table.getOccupant().getId() + ".");
        }
        waitingQueue.remove(customer);
        table.assign(customer);
        customer.seatAt(table);
    }

    /** The customer chooses their own food; the bill is fixed now. */
    public Order takeOrder(Customer customer, int turn) throws InvalidActionException {
        if (customer.getState() != CustomerState.SEATED) {
            throw new InvalidActionException(customer.getId() + " cannot order: they are "
                    + customer.getState().getDescription() + ".");
        }
        MenuItem item = customer.chooseItem(menu, randomFor(customer));
        Order order = new Order(customer, item, priceOrder(customer, item), turn);
        customer.placeOrder(order);
        return order;
    }

    public Order prepare(Customer customer) throws InvalidActionException {
        Order order = customer.getOrder();
        if (order == null) {
            throw new InvalidActionException(customer.getId() + " has no order for the kitchen.");
        }
        if (!order.getStatus().isCookable()) {
            throw new InvalidActionException(customer.getId() + "'s " + order.getItem().getName()
                    + " is " + order.getStatus() + " and needs no more preparation.");
        }
        order.prepareOneUnit();
        return order;
    }

    public Order serve(Customer customer, int turn) throws InvalidActionException {
        Order order = customer.getOrder();
        if (order == null) {
            throw new InvalidActionException(customer.getId() + " has no order to serve.");
        }
        if (order.getStatus() != OrderStatus.READY) {
            throw new InvalidActionException(customer.getId() + "'s " + order.getItem().getName()
                    + " cannot be served: it is " + order.describeStatus() + ", not READY.");
        }
        order.markServed();
        customer.startEating(turn);
        ledger.recordServed();
        return order;
    }

    // ----- Step 4: eating and payment -----

    /**
     * Charges the customer who has been ready to pay the longest, if they became ready
     * on an earlier turn. Frees their table and records their satisfaction.
     */
    public Optional<Customer> collectNextPayment(int turn) {
        Optional<Customer> next = peekNextPayer(turn);
        next.ifPresent(customer -> {
            paymentQueue.removeFirst();
            Table table = customer.getTable();
            customer.getOrder().markPaid();
            customer.pay();
            table.release();
            ledger.recordPayment(customer.getOrder().getBillCents(), customer.getRecordedSatisfaction());
        });
        return next;
    }

    /** Customers served on an earlier turn finish eating and join the payment queue. */
    public List<Customer> finishEating(int turn) {
        List<Customer> finished = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getState() == CustomerState.EATING && customer.getServedTurn() < turn) {
                customer.finishEating(turn);
                paymentQueue.addLast(customer);
                finished.add(customer);
            }
        }
        return finished;
    }

    // ----- Step 5: waiting and abandonment -----

    /**
     * Everyone not yet served loses satisfaction. Customers who reach 0 walk out:
     * their unfinished order is cancelled and their table freed.
     *
     * @return the customers who left this turn
     */
    public List<Customer> applyWaitingPenalties() {
        List<Customer> departed = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getState().isWaitingForService() && customer.loseSatisfaction()) {
                walkOut(customer);
                departed.add(customer);
            }
        }
        return departed;
    }

    private void walkOut(Customer customer) {
        waitingQueue.remove(customer);
        if (customer.getTable() != null) {
            customer.getTable().release();
        }
        if (customer.getOrder() != null) {
            customer.getOrder().cancel();
        }
        customer.leaveUnhappy();
        ledger.recordUnhappyDeparture();
    }

    // ----- Pricing and choice -----

    private int priceOrder(Customer customer, MenuItem item) {
        return customer.priceFor(item.getPriceCents());
    }

    /** Each customer gets their own random source so choices do not depend on order-taking order. */
    private Random randomFor(Customer customer) {
        return new Random(choiceSeed ^ (customer.getNumber() * 0x9E3779B97F4A7C15L));
    }
}
