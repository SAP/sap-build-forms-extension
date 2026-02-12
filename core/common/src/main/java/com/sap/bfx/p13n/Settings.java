package com.sap.bfx.p13n;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Settings class for personalization settings.
 * <p>
 * This class must match the proto definition in p13n.proto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {
    private String id = "";
    private String user = "";
    private String key = "";
    private String app = "";
    private String encoding = "";
    private String value = "";
}
