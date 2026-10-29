package restaurant.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** A typed task such as "seat C1 T2": a verb followed by customer or table ids. */
public final class TaskCommand {

    private final String verb;
    private final List<String> arguments;

    private TaskCommand(String verb, List<String> arguments) {
        this.verb = verb;
        this.arguments = arguments;
    }

    public static TaskCommand parse(String text) throws InvalidActionException {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidActionException("Please enter a task.");
        }
        List<String> words = new ArrayList<>(Arrays.asList(trimmed.split("\\s+")));
        String verb = words.remove(0).toLowerCase(Locale.ROOT);
        List<String> arguments = new ArrayList<>();
        for (String word : words) {
            arguments.add(word.toUpperCase(Locale.ROOT));
        }
        return new TaskCommand(verb, arguments);
    }

    public String getVerb() {
        return verb;
    }

    public int argumentCount() {
        return arguments.size();
    }

    public String argument(int index) {
        return arguments.get(index);
    }

    /** Checks the number of ids given, e.g. 1 or 2 for "seat C1 [T1]". */
    public void requireArguments(int min, int max, String usage) throws InvalidActionException {
        if (arguments.size() < min || arguments.size() > max) {
            throw new InvalidActionException("Usage: " + usage);
        }
    }

    /** The customer named by the first id. */
    public Customer customer(Restaurant restaurant) throws InvalidActionException {
        return restaurant.findCustomer(argument(0));
    }

    /** The table named by the second id, or the first free table if none was given. */
    public Table tableOrFirstFree(Restaurant restaurant) throws InvalidActionException {
        return argumentCount() > 1 ? restaurant.findTable(argument(1)) : restaurant.firstFreeTable();
    }
}
