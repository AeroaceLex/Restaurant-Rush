package restaurant.boundary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import restaurant.control.Objective;
import restaurant.entity.Customer;
import restaurant.entity.Ledger;
import restaurant.entity.Menu;
import restaurant.entity.MenuItem;
import restaurant.entity.Money;
import restaurant.entity.Order;
import restaurant.entity.Restaurant;
import restaurant.entity.Table;
import restaurant.entity.TaskOption;

/**
 * Command-line boundary. It only displays game state and collects the manager's
 * input; every rule lives in the control and entity classes.
 */
public class ConsoleUI {

    private static final String HEAVY_RULE = "=".repeat(78);
    private static final String LIGHT_RULE = "-".repeat(78);

    private final BufferedReader in;
    private final PrintStream out;
    private final boolean echoInput;
    private boolean inputExhausted;

    /**
     * @param echoInput print each line read, so transcripts of scripted runs show the choices
     */
    public ConsoleUI(InputStream in, PrintStream out, boolean echoInput) {
        this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
        this.echoInput = echoInput;
    }

    // ----- Output -----

    public void showWelcome(int maxTurns, int tableCount, List<String> staffNames, Menu menu,
                            List<Integer> arrivalTurns, String objective, long seed) {
        out.println(HEAVY_RULE);
        out.println("RESTAURANT RUSH - you are the manager");
        out.println(HEAVY_RULE);
        out.println("Turns: " + maxTurns + "   Tables: " + tableCount
                + "   Staff: " + String.join(", ", staffNames) + "   Seed: " + seed);
        out.println("Customers arrive at the start of turns " + joinTurns(arrivalTurns) + ".");
        out.println("Menu:");
        for (MenuItem item : menu.getItems()) {
            out.println("  - " + item);
        }
        out.println("Goal after turn " + maxTurns + ": " + objective + ".");
        out.println("Customers choose their own food. Type a task number or a command; 0 or 'wait' skips.");
    }

    public void showTurnHeader(int turn, int maxTurns, Ledger ledger) {
        out.println();
        out.println(HEAVY_RULE);
        out.printf("TURN %d of %d   |   Revenue %s   |   Avg satisfaction (paid) %s%n",
                turn, maxTurns, Money.format(ledger.getRevenueCents()), ledger.formatAverageSatisfaction());
        out.println(HEAVY_RULE);
    }

    /** Queue, tables (with each customer's satisfaction and order status) and staff tasks. */
    public void showState(Restaurant restaurant, Map<String, List<TaskOption>> staffTasks, String cashierStatus) {
        List<Customer> queue = restaurant.getWaitingQueue();
        out.println("Waiting queue:" + (queue.isEmpty() ? " (empty)" : ""));
        for (Customer customer : queue) {
            out.printf("  %-3s %-8s satisfaction %3d%n",
                    customer.getId(), customer.getTypeName(), customer.getSatisfaction());
        }

        out.println("Tables:");
        for (Table table : restaurant.getTables()) {
            Customer occupant = table.getOccupant();
            if (occupant == null) {
                out.printf("  %-3s free%n", table.getId());
            } else {
                out.printf("  %-3s %-3s %-8s satisfaction %3d  %-25s %s%n", table.getId(), occupant.getId(),
                        occupant.getTypeName(), occupant.getSatisfaction(),
                        occupant.getState().getDescription(), describeOrder(occupant.getOrder()));
            }
        }

        out.println("Staff tasks available:");
        for (Map.Entry<String, List<TaskOption>> entry : staffTasks.entrySet()) {
            out.printf("  %-8s %s%n", entry.getKey() + ":", joinCommands(entry.getValue()));
        }
        out.printf("  %-8s %s%n", "Cashier:", cashierStatus);
    }

    public void showHappyHourStatus(String status) {
        out.println("Happy Hour: " + status);
    }

    public void showStep(String title) {
        out.println("-- " + title + " " + "-".repeat(Math.max(0, 74 - title.length())));
    }

    public void info(String message) {
        out.println("  " + message);
    }

    public void error(String message) {
        out.println("  ! Not allowed: " + message);
    }

    public void showTurnEnd(int turn, int maxTurns, Ledger ledger) {
        out.println(LIGHT_RULE);
        out.printf("End of turn %d: served %d | unhappy departures %d | paid %d | revenue %s"
                        + " | avg satisfaction %s | turns used %d/%d%n",
                turn, ledger.getServedCount(), ledger.getUnhappyDepartures(), ledger.getPaidCount(),
                Money.format(ledger.getRevenueCents()), ledger.formatAverageSatisfaction(), turn, maxTurns);
    }

    public void showFinalResult(boolean victory, List<Objective.TargetCheck> checks, Restaurant restaurant) {
        out.println();
        out.println(HEAVY_RULE);
        out.println("GAME OVER - " + (victory ? "VICTORY! The restaurant hit every target."
                : "DEFEAT. At least one target was missed."));
        out.println(HEAVY_RULE);
        for (Objective.TargetCheck check : checks) {
            out.printf("  [%s] %-40s %8s  (target %s)%n",
                    check.isMet() ? " MET  " : "MISSED", check.getName(), check.getActual(), check.getTarget());
        }
        out.println("Customers:");
        for (Customer customer : restaurant.getCustomers()) {
            out.printf("  %-3s %-8s %-25s satisfaction %3d  %s%n", customer.getId(), customer.getTypeName(),
                    customer.getState().getDescription(), customer.getSatisfaction(),
                    describeOrder(customer.getOrder()));
        }
    }

    // ----- Input -----

    /** Lists the options and returns what the manager typed ("wait" once input runs out). */
    public String askForTask(String staffName, List<TaskOption> options) {
        out.println(staffName + " - choose a task:");
        for (int i = 0; i < options.size(); i++) {
            TaskOption option = options.get(i);
            out.printf("  %d) %-14s %s%n", i + 1, option.getCommand(), option.getDescription());
        }
        out.println("  0) wait");
        String line = readLine("Enter a number or a command > ");
        return line == null ? "wait" : line;
    }

    /** Asks until the manager answers y or n. An empty line, or no more input, means no. */
    public boolean askYesNo(String question) {
        while (true) {
            String line = readLine(question);
            String answer = line == null ? "n" : line.trim().toLowerCase(Locale.ROOT);
            if (answer.isEmpty() || answer.equals("n") || answer.equals("no")) {
                return false;
            }
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            error("Please answer y or n.");
        }
    }

    /** Returns null once input has run out. */
    private String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        if (!inputExhausted) {
            try {
                String line = in.readLine();
                if (line != null) {
                    if (echoInput) {
                        out.println(line);
                    }
                    return line;
                }
            } catch (IOException e) {
                out.println();
                out.println("  (could not read input: " + e.getMessage() + ")");
            }
            inputExhausted = true;
        }
        out.println("(no more input - waiting)");
        return null;
    }

    // ----- Formatting helpers -----

    private static String describeOrder(Order order) {
        if (order == null) {
            return "no order yet";
        }
        return order.getItem().getName() + " " + order.describeStatus() + ", bill " + Money.format(order.getBillCents());
    }

    private static String joinCommands(List<TaskOption> options) {
        if (options.isEmpty()) {
            return "none (wait)";
        }
        StringBuilder text = new StringBuilder();
        for (TaskOption option : options) {
            if (text.length() > 0) {
                text.append(" | ");
            }
            text.append(option.getCommand());
        }
        return text.toString();
    }

    private static String joinTurns(List<Integer> turns) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < turns.size(); i++) {
            if (i > 0) {
                text.append(i == turns.size() - 1 ? " and " : ", ");
            }
            text.append(turns.get(i));
        }
        return text.toString();
    }
}
