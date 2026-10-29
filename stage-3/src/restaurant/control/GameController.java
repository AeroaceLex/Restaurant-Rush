package restaurant.control;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import restaurant.boundary.ConsoleUI;
import restaurant.entity.Assignable;
import restaurant.entity.Cashier;
import restaurant.entity.Chef;
import restaurant.entity.Customer;
import restaurant.entity.HappyHour;
import restaurant.entity.Host;
import restaurant.entity.InvalidActionException;
import restaurant.entity.Money;
import restaurant.entity.Restaurant;
import restaurant.entity.TaskCommand;
import restaurant.entity.TaskOption;
import restaurant.entity.Waiter;

/**
 * Runs the game turn by turn in the order the rules require. It asks the UI for the
 * manager's decisions and hands them to staff and the restaurant, which apply the rules.
 */
public class GameController {

    private final GameConfig config;
    private final ConsoleUI ui;
    private final Restaurant restaurant;
    /** Staff the manager directs, in the order they act each turn. */
    private final List<Assignable> directedStaff;
    private final Cashier cashier = new Cashier();

    public GameController(GameConfig config, ConsoleUI ui) {
        this.config = config;
        this.ui = ui;
        this.restaurant = new Restaurant(config.createMenu(), config.getTableCount(), config.getSeed());
        this.directedStaff = List.of(new Host(), new Waiter(), new Chef());
    }

    /** Plays all turns and reports the result. Returns true on victory. */
    public boolean play() {
        ui.showWelcome(config.getMaxTurns(), config.getTableCount(), staffNames(), restaurant.getMenu(),
                config.getArrivalSchedule().getArrivalTurns(), config.getObjective().describe(), config.getSeed());
        for (int turn = 1; turn <= config.getMaxTurns(); turn++) {
            playTurn(turn);
        }
        boolean victory = config.getObjective().isMet(restaurant.getLedger());
        ui.showFinalResult(victory, config.getObjective().evaluate(restaurant.getLedger()), restaurant);
        return victory;
    }

    private void playTurn(int turn) {
        for (Assignable staff : directedStaff) {
            staff.startTurn();
        }
        cashier.startTurn();

        ui.showTurnHeader(turn, config.getMaxTurns(), restaurant.getLedger());

        // Step 1: arrivals
        Optional<Customer> arrival = config.getArrivalSchedule().arrivalFor(turn);
        arrival.ifPresent(customer -> {
            restaurant.admit(customer, turn);
            ui.info("Arrival: " + customer.describe() + " joins the waiting queue.");
        });

        ui.showState(restaurant, staffTasks(), cashierPreview(turn));
        startOfTurnHappyHour();

        // Steps 2 and 3: manager choices: Host, then Waiter, then Chef
        ui.showStep("Manager choices");
        for (Assignable staff : directedStaff) {
            direct(staff, turn);
        }

        // Step 4: eating and payment
        ui.showStep("Eating and payment");
        cashier.collectPayment(restaurant, turn).ifPresent(customer -> ui.info("Cashier collects "
                + Money.format(customer.getOrder().getBillCents()) + " from " + customer.getId()
                + ": PAID, satisfaction " + customer.getRecordedSatisfaction() + " recorded, table freed."));
        for (Customer customer : restaurant.finishEating(turn)) {
            ui.info(customer.getId() + " finishes eating and is READY_TO_PAY (pays next turn at the earliest).");
        }

        // Step 5: waiting and abandonment
        for (Customer customer : restaurant.applyWaitingPenalties()) {
            ui.info(customer.getId() + " (" + customer.getTypeName()
                    + ") runs out of patience and leaves without paying.");
        }

        restaurant.getHappyHour().endTurn();
        ui.showTurnEnd(turn, config.getMaxTurns(), restaurant.getLedger());
    }

    /** Offers Happy Hour while it is unused; while it is active, waiting customers recover. */
    private void startOfTurnHappyHour() {
        HappyHour happyHour = restaurant.getHappyHour();
        ui.showHappyHourStatus(happyHour.describe());
        if (happyHour.isAvailable() && ui.askYesNo("Activate Happy Hour now (y/n)? > ")) {
            try {
                happyHour.activate();
                ui.info("Happy Hour is on: " + happyHour.describe() + ".");
            } catch (InvalidActionException e) {
                ui.error(e.getMessage());
            }
        }
        if (happyHour.isActive()) {
            for (Customer customer : restaurant.recoverWaitingCustomers(HappyHour.SATISFACTION_RECOVERY)) {
                ui.info("Happy Hour: " + customer.getId() + " recovers to satisfaction "
                        + customer.getSatisfaction() + ".");
            }
        }
    }

    /** Asks for one staff member's task until it succeeds or the manager chooses to wait. */
    private void direct(Assignable staff, int turn) {
        while (true) {
            List<TaskOption> options = staff.availableTasks(restaurant);
            if (options.isEmpty()) {
                ui.info(staff.getName() + " has no available task and waits.");
                return;
            }
            String input = ui.askForTask(staff.getName(), options);
            try {
                String commandText = resolveChoice(input, options);
                if (isWait(commandText)) {
                    ui.info(staff.getName() + " waits.");
                    return;
                }
                ui.info(staff.perform(TaskCommand.parse(commandText), restaurant, turn));
                return;
            } catch (InvalidActionException e) {
                ui.error(e.getMessage() + " Choose again (" + staff.getCommandHelp() + ").");
            }
        }
    }

    /** Turns a menu number into its command; anything else is treated as a typed command. */
    private static String resolveChoice(String input, List<TaskOption> options) throws InvalidActionException {
        String trimmed = input.trim();
        if (!trimmed.matches("\\d+")) {
            return trimmed;
        }
        int choice = Integer.parseInt(trimmed);
        if (choice == 0) {
            return "wait";
        }
        if (choice > options.size()) {
            throw new InvalidActionException("There is no option " + choice + ".");
        }
        return options.get(choice - 1).getCommand();
    }

    private static boolean isWait(String command) {
        return command.equalsIgnoreCase("wait") || command.equalsIgnoreCase("w");
    }

    private List<String> staffNames() {
        List<String> names = new ArrayList<>();
        for (Assignable staff : directedStaff) {
            names.add(staff.getName());
        }
        names.add(cashier.getName());
        return names;
    }

    private Map<String, List<TaskOption>> staffTasks() {
        Map<String, List<TaskOption>> tasks = new LinkedHashMap<>();
        for (Assignable staff : directedStaff) {
            tasks.put(staff.getName(), staff.availableTasks(restaurant));
        }
        return tasks;
    }

    private String cashierPreview(int turn) {
        return restaurant.peekNextPayer(turn)
                .map(customer -> "automatic - will charge " + customer.getId() + " "
                        + Money.format(customer.getOrder().getBillCents()))
                .orElse("automatic - nobody is ready to pay yet");
    }
}
