package xyz.deszaras.grounds.command;

/**
 * An exception thrown when a command cannot be built. Besides
 * {@link CommandFactory} throwing it, building code in commands
 * themselves should throw this.
 */
public class CommandFactoryException extends Exception {
    /**
     * Creates a new exception.
     */
    public CommandFactoryException() {}

    /**
     * Creates a new exception with the given message.
     *
     * @param msg message
     */
    public CommandFactoryException(String msg) { super(msg); }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause cause
     */
    public CommandFactoryException(Throwable cause) { super(cause); }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param msg message
     * @param cause cause
     */
    public CommandFactoryException(String msg, Throwable cause) { super(msg, cause); }
}
