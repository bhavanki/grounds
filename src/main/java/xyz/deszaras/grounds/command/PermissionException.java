package xyz.deszaras.grounds.command;

/**
 * An exception during command execution when the caller lacks permission for
 * the command.
 */
public class PermissionException extends CommandException {
    /**
     * Creates a new exception.
     */
    public PermissionException() { super(); }

    /**
     * Creates a new exception with the given message.
     *
     * @param msg message
     */
    public PermissionException(String msg) { super(msg); }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause cause
     */
    public PermissionException(Throwable cause) { super(cause); }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param msg message
     * @param cause cause
     */
    public PermissionException(String msg, Throwable cause) { super(msg, cause); }
}
