package kz.qazroute.exception;

/**
 * Thrown when no route can be selected: the list is empty, or every route breaks a hard constraint
 * (unavailable, over budget, too slow, wrong cities).
 *
 * <p>It is an unchecked exception so that beginners are not forced to write try/catch everywhere,
 * but callers that care (like comparison mode) catch it explicitly.</p>
 */
public class NoSuitableRouteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoSuitableRouteException(String message) {
        super(message);
    }
}
