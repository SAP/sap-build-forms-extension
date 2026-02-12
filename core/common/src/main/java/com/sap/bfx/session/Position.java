package com.sap.bfx.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    @Getter
    private String rowId;
    @Getter
    private String key;
}
