<#macro visualAttributes item>
    public boolean is${item.definition.name}Visible() {
        var opt = form.getVisible(this.rowId, "${item.definition.key}");
        return opt != null && opt.isPresent() && opt.get();
    }

    public void set${item.definition.name}Visible(final boolean v) {
        form.setVisible(this.rowId, "${item.definition.key}", v);
    }

    <#if item.canBeEditable>
    public boolean is${item.definition.name}Editable() {
        var opt = form.getEditable(this.rowId, "${item.definition.key}");
        return opt != null && opt.isPresent() && opt.get();
    }

    public void set${item.definition.name}Editable(final boolean v) {
        form.setEditable(this.rowId, "${item.definition.key}", v);
    }
    </#if>

    <#if item.canBeRequired>
    public boolean is${item.definition.name}Required() {
        var opt = form.getRequired(this.rowId, "${item.definition.key}");
        return opt != null && opt.isPresent() && opt.get();
    }

    public void set${item.definition.name}Required(final boolean v) {
        form.setRequired(this.rowId, "${item.definition.key}", v);
    }
    </#if>

    <#if item.canHaveMessage>
    public Optional<Message> get${item.definition.name}Message() {
        return form.getMessage(this.rowId, "${item.definition.key}");
    }

    public void set${item.definition.name}Message(final Message v) {
        form.setMessage(this.rowId, "${item.definition.key}", v);
    }
    </#if>
</#macro>

package ${basePackage}.metadata;

import com.sap.consulting.forms.definition.*;
import com.sap.consulting.forms.callback.*;
import com.sap.consulting.forms.session.*;
import jakarta.annotation.Generated;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.function.Predicate;

@Generated("com.sap.consulting.forms.maven.TemplateService")
@SuppressWarnings("unused")
public class ${className} implements AccessClass {
<#--
<#list types as item>
    private final static TypeLiteral ${item.getRight()} = new TypeLiteral<List<${item.getLeft()}>>() {};
</#list>
-->
    private final Form form;
    private final String rowId;

    public ${className}(final Form form, final String rowId) {
        this.form = form;
        this.rowId = rowId;
    }

    public void setSelected(final boolean value) {
        FormUtils.setSelected(form, rowId, value);
    }

    public boolean isSelected() {
        return FormUtils.isSelected(form, rowId);
    }

<#if isRoot>
    public String getRootValue() {
        return form.getValue(this.rowId, "${rootKey}", String.class).orElse(null);
    }

    public void setRootValue(final String value) {
        form.setValue(this.rowId, "${rootKey}", value);
    }
</#if>

<#list elements as item>
<#if item.collection>

    public ${item.returnDataType} add${item.definition.name}(final Context<? extends AccessClass> ctx) {
        final var row = FormUtils.addRow(form, this.rowId, "${item.definition.key}", ctx);
        if (row == null) {
            throw new RuntimeException("failed to add row to ${item.definition.name}");
        }
        return new ${item.returnDataType}(form, row.getRowId());
    }

    public Iterable<${item.returnDataType}> get${item.definition.name}Value() {
        final var rowId = this.rowId;
        final var form = this.form;

        return () -> {
            var vOpt = form.getValue(rowId, "${item.definition.key}", Object.class);
            return vOpt.map(o -> ((Table) o).getRows().stream()
                    .map(it -> new ${item.returnDataType}(form, it))
                    .toList().iterator()).orElse(Collections.emptyIterator());
        };
    }

    public List<String> get${item.definition.name}Rows() {
        return FormUtils.getRows(form, rowId, "${item.definition.key}");
    }

    public ${item.returnDataType} get${item.definition.name}Row(final String rowId) {
        var vOpt = form.getValue(this.rowId, "${item.definition.key}", Object.class);
        if (!vOpt.isPresent()) {
            return null;
        }
        for (var id : ((Table) vOpt.get()).getRows()) {
            if (StringUtils.equals(id, rowId)) {
                return new ${item.returnDataType}(form, id);
            }
        }
        return null;
    }

    public void deleteAll${item.definition.name}() {
        FormUtils.deleteAll(form, this.rowId, "${item.definition.key}");
    }

    public void filter${item.definition.name}(final Predicate<${item.returnDataType}> predicate) {
        final var element = FormUtils.findElementByRowAndKey(form, rowId, "${item.definition.key}");
        if (element == null) {
            return;
        }

        if (element.getValue() instanceof Table table) {
            final var h = new ArrayList<String>();
            table.getRows().forEach(it -> {
                if (!predicate.test(new ${item.returnDataType}(form, it))) {
                    table.getData().remove(it);
                    form.getJournal().deleteRow(element, it);
                } else {
                    h.add(it);
                }
            });
            table.setRows(h);
        }
    }

    public List<${item.returnDataType}> get${item.definition.name}Selected() {
        final var result = new ArrayList<${item.returnDataType}>();
        var vOpt = form.getValue(rowId, "${item.definition.key}", Object.class);
        vOpt.ifPresent(o -> ((Table) o).getRows()
                .stream()
                .filter(r -> ((Table) o).getData().get(r).isSelected())
                .forEach(r -> result.add(new ${item.returnDataType}(form, r))));
        return result;
    }

    public void forEach${item.definition.name}(RowVisitor visitor, Context<? extends AccessClass> ctx) {
        FormUtils.forEach(form, rowId, "${item.definition.key}", visitor, ctx);
    }

    <@visualAttributes item=item/>

<#else>
    public ${item.returnDataType} get${item.definition.name}Value() {
        return (${item.returnDataType}) form.getValue(this.rowId, "${item.definition.key}", Object.class).orElse(null);
    }

    public Optional<${item.returnDataType}> get${item.definition.name}OptVal() {
        return form.getValue(this.rowId, "${item.definition.key}", Object.class).map(o -> (${item.returnDataType}) o);
    }
    <#if item.dialog>

    public ${item.returnDialogType} get${item.definition.name}Dialog() {
        return new ${item.returnDialogType}(form, this.rowId);
    }
    </#if>

    public void set${item.definition.name}Value(${item.returnDataType} v) {
        form.setValue(this.rowId, "${item.definition.key}", v);
    }

    <@visualAttributes item=item/>

</#if>
</#list>
<#if collection>
    public void delete() {
        FormUtils.deleteRow(this.form, this.rowId);
    }
</#if>
}