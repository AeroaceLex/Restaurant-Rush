package restaurant.entity;

/** A task the manager could choose: the command to type and what it would do. */
public final class TaskOption {

    private final String command;
    private final String description;

    public TaskOption(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }
}
