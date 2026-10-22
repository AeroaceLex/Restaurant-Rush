package restaurant.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The items customers can choose from, in a fixed display order. */
public class Menu {

    private final List<MenuItem> items = new ArrayList<>();

    /** The three dishes of the base setting. */
    public static Menu standard() {
        Menu menu = new Menu();
        menu.add(new MenuItem("Salad", 900, 1));
        menu.add(new MenuItem("Burger", 1500, 1));
        menu.add(new MenuItem("Pasta", 1800, 2));
        return menu;
    }

    public void add(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add a null menu item");
        }
        items.add(item);
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
