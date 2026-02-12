package com.sap.bfx.callback;

import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.session.Form;

/**
 * Factory interface for creating AccessClass and DataApi instances based on the provided
 * ScenarioDefinition and Form.
 */
public interface AccessClassFactory {

    /**
     * Creates an instance of AccessClass based on the given ScenarioDefinition and Form.
     *
     * @param sd   the scenario definition
     * @param form the form instance
     * @param <AC> the type of AccessClass to be created
     * @return an instance of the specified AccessClass
     */
    <AC extends AccessClass> AC createAccessClass(final ScenarioDefinition sd, final Form form);

    /**
     * Creates an instance of DataApi based on the given ScenarioDefinition and Form.
     *
     * @param sd   the scenario definition
     * @param form the form instance
     * @return an instance of DataApi
     */
    DataApi createDataApi(final ScenarioDefinition sd, final Form form);
}
