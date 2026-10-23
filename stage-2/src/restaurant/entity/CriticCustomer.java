package restaurant.entity;

import java.util.Random;

/**
 * A food critic is impatient (loses 12 per waiting turn) and pays full price.
 *
 * <p>Order preference: a critic always reviews the kitchen's showpiece, so they order the
 * most expensive item on the menu (the earlier-listed item wins a tie) and ignore the
 * random source. With the standard menu this is always Pasta, the dish that takes the
 * Chef longest, so a critic is the hardest customer to keep happy.
 */
public class CriticCustomer extends Customer {

    public static final int SATISFACTION_LOSS = 12;

    public CriticCustomer(int number) {
        super(number);
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS;
    }

    @Override
    public MenuItem chooseItem(Menu menu, Random random) {
        MenuItem choice = null;
        for (MenuItem item : menu.getItems()) {
            if (choice == null || item.getPriceCents() > choice.getPriceCents()) {
                choice = item;
            }
        }
        return choice;
    }
}
