package com.sap.bfx.security;

/**
 * Factory interface for creating SecuritySession instances. Implementations of this interface should provide the
 * logic to create a new SecuritySession based on the provided input parameters, including the user and any
 * authority/permission assigned to him. This allows for flexibility in how SecuritySession objects are created.
 */
public interface SecuritySessionFactory {

    /**
     * Creates a new SecuritySession instance with the provided parameters.
     *
     * @param input the input object containing the necessary parameters to create a SecuritySession
     * @return a new SecuritySession instance
     */
    SecuritySession create(Object input);
}
