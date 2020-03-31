package xyz.deszaras.grounds.util;

/**
 * An exception thrown by {@link ArgumentResolver} when it cannot
 * resolve an argument.
 */
public class ArgumentResolverException extends Exception {
    /**
     * Creates a new exception.
     */
    public ArgumentResolverException() {}

    /**
     * Creates a new exception with the given message.
     *
     * @param msg message
     */
    public ArgumentResolverException(String msg) { super(msg); }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause cause
     */
    public ArgumentResolverException(Throwable cause) { super(cause); }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param msg message
     * @param cause cause
     */
    public ArgumentResolverException(String msg, Throwable cause) { super(msg, cause); }
}
