package com.sap.bfx.session;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sap.bfx.callback.AbstractAdapterHandlingService;
import com.sap.bfx.callback.PersistenceAdapter;
import com.sap.bfx.definition.DefinitionService;
import com.sap.bfx.definition.ProcessState;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.utils.EnumUtils;
import com.sap.bfx.utils.SerializationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

/**
 *
 */
@Service
@Slf4j
public class FormsService extends AbstractAdapterHandlingService<PersistenceAdapter> {

    private static final String DATABASE_FORMS = "database-forms";
    private final DefinitionService defService;
    private final ObjectMapper om;

    /**
     * @param defService
     */
    @Autowired
    public FormsService(final DefinitionService defService, final ApplicationContext applicationContext) {
        super(applicationContext, PersistenceAdapter.class);

        this.defService = defService;

        om = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);
        final var module = new SimpleModule();
        module.addSerializer(Form.class, new FormSerializer());
        om.registerModule(module);
    }

    /**
     * Create a new element based on the element definition provided.
     * All child elements are created as well, but no default values
     * are set.
     *
     * @param node JSON node to be read
     * @return Form instance
     */
    public Form readForm(final JsonNode node) {
        // read scenario definition properties
        final var defVersion = SerializationUtils.getPropInt(node, FormUtils.NM_DEF_VERSION);

        // read scenario definition by it's version
        final var stOpt = defService.findDefinitionByVersion(defVersion);
        final var form = new Form(stOpt.get(), new BackendJournal(stOpt.get()));

        // read root element row and copy the values to form
        final var rootForm = FormUtils.readElementRow(stOpt.get(), node);
        form.setRowId(rootForm.getRowId());
        form.setSelected(rootForm.isSelected());
        form.setElements(rootForm.getElements());

        // set additional properties of the form
        form.setId(SerializationUtils.getPropText(node, FormUtils.NM_ID));
        form.setVersion(SerializationUtils.getPropInt(node, FormUtils.NM_FORM_VERSION));
        form.setScenarioName(SerializationUtils.getPropText(node, FormUtils.NM_DEF_NAME));
        form.setScenarioVersion(defVersion);
        form.setChangedBy(SerializationUtils.getPropText(node, FormUtils.NM_CHANGED_BY));
        form.setChangedAt(SerializationUtils.getPropInstant(node, FormUtils.NM_CHANGED_AT));
        form.setRefId(SerializationUtils.getPropText(node, FormUtils.NM_REF));
        form.setTemplateName(SerializationUtils.getPropText(node, FormUtils.NM_TEMPLATE));
        form.setWorkflowAdapter(SerializationUtils.getPropText(node, FormUtils.NM_WORKFLOW_ADAPTER));
        form.setDescription(SerializationUtils.getPropText(node, FormUtils.NM_DESCRIPTION));
        form.setFinishedAt(SerializationUtils.getPropInstant(node, FormUtils.NM_FINISHED_AT));
        form.setFunctionalId(SerializationUtils.getPropText(node, FormUtils.NM_FUNCTIONAL_ID));
        form.setStartedBy(SerializationUtils.getPropText(node, FormUtils.NM_CREATED_BY));
        form.setStartedAt(SerializationUtils.getPropInstant(node, FormUtils.NM_CREATED_AT));
        final var ps = EnumUtils.valueById(ProcessState.class, SerializationUtils.getPropText(node, FormUtils.NM_STATE));
        form.setState(ps.isPresent() ? ps.get() : ProcessState.Draft);
        form.setDetailState(SerializationUtils.getPropText(node, FormUtils.NM_DETAIL_STATE));

        return form;
    }

    /**
     * Load form by id
     *
     * @param id
     * @return
     */
    public Form loadById(String id) {
        return this.loadById(null, id);
    }

    /**
     * @param adapterName
     * @param id
     * @return
     */
    public Form loadById(final String adapterName, final String id) {
        final var adapter = this.getAdapter(adapterName);
        final var formInputStreamPair = adapter.loadById(id);
        final var form = (Form) formInputStreamPair.getLeft();
        final var is = formInputStreamPair.getRight();
        // is == null indicates that the form wasn't found. Let's throw a dedicated exception for this
        if (is == null) {
            throw new BadRequestException("Form with id " + id + " not found");
        }
        try {
            deserializeElements(form, is);
        } catch (IOException e) {
            throw ExceptionUtils.from(e);
        }
        return form;
    }

    /**
     * @param scenarioName
     * @param refId
     * @return
     */
    public Form loadByRefId(final String scenarioName, final String refId) {
        return this.loadByRefId(null, scenarioName, refId);
    }

    /**
     * @param scenarioName
     * @param refId
     * @return
     */
    public Form loadByRefId(final String adapterName, final String scenarioName,
                            final String refId) {
        final var adapter = this.getAdapter(adapterName);
        final var formInputStreamPair = adapter.loadByRefId(scenarioName, refId);
        final var form = (Form) formInputStreamPair.getLeft();
        InputStream is = formInputStreamPair.getRight();
        try {
            deserializeElements(form, is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return form;
    }

    /**
     * @param form
     */
    public void save(final Form form) {
        var isNew = false;
        // increase version in order to detect concurrency issues
        form.setVersion(form.getVersion() + 1);
        // update user and timestamp, e.g. get user from spring security context
        form.setChangedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        form.setChangedAt(Instant.now());
        // update scenario name and version (especially important for new form)
        form.setScenarioName(form.getSd().getName());
        form.setScenarioVersion(form.getSd().getVersion());
        // check if id is blank -> determine if insert or update and set id if new
        if (StringUtils.isBlank(form.getId())) {
            form.setId(java.util.UUID.randomUUID().toString());
            isNew = true;
        }
        // last step is to serialize the form and then call the adapter to save it
        try (var os = new ByteArrayOutputStream()) {
            om.writeValue(os, form);
            var is = new ByteArrayInputStream(os.toByteArray());
            final var adapter = this.getAdapter(DATABASE_FORMS);
            adapter.save(form, is, isNew);
        } catch (IOException e) {
            form.setVersion(form.getVersion() - 1);
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * @param id
     */
    public void delete(String id) {
        final var adapter = this.getAdapter(DATABASE_FORMS);
        adapter.delete(id);
    }

    /**
     * @param form
     * @param is
     * @throws IOException
     */
    private void deserializeElements(Form form, InputStream is) throws IOException {
        final var jp = om.createParser(is);
        final var node = jp.getCodec().readTree(jp);
        final var sd = defService.findDefinitionByVersion(form.getScenarioVersion()).get();
        final var d = FormUtils.readElementRow(sd, (JsonNode) node);
        form.setSd(sd);
        form.setRowId(d.getRowId());
        form.setElements(d.getElements());
    }
}