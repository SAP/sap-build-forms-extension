package com.sap.bfx.cockpit.callback;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class FrontendSettings {
    private String language = "en";
    private List<Profile> profiles = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String id;
        private String name;
        private boolean selected;
    }
}
