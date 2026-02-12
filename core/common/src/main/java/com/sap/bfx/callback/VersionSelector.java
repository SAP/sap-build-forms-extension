package com.sap.bfx.callback;

import java.util.Arrays;

/**
 * Version selector for callback methods.
 * <p>
 * Use {@link #IGNORE} to ignore version checks.
 * </p>
 *
 */
public abstract class VersionSelector {

    public final static int IGNORE = -1;

    /**
     * Private constructor to prevent instantiation.
     */
    private VersionSelector() {
    }

    /**
     * Selects all versions.
     *
     * @return a VersionSelector that matches all versions
     */
    public static VersionSelector all() {
        return new VersionSelector() {
            @Override
            protected boolean match(int version) {
                return true;
            }
        };
    }

    /**
     * Selects all versions up to and including the specified version.
     *
     * @param untilVersion the upper version limit (inclusive)
     * @return a VersionSelector that matches versions up to and including untilVersion
     */
    public static VersionSelector until(int untilVersion) {
        return new VersionSelector() {
            @Override
            protected boolean match(int version) {
                return version == IGNORE || version <= untilVersion;
            }
        };
    }

    /**
     * Selects all versions from the specified version onwards.
     *
     * @param sinceVersion the lower version limit (inclusive)
     * @return a VersionSelector that matches versions from sinceVersion onwards
     */
    public static VersionSelector since(int sinceVersion) {
        return new VersionSelector() {
            @Override
            protected boolean match(int version) {
                return version == IGNORE || version >= sinceVersion;
            }
        };
    }

    /**
     * Selects all versions between the specified lower and upper version (inclusive).
     *
     * @param lowerVersion the lower version limit (inclusive)
     * @param upperVersion the upper version limit (inclusive)
     * @return a VersionSelector that matches versions between lowerVersion and upperVersion
     */
    public static VersionSelector between(int lowerVersion, int upperVersion) {
        return new VersionSelector() {
            @Override
            protected boolean match(int version) {
                return version == IGNORE || (version >= lowerVersion && version <= upperVersion);
            }
        };
    }

    /**
     * Selects specific versions.
     *
     * @param versions the specific versions to match
     * @return a VersionSelector that matches the specified versions
     */
    public static VersionSelector versions(int... versions) {
        return new VersionSelector() {
            @Override
            protected boolean match(int version) {
                return version == IGNORE || Arrays.stream(versions).anyMatch(it -> version == it);
            }
        };
    }

    /**
     * Checks if the given version matches the selector criteria.
     *
     * @param version the version to check
     * @return true if the version matches, false otherwise
     */
    protected abstract boolean match(int version);
}
