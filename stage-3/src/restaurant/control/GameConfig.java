package restaurant.control;

import restaurant.entity.ComboMeal;
import restaurant.entity.CriticCustomer;
import restaurant.entity.Menu;
import restaurant.entity.MenuItem;
import restaurant.entity.RegularCustomer;
import restaurant.entity.VipCustomer;

/** Everything that defines one game: length, tables, arrivals, menu, targets and seed. */
public class GameConfig {

    public static final long DEFAULT_SEED = 2002;

    private final int maxTurns;
    private final int tableCount;
    private final long seed;
    private final ArrivalSchedule arrivalSchedule;
    private final Objective objective;

    public GameConfig(int maxTurns, int tableCount, long seed, ArrivalSchedule arrivalSchedule, Objective objective) {
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.seed = seed;
        this.arrivalSchedule = arrivalSchedule;
        this.objective = objective;
    }

    /**
     * The required base setting (Section 2.1 of the brief). Two of the six regular
     * arrivals are replaced by a VIP (turn 4) and a Critic (turn 7).
     */
    public static GameConfig baseSetting(long seed) {
        ArrivalSchedule arrivals = new ArrivalSchedule()
                .add(1, RegularCustomer::new)
                .add(4, VipCustomer::new)
                .add(7, CriticCustomer::new)
                .add(10, RegularCustomer::new)
                .add(13, RegularCustomer::new)
                .add(16, RegularCustomer::new);
        return new GameConfig(18, 2, seed, arrivals, new Objective(4, 4500, 60));
    }

    /** The three dishes plus two combos built from them. */
    public Menu createMenu() {
        Menu menu = Menu.standard();
        MenuItem salad = menu.findByName("Salad");
        menu.add(new ComboMeal(salad, menu.findByName("Burger")));
        menu.add(new ComboMeal(salad, menu.findByName("Pasta")));
        return menu;
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public long getSeed() {
        return seed;
    }

    public ArrivalSchedule getArrivalSchedule() {
        return arrivalSchedule;
    }

    public Objective getObjective() {
        return objective;
    }
}
