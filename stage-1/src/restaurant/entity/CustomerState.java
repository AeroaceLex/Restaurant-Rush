package restaurant.entity;

/** Where a customer is in the visit, from arrival to leaving. */
public enum CustomerState {
    WAITING("waiting for a table", true),
    SEATED("seated, waiting to order", true),
    ORDERED("waiting for food", true),
    EATING("eating", false),
    READY_TO_PAY("ready to pay", false),
    PAID("paid and left", false),
    LEFT("left unhappy", false);

    private final String description;
    private final boolean waitingForService;

    CustomerState(String description, boolean waitingForService) {
        this.description = description;
        this.waitingForService = waitingForService;
    }

    public String getDescription() {
        return description;
    }

    /** True while the customer has not been served yet, so their patience is running out. */
    public boolean isWaitingForService() {
        return waitingForService;
    }

    /** True once the customer has gone, whether they paid or not. */
    public boolean hasLeft() {
        return this == PAID || this == LEFT;
    }
}
