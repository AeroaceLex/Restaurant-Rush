package restaurant.entity;

import java.util.List;

/**
 * Two existing menu items sold together. Preparation units are the sum of the two
 * components, and the price is 90% of their combined price, rounded to the nearest cent.
 * A combo is a menu item, so orders, the Chef and pricing handle it without changes.
 */
public class ComboMeal extends MenuItem {

    public static final int PRICE_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName() + " Combo",
                Money.percentOf(first.getPriceCents() + second.getPriceCents(), PRICE_PERCENT),
                first.getPreparationUnits() + second.getPreparationUnits());
        this.first = first;
        this.second = second;
    }

    public List<MenuItem> getComponents() {
        return List.of(first, second);
    }
}
