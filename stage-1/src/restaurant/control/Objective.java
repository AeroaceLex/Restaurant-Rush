package restaurant.control;

import java.util.ArrayList;
import java.util.List;

import restaurant.entity.Ledger;
import restaurant.entity.Money;

/** The three targets that decide victory or defeat after the last turn. */
public class Objective {

    /** One target and whether it was met. */
    public static final class TargetCheck {
        private final String name;
        private final String target;
        private final String actual;
        private final boolean met;

        TargetCheck(String name, String target, String actual, boolean met) {
            this.name = name;
            this.target = target;
            this.actual = actual;
            this.met = met;
        }

        public String getName() {
            return name;
        }

        public String getTarget() {
            return target;
        }

        public String getActual() {
            return actual;
        }

        public boolean isMet() {
            return met;
        }
    }

    private final int minPaidCustomers;
    private final int minRevenueCents;
    private final int minAverageSatisfaction;

    public Objective(int minPaidCustomers, int minRevenueCents, int minAverageSatisfaction) {
        this.minPaidCustomers = minPaidCustomers;
        this.minRevenueCents = minRevenueCents;
        this.minAverageSatisfaction = minAverageSatisfaction;
    }

    public List<TargetCheck> evaluate(Ledger ledger) {
        List<TargetCheck> checks = new ArrayList<>();
        checks.add(new TargetCheck("Paid customers", ">= " + minPaidCustomers,
                String.valueOf(ledger.getPaidCount()), ledger.getPaidCount() >= minPaidCustomers));
        checks.add(new TargetCheck("Revenue", ">= " + Money.format(minRevenueCents),
                Money.format(ledger.getRevenueCents()), ledger.getRevenueCents() >= minRevenueCents));
        checks.add(new TargetCheck("Average satisfaction of paid customers", ">= " + minAverageSatisfaction,
                ledger.formatAverageSatisfaction(), ledger.averageSatisfactionAtLeast(minAverageSatisfaction)));
        return checks;
    }

    public boolean isMet(Ledger ledger) {
        for (TargetCheck check : evaluate(ledger)) {
            if (!check.isMet()) {
                return false;
            }
        }
        return true;
    }

    public String describe() {
        return "at least " + minPaidCustomers + " paid customers AND revenue at least "
                + Money.format(minRevenueCents) + " AND average satisfaction of paid customers at least "
                + minAverageSatisfaction;
    }
}
