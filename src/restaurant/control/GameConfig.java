package restaurant.control;

import restaurant.entity.Menu;
import restaurant.entity.RegularCustomer;

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

    /** The required base setting (Section 2.1 of the brief). */
    public static GameConfig baseSetting(long seed) {
        ArrivalSchedule arrivals = new ArrivalSchedule()
                .add(1, RegularCustomer::new)
                .add(4, RegularCustomer::new)
                .add(7, RegularCustomer::new)
                .add(10, RegularCustomer::new)
                .add(13, RegularCustomer::new)
                .add(16, RegularCustomer::new);
        return new GameConfig(18, 2, seed, arrivals, new Objective(4, 4500, 60));
    }

    public Menu createMenu() {
        return Menu.standard();
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
