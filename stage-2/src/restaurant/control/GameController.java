package restaurant.control;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import restaurant.boundary.ConsoleUI;
import restaurant.entity.Assignable;
import restaurant.entity.Cashier;
import restaurant.entity.Chef;
import restaurant.entity.Customer;
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
        this.directedStaff = List.of(new Waiter(), new Chef());
    }

    /** Plays all turns and reports the result. Returns true on victory. */
    public boolean play() {
        ui.showWelcome(config.getMaxTurns(), config.getTableCount(), restaurant.getMenu(),
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

        // Steps 2 and 3: manager choices, Waiter before Chef
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

        ui.showTurnEnd(turn, config.getMaxTurns(), restaurant.getLedger());
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
