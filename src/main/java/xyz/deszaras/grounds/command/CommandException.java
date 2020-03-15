package xyz.deszaras.grounds.command;

public class CommandException extends Exception {
    /**
     * Creates a new exception.
     */
    public CommandException() {}

    /**
     * Creates a new exception with the given message.
     *
     * @param msg message
     */
    public CommandException(String msg) { super(msg); }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause cause
     */
    public CommandException(Throwable cause) { super(cause); }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param msg message
     * @param cause cause
     */
    public CommandException(String msg, Throwable cause) { super(msg, cause); }
}
