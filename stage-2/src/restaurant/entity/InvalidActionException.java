package restaurant.entity;

/**
 * Thrown when the manager asks for something the rules do not allow. The message
 * explains why, so the manager can choose again without losing the staff task.
 */
public class InvalidActionException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidActionException(String reason) {
        super(reason);
    }
}
