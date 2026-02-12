package com.sap.bfx.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.exception.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class FrontendJournal {
    @Getter
    private Collection<UpdatedInfo> updated = new ArrayList<>();
    @Getter
    private Collection<DeletedInfo> deleted = new ArrayList<>();

    /**
     *
     */
    private FrontendJournal() {
    }

    /**
     * @param sd
     * @param node
     */
    public static FrontendJournal initJson(final ScenarioDefinition sd, final JsonNode node) {
        final var j = new FrontendJournal();

        // read updated nodes
        node.get("updated").iterator().forEachRemaining(itUpdated -> {
            final var updatedInfo = new UpdatedInfo(itUpdated.get("rowId").asText(), itUpdated.get("key").asText(),
                    new ArrayList<>());
            j.updated.add(updatedInfo);

            var ed = sd.findElementByKey(updatedInfo.getKey());

            itUpdated.get("changes").iterator().forEachRemaining(itChange -> {
                final var changeInfo = new ChangeInfo();
                changeInfo.setProp(ChangePropertyType.valueByKey(itChange.get("p").asText()).get());
                switch (changeInfo.getProp()) {
                    case Value:
                        changeInfo.setValue(FormUtils.readElementData(sd, ed, itChange));
                        break;
                    case Visible:
                    case Selected:
                        changeInfo.setValue(FormUtils.readElementData(itChange, Boolean.class));
                        break;
                    case Position:
                    case PageSize:
                        changeInfo.setValue(FormUtils.readElementData(itChange, Integer.class));
                        break;
                    case SortField:
                    case SortOrder:
                        changeInfo.setValue(FormUtils.readElementData(itChange, String.class));
                        break;
                    default:
                        throw new BadRequestException("Unsupported property '" + changeInfo.getProp()
                                + "' in Journal.readFromJson");
                }

                updatedInfo.getChanges().add(changeInfo);
            });
        });

        // read deleted nodes
        node.get("deleted").iterator().forEachRemaining(row -> {
            final var deletedInfo = new DeletedInfo(row.get("rowId").asText(), row.get("key").asText(),
                    new ArrayList<>());
            j.deleted.add(deletedInfo);

            row.get("ids").iterator().forEachRemaining(id -> {
                deletedInfo.getIds().add(id.asText());
            });
        });

        return j;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChangeInfo {
        private ChangePropertyType prop;
        private Object value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatedInfo {
        private String rowId;
        private String key;
        private Collection<ChangeInfo> changes = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeletedInfo {
        private String rowId;
        private String key;
        private Collection<String> ids = new ArrayList<>();
    }
}
