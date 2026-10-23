package restaurant.entity;

/**
 * Something a customer can order: a name, a list price and the number of
 * preparation units the Chef must spend on it.
 */
public class MenuItem {

    private final String name;
    private final int priceCents;
    private final int preparationUnits;

    public MenuItem(String name, int priceCents, int preparationUnits) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A menu item needs a name");
        }
        if (priceCents <= 0 || preparationUnits <= 0) {
            throw new IllegalArgumentException("Price and preparation units must be positive");
        }
        this.name = name;
        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }

    @Override
    public String toString() {
        return name + " (" + Money.format(priceCents) + ", " + preparationUnits
                + (preparationUnits == 1 ? " unit)" : " units)");
    }
}
