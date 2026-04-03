package com.sap.bfx.session;

import lombok.Data;

/**
 * Data class for the document-form element.
 */
@Data
public class DocFormData {
    private String selectedTab;
    private String docUrl;
    private String docContentType;

}
