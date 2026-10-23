package restaurant.entity;

/** An ordinary customer: loses 8 satisfaction per waiting turn and pays full price. */
public class RegularCustomer extends Customer {

    public static final int SATISFACTION_LOSS = 8;

    public RegularCustomer(int number) {
        super(number);
    }

    @Override
    public String getTypeName() {
        return "Regular";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }
}
