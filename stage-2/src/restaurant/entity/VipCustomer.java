package restaurant.entity;

/** A VIP is more patient (loses 5 per waiting turn) and gets 10% off the bill. */
public class VipCustomer extends Customer {

    public static final int SATISFACTION_LOSS = 5;
    public static final int DISCOUNT_PERCENT = 10;

    public VipCustomer(int number) {
        super(number);
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    /** 10% off the list price, rounded to the nearest cent. */
    @Override
    public int priceFor(int listPriceCents) {
        return Money.percentOf(listPriceCents, 100 - DISCOUNT_PERCENT);
    }
}
