package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PdfViewerElementDefinition extends ElementDefinition {

    /** When true, the element renders as a floating, draggable, resizable panel instead of inline. */
    private boolean floating = false;

    /** Optional explicit size for the viewer (width/height CSS values, e.g. "600px", "80vh"). */
    private Dimension size;

    public PdfViewerElementDefinition() {
        super(UIElementType.PdfViewer);
    }
}
