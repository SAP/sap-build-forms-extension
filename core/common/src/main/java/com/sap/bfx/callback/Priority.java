package com.sap.bfx.callback;

/**
 * Priority class to define the order of execution for callbacks.
 */
public final class Priority {

    public static final Priority MIN = new Priority(0);
    public static final Priority LOW = new Priority(10);
    public static final Priority DEFAULT = new Priority(50);

    public static final Priority HIGH = new Priority(80);

    public static final Priority MAX = new Priority(100);
    private int order;

    /**
     * Constructor
     *
     * @param order the order of execution, between 0 and 100
     */
    private Priority(final int order) {
        if (order < 0 || order > 100) {
            throw new IllegalArgumentException("order out of range");
        }
        this.order = order;
    }

    /**
     * Get the order of execution
     *
     * @return the order of execution
     */
    public int getOrder() {
        return this.order;
    }
}
