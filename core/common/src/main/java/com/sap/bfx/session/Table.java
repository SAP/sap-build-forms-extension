package com.sap.bfx.session;

import com.sap.bfx.definition.TableElementDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class Table extends Element {
    private String sortField;
    private SortOrder sortOrder = SortOrder.ASCENDING;
    private List<String> rows = new ArrayList<>();
    private Map<String, ElementRow> data = new HashMap<>();
    private int pos = 0;
    private int pageSize;

    /**
     * Default constructor for a table.
     */
    public Table(final TableElementDefinition ed) {
        this.pageSize = ed.getPageSize();
    }

    /**
     * @param c
     * @return
     */
    public static SortOrder fromCode(String c) {
        if (StringUtils.equalsIgnoreCase(c, "d")) {
            return SortOrder.DESCENDING;
        }
        return SortOrder.ASCENDING;
    }

    /**
     * @param sd
     * @return
     */
    public static String toCode(SortOrder sd) {
        if (sd == SortOrder.DESCENDING) {
            return "d";
        }
        return "a";
    }

    /**
     * @param row
     */
    public void addRow(final ElementRow row) {
        rows.add(row.getRowId());
        data.put(row.getRowId(), row);
    }

    /**
     * @param rowId
     * @return
     */
    public boolean deleteRow(final String rowId) {
        data.remove(rowId);
        return rows.removeIf(it -> StringUtils.equals(it, rowId));
    }

    /**
     *
     */
    public void clear() {
        data.clear();
        rows.clear();
    }

    /**
     * @return
     */
    public List<String> getCurrentRows() {
        final var result = new ArrayList<String>();
        for (var i = pos; i < pos + pageSize; i++) {
            if (i < rows.size()) {
                result.add(rows.get(i));
            }
        }
        return result;
    }
}
