package restaurant.entity;

import java.util.List;
import java.util.Random;

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

    /** Picks any menu item with equal probability, using the seeded random source. */
    @Override
    public MenuItem chooseItem(Menu menu, Random random) {
        List<MenuItem> items = menu.getItems();
        return items.get(random.nextInt(items.size()));
    }
}
