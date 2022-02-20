package xyz.deszaras.grounds.api;

/**
 * An exception thrown by {@link PluginCallFactory} when building a plugin call
 * fails.
 */
public class PluginCallFactoryException extends Exception {
    /**
     * Creates a new exception.
     */
    public PluginCallFactoryException() {}

    /**
     * Creates a new exception with the given message.
     *
     * @param msg message
     */
    public PluginCallFactoryException(String msg) { super(msg); }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause cause
     */
    public PluginCallFactoryException(Throwable cause) { super(cause); }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param msg message
     * @param cause cause
     */
    public PluginCallFactoryException(String msg, Throwable cause) { super(msg, cause); }
}
