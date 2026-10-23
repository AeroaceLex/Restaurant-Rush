package restaurant.entity;

/** A table holds at most one customer until that customer pays or leaves. */
public class Table {

    private final int number;
    private Customer occupant;

    public Table(int number) {
        this.number = number;
    }

    public String getId() {
        return "T" + number;
    }

    public boolean isFree() {
        return occupant == null;
    }

    /** The seated customer, or null if the table is free. */
    public Customer getOccupant() {
        return occupant;
    }

    void assign(Customer customer) {
        if (!isFree()) {
            throw new IllegalStateException(getId() + " is already occupied");
        }
        occupant = customer;
    }

    void release() {
        occupant = null;
    }
}
