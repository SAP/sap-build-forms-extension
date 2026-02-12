package com.sap.bfx.definition;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Dimension {
    String height;
    String width;

    /**
     *
     */
    public Dimension() {

    }

    /**
     * @param height
     * @param width
     */
    public Dimension(final String height, final String width) {
        this.setHeight(height);
        this.setWidth(width);
    }
}
