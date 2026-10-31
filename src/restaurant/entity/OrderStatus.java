package restaurant.entity;

/** Life cycle of an order. */
public enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    SERVED,
    PAID,
    CANCELLED;

    /** True while the Chef can still spend preparation units on the order. */
    public boolean isCookable() {
        return this == PLACED || this == PREPARING;
    }
}
