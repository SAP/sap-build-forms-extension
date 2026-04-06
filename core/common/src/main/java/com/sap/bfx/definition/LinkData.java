package com.sap.bfx.definition;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Setter
@Getter
public class LinkData {

    String text; //Anzeigetext
    String hRef; //link URL


    /**
     *
     */
    public LinkData() {

    }

    /**
     * @param text
     * @param hRef
     */
    public LinkData(final String text, final String hRef) {
        this.setText(text);
        this.setHRef(hRef);
    }
}
