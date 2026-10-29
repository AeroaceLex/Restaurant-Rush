package restaurant.app;

import restaurant.boundary.ConsoleUI;
import restaurant.control.GameConfig;
import restaurant.control.GameController;

/**
 * Starts Restaurant Rush with the base setting.
 *
 * <pre>
 * java -cp out restaurant.app.Main [--seed N] [--echo]
 *   --seed N   seed for customers' food choices (default 2002)
 *   --echo     print each input line, for transcripts of scripted runs
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        long seed = GameConfig.DEFAULT_SEED;
        boolean echo = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--echo")) {
                echo = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                try {
                    seed = Long.parseLong(args[++i]);
                } catch (NumberFormatException e) {
                    System.out.println("Ignoring invalid seed '" + args[i] + "'; using " + seed + ".");
                }
            } else {
                System.out.println("Ignoring unknown argument '" + args[i] + "'.");
            }
        }
        ConsoleUI ui = new ConsoleUI(System.in, System.out, echo);
        new GameController(GameConfig.baseSetting(seed), ui).play();
    }
}
