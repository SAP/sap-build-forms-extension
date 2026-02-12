package com.sap.bfx.definition;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class LinkData {

    String text;
    String hRef;
    String target;

    /**
     *
     */
    public LinkData() {

    }

    /**
     * @param text
     * @param hRef
     * @param target
     */
    public LinkData(final String text, final String hRef, final String target) {
        this.setText(text);
        this.setHRef(hRef);
        this.setTarget(target);
    }
}