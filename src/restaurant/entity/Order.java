package restaurant.entity;

/**
 * One customer's order. The bill is locked when the order is taken, so later
 * pricing changes never affect it.
 */
public class Order {

    private final Customer customer;
    private final MenuItem item;
    private final int billCents;
    private final int turnPlaced;
    private int unitsDone;
    private OrderStatus status = OrderStatus.PLACED;

    Order(Customer customer, MenuItem item, int billCents, int turnPlaced) {
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
        this.turnPlaced = turnPlaced;
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getBillCents() {
        return billCents;
    }

    public int getTurnPlaced() {
        return turnPlaced;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    public int getUnitsRemaining() {
        return item.getPreparationUnits() - unitsDone;
    }

    /** Short status text such as "PREPARING 1/2" or "READY". */
    public String describeStatus() {
        if (status.isCookable()) {
            return status + " " + unitsDone + "/" + item.getPreparationUnits();
        }
        return status.toString();
    }

    /** Spends one preparation unit. The last unit makes the order READY. */
    void prepareOneUnit() {
        if (!status.isCookable()) {
            throw new IllegalStateException("Order is " + status + " and cannot be prepared");
        }
        unitsDone++;
        status = unitsDone >= item.getPreparationUnits() ? OrderStatus.READY : OrderStatus.PREPARING;
    }

    void markServed() {
        requireStatus(OrderStatus.READY);
        status = OrderStatus.SERVED;
    }

    void markPaid() {
        requireStatus(OrderStatus.SERVED);
        status = OrderStatus.PAID;
    }

    /** Cancels an order that was never served. */
    void cancel() {
        if (status == OrderStatus.SERVED || status == OrderStatus.PAID) {
            throw new IllegalStateException("A served order cannot be cancelled");
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Order is " + status + ", expected " + expected);
        }
    }
}
