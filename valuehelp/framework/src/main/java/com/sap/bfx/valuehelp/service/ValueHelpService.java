package com.sap.bfx.valuehelp.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.bfx.callback.AdapterDescriptor;
import com.sap.bfx.utils.EnumUtils;
import com.sap.bfx.valuehelp.adapter.ValueHelpAdapter;
import com.sap.bfx.valuehelp.config.ApplicationConfig;
import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import com.sap.bfx.valuehelp.model.ValueHelpType;
import com.sap.bfx.valuehelp.model.xml.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for ValueHelp operations.
 */
@Service
@Slf4j
public class ValueHelpService {

    private final static java.sql.Timestamp MAX_VALID_UNTIL = new java.sql.Timestamp(
            LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 59).toEpochSecond(ZoneOffset.UTC) * 1000);

    private final ApplicationConfig appCfg;
    private final Map<String, ValueHelpAdapter> adapterMap = new HashMap<>();
    private final ValueHelpDao dao;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * @param dao
     * @param appCfg
     */
    @Autowired
    public ValueHelpService(final ValueHelpDao dao, final ApplicationConfig appCfg) {
        this.dao = dao;
        this.appCfg = appCfg;
    }

    /**
     * Inits the given ValueHelpAdapters and stores them in a map for later use. The key of the map is defined in the
     * AdapterDescriptor annotation of the ValueHelpAdapter.
     *
     * @param ctx Spring ApplicationContext to be used
     */
    public void initValueHelpAdapters(final ApplicationContext ctx) {
        adapterMap.clear();

        final var adapter = ctx.getBeansOfType(ValueHelpAdapter.class);
        adapter.values().forEach(it -> {
            var descriptor = it.getClass().getAnnotation(AdapterDescriptor.class);
            if (descriptor == null) {
                log.error("ValueHelpAdapter '" + it.getClass().getName() +
                        "' has not annotation of type AdapterDescriptor");
            } else {
                adapterMap.put(descriptor.value(), it);
                log.info("ValueHelpAdapter '" + it.getClass().getName() + "' added with name '" + descriptor.value() +
                        "'.");
            }
        });
    }

    /**
     *
     * @param searchString
     * @param adapter
     * @return
     */
    public Collection<ValueHelpDef> findAllDefs(String searchString, String[] adapter) {
        if (searchString != null && !searchString.isEmpty() && adapter != null && adapter.length > 0) {
            return dao.findAllDefsBySearchIDAndAdapter(searchString, adapter);
        } else if (searchString != null && !searchString.isEmpty()) {
            return dao.findAllDefsBySearchID(searchString);
        } else if (adapter != null && adapter.length > 0) {
            return dao.findAllDefsByAdapter(adapter);
        } else {
            return dao.findAllDefs();
        }
    }

    /**
     * @param id
     * @return
     */
    public Optional<ValueHelpDef> findDefById(String id) {
        return dao.findDefById(id);
    }

    /**
     * @return
     */
    public Collection<String> findAllAdapter() {
        return dao.findAllAdapter();
    }

    /**
     * @return
     */
    public Collection<String> findAllDefinedAdapter() {
        return adapterMap.keySet();
    }

    public String[] findAllDefinedLocales() {
        return appCfg.getLocales().split(",");
    }

    /**
     * @param def
     */
    public void addDef(ValueHelpDef def) {
        if (def != null) {
            if (def.getAdapter().equalsIgnoreCase("local")) {
                def.setAdapter("local");
            } else {
                Optional<String> existingAdapter =
                        adapterMap.keySet().stream().filter(e -> e.equalsIgnoreCase(def.getAdapter())).findFirst();
                if (existingAdapter.isPresent()) {
                    def.setAdapter(existingAdapter.get());
                } else {
                    throw new RuntimeException("Adapter " + def.getAdapter() + " not defined");
                }
            }

            if (def.getLanguages() != null &&
                    def.getLanguages().stream().anyMatch(l -> !this.appCfg.getLocales().contains(l))) {
                throw new RuntimeException("Locales " + def.getLanguages() + " not valid");
            }

            dao.addDef(def);
        }
    }

    /**
     * @param def
     */
    public void updateDef(ValueHelpDef def) {
        if (def != null) {
            if (def.getAdapter().equalsIgnoreCase("local")) {
                def.setAdapter("local");
            } else {
                Optional<String> existingAdapter =
                        adapterMap.keySet().stream().filter(e -> e.equalsIgnoreCase(def.getAdapter())).findFirst();
                if (existingAdapter.isPresent()) {
                    def.setAdapter(existingAdapter.get());
                } else {
                    throw new RuntimeException("Adapter " + def.getAdapter() + " not defined");
                }
            }
            if (def.getLanguages() != null &&
                    def.getLanguages().stream().anyMatch(l -> !this.appCfg.getLocales().contains(l))) {
                throw new RuntimeException("Locales " + def.getLanguages() + " not valid");
            }
            dao.updateDef(def);

            var values = findValueById(def.getId());
            for (ValueHelp valueHelp : values) {
                if (!def.getLanguages().contains(valueHelp.getLocale().toString())) {
                    deleteValue(def.getId(), valueHelp.getLocale().toString());
                }
            }
        }
    }

    /**
     * @param id
     */
    public void deleteDef(String id) {
        dao.deleteDef(id);
        dao.deleteValue(id);
    }

    /**
     * @return
     */
    public byte[] exportDefs() {
        return this.export(dao.findAllDefs());
    }

    /**
     * @param search
     * @return
     */
    public byte[] exportDefs(String search) {
        return this.export(dao.findAllDefsBySearchID(search));
    }

    /**
     * @param adapter
     * @return
     */
    public byte[] exportDefs(String[] adapter) {
        return this.export(dao.findAllDefsByAdapter(adapter));
    }

    /**
     * @param search
     * @param adapter
     * @return
     */
    public byte[] exportDefs(String search, String[] adapter) {
        return this.export(dao.findAllDefsBySearchIDAndAdapter(search, adapter));
    }

    /**
     * @param ids
     * @return
     */
    public byte[] exportDefsByIds(String[] ids) {
        Collection<ValueHelpDef> defs = new ArrayList<>();
        for (String id : ids) {
            Optional<ValueHelpDef> def = dao.findDefById(id);
            if (def.isPresent()) {
                defs.add(def.get());
            } else {
                log.warn("ValueHelp definition " + id + " not found.");
            }
        }
        return this.export(defs);
    }

    /**
     * @param defs
     * @return
     */
    public byte[] export(Collection<ValueHelpDef> defs) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Marshaller mar = JAXBContext.newInstance(XmlValueHelps.class).createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            ArrayList<XmlValueHelpDef> xmlValueHelpDefs = new ArrayList<>();

            for (ValueHelpDef d : defs) {
                Collection<ValueHelp> values = dao.findAllValuesByDefId(d.getId());
                ArrayList<XmlValueHelpValue> xmlValueHelpValues = new ArrayList<>();

                for (ValueHelp v : values) {
                    xmlValueHelpValues.add(
                            new XmlValueHelpValue(v.getId(), v.getVersion(), v.getLocale(), v.getValidUntil(),
                                    v.getValues()));
                }

                xmlValueHelpDefs.add(
                        new XmlValueHelpDef(d.getId(), d.getTtl(), d.getAdapter(), d.getConfig(), d.getDescription(),
                                d.getLanguages(), d.getKeyKey(), d.getValueKeys(), d.getFormatTemplate(),
                                d.getValueHelpType().getIdentifier(), xmlValueHelpValues));
            }

            mar.marshal(new XmlValueHelps(xmlValueHelpDefs), outputStream);
            return outputStream.toByteArray();

        } catch (JAXBException e) {
            log.error("Exception during creation of xml file.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Exception during creation of xml file.");
        }
    }

    /**
     * @param file
     * @param override
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    public String importXmlFile(MultipartFile file, boolean override) throws JAXBException, IOException {
        List<String> notImportedValues = new ArrayList<>();
        List<String> notImportedDefs = new ArrayList<>();
        XmlValueHelps xmlValueHelps = (XmlValueHelps) JAXBContext.newInstance(XmlValueHelps.class).createUnmarshaller()
                                                                 .unmarshal(new ByteArrayInputStream(file.getBytes()));
        validateXml(xmlValueHelps);

        if (xmlValueHelps.getValueHelpDefs() != null) {
            for (XmlValueHelpDef def : xmlValueHelps.getValueHelpDefs()) {
                String adapter;
                if (def.getAdapter().equalsIgnoreCase("local")) {
                    adapter = "local";
                } else {
                    Optional<String> existingAdapter =
                            adapterMap.keySet().stream().filter(e -> e.equalsIgnoreCase(def.getAdapter())).findFirst();
                    if (existingAdapter.isPresent()) {
                        adapter = existingAdapter.get();
                    } else {
                        log.error("ValueHelpDefinition " + def.getId() + " could not be inserted because adapter" +
                                def.getAdapter() + " is not defined");
                        notImportedDefs.add(def.getId());
                        continue;
                    }
                }

                if (def.getLanguages() != null &&
                        def.getLanguages().stream().anyMatch(l -> !this.appCfg.getLocales().contains(l))) {
                    log.error("ValueHelpDefinition " + def.getId() + " could not be inserted because languages" +
                            def.getLanguages() + " are not valid");
                    notImportedDefs.add(def.getId());
                    continue;
                }

                // Check type, if not defined or wrong then set freestyle
                if (def.getValueHelpType() != null) {
                    def.setValueHelpType(
                            EnumUtils.valueById(ValueHelpType.class, def.getValueHelpType(), ValueHelpType.FREESTYLE)
                                     .getIdentifier());
                } else {
                    def.setValueHelpType(ValueHelpType.FREESTYLE.getIdentifier());
                }

                if (override) {
                    dao.deleteDef(def.getId());
                    dao.deleteValue(def.getId());
                }

                if (dao.findDefById(def.getId()).isEmpty()) {
                    List<String> languages = def.getLanguages() == null ? new ArrayList<>() : def.getLanguages();

                    if (def.getValueHelpValues() != null) {
                        for (XmlValueHelpValue value : def.getValueHelpValues()) {
                            if (dao.findValueByIdLocaleVersion(value.getId(), value.getLocale().toString(),
                                    value.getVersion()).isEmpty()) {
                                String newValue;
                                try {
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    newValue = objectMapper.writeValueAsString(value.getValues());
                                } catch (Exception e) {
                                    log.error("ValueHelpValue with language " + value.getLocale() + " of definition " +
                                            def.getId() +
                                            " could not be inserted because no valid values json could be created.");
                                    notImportedValues.add(
                                            "language: " + value.getLocale() + " of definition " + def.getId());
                                    continue;
                                }

                                if (!isValidJson(newValue)) {
                                    log.error("ValueHelpValue with language " + value.getLocale() + " of definition " +
                                            def.getId() +
                                            " could not be inserted because no valid values json could be created.");
                                    notImportedValues.add(
                                            "language: " + value.getLocale() + " of definition " + def.getId());
                                    continue;
                                }

                                if (!value.getLocale().toString().equals("_")) {
                                    if (!this.appCfg.getLocales().contains(value.getLocale().toString())) {
                                        log.error("ValueHelpValue with language " + value.getLocale() +
                                                " of definition " + def.getId() +
                                                " could not be inserted because locale is not valid.");
                                        notImportedValues.add(
                                                "language: " + value.getLocale() + " of definition " + def.getId());
                                        continue;
                                    }
                                    if (!languages.contains(value.getLocale().toString())) {
                                        languages.add(value.getLocale().toString());
                                    }
                                }

                                dao.addValue(value.getId(), value.getVersion(), value.getLocale().toString(),
                                        new Timestamp(value.getValidUntil().getTime()), newValue);
                            }
                        }
                    }
                    dao.addDef(
                            new ValueHelpDef(def.getId(), def.getTtl(), adapter, def.getConfig(), def.getDescription(),
                                    languages, def.getKeyKey(), def.getValueKeys(), def.getFormatTemplate(),
                                    EnumUtils.valueById(ValueHelpType.class, def.getValueHelpType(),
                                            ValueHelpType.FREESTYLE)));
                }
            }
        }
        if (notImportedDefs.size() > 0) {
            if (notImportedValues.size() > 0) {
                return "The following value help definitions could not be imported: \n" +
                        notImportedDefs.stream().map(Object::toString).collect(Collectors.joining(", \n")) +
                        ". The following value help values could not be imported: \n" +
                        notImportedValues.stream().map(Object::toString).collect(Collectors.joining(", \n"));
            } else {
                return "The following value help definitions could not be imported: \n" +
                        notImportedDefs.stream().map(Object::toString).collect(Collectors.joining(", \n"));
            }
        } else if (notImportedValues.size() > 0) {
            return "The following value help values could not be imported: \n" +
                    notImportedValues.stream().map(Object::toString).collect(Collectors.joining(", \n"));
        } else {
            return null;
        }
    }

    /**
     * @param file
     * @param override
     * @param useTechnicalName
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    public String importAbpmXmlFile(MultipartFile file, boolean override, boolean useTechnicalName)
            throws JAXBException, IOException {
        List<String> notImportedValues = new ArrayList<>();
        List<String> notImportedDefs = new ArrayList<>();
        XmlAbpmValueHelps xmlValueHelps =
                (XmlAbpmValueHelps) JAXBContext.newInstance(XmlAbpmValueHelps.class).createUnmarshaller()
                                               .unmarshal(new ByteArrayInputStream(file.getBytes()));
        validateAbpmXml(xmlValueHelps);

        IterableUtils.forEach(xmlValueHelps.getValueHelpDefs(), def -> {
            if (useTechnicalName && (def.getDescription() == null || def.getDescription().isEmpty())) {
                log.error("Warning: ValueHelpDef with id " + def.getId() +
                        "could not be imported because it does not contain any technical name.");
                notImportedDefs.add("id: " + def.getId());
                return;
            }

            String adapter;
            if (def.getAdapter().equalsIgnoreCase("local")) {
                adapter = "local";
            } else {
                Optional<String> existingAdapter =
                        adapterMap.keySet().stream().filter(e -> e.equalsIgnoreCase(def.getAdapter())).findFirst();
                if (existingAdapter.isPresent()) {
                    adapter = existingAdapter.get();
                } else {
                    log.error("ValueHelpDefinition " + def.getId() + " could not be inserted because adapter" +
                            def.getAdapter() + " is not defined");
                    return;
                }
            }

            if (override) {
                if (useTechnicalName) {
                    dao.deleteDef(def.getDescription());
                    dao.deleteValue(def.getDescription());
                } else {
                    dao.deleteDef(def.getId());
                    dao.deleteValue(def.getId());
                }
            }

            if ((useTechnicalName && dao.findDefById(def.getDescription()).isEmpty()) ||
                    (!useTechnicalName && dao.findDefById(def.getId()).isEmpty())) {

                // Initialize the new valuehelp def
                ValueHelpDef newValueHelpDef;
                final var valueKeys = new ArrayList<String>();
                valueKeys.add(def.getValueKey());

                if (useTechnicalName) {
                    newValueHelpDef =
                            new ValueHelpDef(def.getDescription(), def.getTtl(), adapter, "", "", new ArrayList<>(),
                                    def.getKeyKey(), valueKeys, "", ValueHelpType.FREESTYLE);
                } else {
                    newValueHelpDef = new ValueHelpDef(def.getId(), def.getTtl(), adapter, "", def.getDescription(),
                            new ArrayList<>(), def.getKeyKey(), valueKeys, "", ValueHelpType.FREESTYLE);
                }

                //TODO: Create config if adapter is not local

                // Import values
                IterableUtils.forEach(def.getValueHelpValues(), value -> {
                    if (!value.getLocale().toString().equals("_")) {
                        if (!this.appCfg.getLocales().contains(value.getLocale().toString())) {
                            log.error("Warning: Locale " + value.getLocale() + " in value with id " + value.getId() +
                                    " not valid. Value could not be created");
                            notImportedValues.add("language: " + value.getLocale() + " of definition " + def.getId());
                            return; // acts as continue in tranditional loop
                        } else if (newValueHelpDef.getLanguages().stream()
                                                  .noneMatch(l -> l.equalsIgnoreCase(value.getLocale().toString()))) {
                            newValueHelpDef.getLanguages().add(value.getLocale().toString());
                        }
                    }

                    if (value.getSelection() != 0) {
                        log.error("Warning: Selection is not zero in value with id: " + value.getId() +
                                ". Value is ignored");
                        notImportedValues.add("language: " + value.getLocale() + " of definition " + def.getId());
                        return; // acts as continue in tranditional loop
                    }

                    Optional<ValueHelp> existingValues =
                            dao.findValueByIdLocaleVersion(newValueHelpDef.getId(), value.getLocale().toString(), 0);

                    List<Map<String, String>> newValues;
                    boolean existing = false;
                    if (existingValues.isPresent()) {
                        newValues = existingValues.get().getValues();
                        existing = true;
                    } else {
                        newValues = new ArrayList<>();
                    }

                    if (CollectionUtils.isEmpty(value.getValues())) {
                        IterableUtils.forEach(value.getValues(), row -> {
                            final var newRow = new HashMap<String, String>();
                            for (var i = 0; i < value.getValues().size(); i++) {
                                newRow.put(value.getHvSort().get(i), value.getValues().get(i));
                            }
                            newValues.add(newRow);
                        });
                    }

                    String newValue;
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        newValue = objectMapper.writeValueAsString(newValues);
                    } catch (Exception e) {
                        log.error(
                                "ValueHelpValue with language " + value.getLocale() + " of definition " + def.getId() +
                                        " could not be inserted because no valid values json could be created.");
                        notImportedValues.add("language: " + value.getLocale() + " of definition " + def.getId());
                        return;
                    }

                    if (!isValidJson(newValue)) {
                        log.error(
                                "ValueHelpValue with language " + value.getLocale() + " of definition " + def.getId() +
                                        " could not be inserted because no valid values json could be created.");
                        notImportedValues.add("language: " + value.getLocale() + " of definition " + def.getId());
                        return;
                    }

                    if (existing) {
                        dao.updateValue(newValueHelpDef.getId(), 0L, value.getLocale().toString(),
                                getTimestamp(newValueHelpDef.getTtl()), newValue);
                    } else {
                        dao.addValue(newValueHelpDef.getId(), 0L, value.getLocale().toString(),
                                getTimestamp(newValueHelpDef.getTtl()), newValue);
                    }
                });
                dao.addDef(newValueHelpDef);
            }
        });

        if (notImportedDefs.size() > 0) {
            if (notImportedValues.size() > 0) {
                return "The following value help definitions could not be imported: \n" +
                        notImportedDefs.stream().map(Object::toString).collect(Collectors.joining(", \n")) +
                        ". The following value help values could not be imported: \n" +
                        notImportedValues.stream().map(Object::toString).collect(Collectors.joining(", \n"));
            } else {
                return "The following value help definitions could not be imported: \n" +
                        notImportedDefs.stream().map(Object::toString).collect(Collectors.joining(", \n"));
            }
        } else if (notImportedValues.size() > 0) {
            return "The following value help values could not be imported: \n" +
                    notImportedValues.stream().map(Object::toString).collect(Collectors.joining(", \n"));
        } else {
            return null;
        }
    }

    /**
     * @param json
     * @return
     */
    public boolean isValidJson(String json) {
        try {
            new ObjectMapper().readValue(json, List.class);
        } catch (JacksonException e) {
            return false;
        }
        return true;
    }

    /**
     * @param xmlValueHelps
     * @throws JAXBException
     */
    private void validateXml(@Valid XmlValueHelps xmlValueHelps) throws JAXBException {
        validate(xmlValueHelps);
        if (xmlValueHelps.getValueHelpDefs() != null) {
            for (XmlValueHelpDef def : xmlValueHelps.getValueHelpDefs()) {
                validate(def);
                if (def.getValueHelpValues() != null) {
                    for (XmlValueHelpValue value : def.getValueHelpValues()) {
                        validate(value);
                    }
                }
            }
        }
    }

    /**
     * @param xmlValueHelps
     * @throws JAXBException
     */
    private void validateAbpmXml(@Valid XmlAbpmValueHelps xmlValueHelps) throws JAXBException {
        validate(xmlValueHelps);
        if (xmlValueHelps.getValueHelpDefs() != null) {
            for (XmlAbpmValueHelpDef def : xmlValueHelps.getValueHelpDefs()) {
                validate(def);
                if (def.getValueHelpValues() != null) {
                    for (XmlAbpmValueHelpValue value : def.getValueHelpValues()) {
                        validate(value);
                    }
                }
            }
        }
    }

    /**
     * @param o
     * @throws JAXBException
     */
    private void validate(Object o) throws JAXBException {
        Set<ConstraintViolation<Object>> violations = validator.validate(o);
        if (!validator.validate(o).isEmpty()) {
            for (ConstraintViolation<Object> violation : violations) {
                log.error(violation.getPropertyPath() + ": " + violation.getMessage());
            }
            throw new JAXBException("Class is not valid");
        }
    }

    /**
     * @param id
     * @return
     */
    public Collection<ValueHelp> findValueById(String id) {
        return dao.findAllValuesByDefId(id);
    }

    /**
     * @param id
     * @param locale
     * @return
     */
    public Collection<ValueHelp> findValueByIdLocale(String id, String locale) {
        return dao.findAllValuesByIdLocale(id, locale);
    }

    /**
     * @param id
     * @param locale
     * @return
     */
    public Optional<ValueHelp> findValueLatestVersionByIdLocale(String id, String locale) {
        if (locale.equals("_")) {
            Optional<ValueHelp> vh = dao.findValueByIdLocaleLatestVersion(id, locale);
            if (vh.isPresent()) {
                return vh;
            } else {
                return dao.findValueByIdLocaleLatestVersion(id, "");
            }
        }
        return dao.findValueByIdLocaleLatestVersion(id, locale);
    }

    /**
     * @param id
     * @param locale
     * @param version
     * @return
     */
    public Optional<ValueHelp> findValueByIdLocaleVersion(String id, String locale, long version) {
        return dao.findValueByIdLocaleVersion(id, locale, version);
    }

    /**
     * @param id
     */
    public void deleteValue(String id) {
        dao.deleteValue(id);
    }

    /**
     * @param id
     * @param locale
     */
    public void deleteValue(String id, String locale) {
        dao.deleteValue(id, locale);
    }

    /**
     * @param id
     * @param locale
     */
    public void deleteValue(String id, String locale, Long version) {
        dao.deleteValue(id, locale, version);
    }

    /**
     * @param vh
     * @return
     */
    public ValueHelp addValue(ValueHelp vh) {
        if (vh == null) {
            return null;
        }
        Optional<ValueHelpDef> def = findDefById(vh.getId());
        if (def.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Value help definition with id " + vh.getId() + " does not exist.");
        }
        vh.setValidUntil(getTimestamp(def.get().getTtl()));

        String newValue;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            newValue = objectMapper.writeValueAsString(vh.getValues());
        } catch (Exception e) {
            log.error("ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                    " could not be inserted because no valid values json could be created.");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                            " could not be inserted because no valid values json could be created.");
        }

        if (!isValidJson(newValue)) {
            log.error("ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                    " could not be inserted because no valid values json could be created.");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                            " could not be inserted because no valid values json could be created.");
        }

        dao.addValue(vh.getId(), vh.getVersion(), vh.getLocale().toString(), vh.getValidUntil(), newValue);
        return vh;
    }

    /**
     * @param vh
     * @return
     */
    @Transactional
    public ValueHelp updateValue(ValueHelp vh) {
        var resultOptNew = this.findValueByIdLocaleVersion(vh.getId(), vh.getLocale().toString(), vh.getVersion() + 1);
        if (resultOptNew.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Provided entity is not latest. " + "Please reload data and try again");
        }
        Optional<ValueHelpDef> def = findDefById(vh.getId());
        if (def.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Value help definition with id " + vh.getId() + " does not exist.");
        }
        vh.setValidUntil(getTimestamp(def.get().getTtl()));

        String newValue;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            newValue = objectMapper.writeValueAsString(vh.getValues());
        } catch (Exception e) {
            log.error("ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                    " could not be inserted because no valid values json could be created.");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                            " could not be inserted because no valid values json could be created.");
        }
        if (!isValidJson(newValue)) {
            log.error("ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                    " could not be inserted because no valid values json could be created.");
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                            " could not be inserted because no valid values json could be created.");
        }

        dao.updateValue(vh.getId(), vh.getVersion(), vh.getLocale().toString(), vh.getValidUntil(), newValue);
        return vh;
    }

    /**
     * @param vh
     * @param ttl
     */
    public void addValue(ValueHelp vh, Long ttl) {
        vh.setValidUntil(getTimestamp(ttl));
        final var om =
                new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        try (var reader = new InputStreamReader(new ByteArrayInputStream(om.writeValueAsBytes(vh.getValues())))) {
            String newValue = IOUtils.toString(reader);
            if (!isValidJson(newValue)) {
                log.error("ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                        " could not be inserted because no valid values json could be created.");
                throw new RuntimeException(
                        "ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                                " could not be created because no valid values json could be created.");
            }
            dao.addValue(vh.getId(), vh.getVersion(), vh.getLocale().toString(), vh.getValidUntil(), newValue);
        } catch (Exception e) {
            log.error("Error converting value-help-value", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param vh
     * @param ttl
     */
    public void updateValue(ValueHelp vh, Long ttl) {
        vh.setValidUntil(getTimestamp(ttl));
        final var om =
                new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        try (var reader = new InputStreamReader(new ByteArrayInputStream(om.writeValueAsBytes(vh.getValues())))) {
            String newValue = IOUtils.toString(reader);
            if (!isValidJson(newValue)) {
                log.error("ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                        " could not be updated because no valid values json could be created.");
                throw new RuntimeException(
                        "ValueHelpValue with language " + vh.getLocale() + " of definition " + vh.getId() +
                                " could not be updated because no valid values json could be created.");
            }
            dao.updateValue(vh.getId(), vh.getVersion(), vh.getLocale().toString(), vh.getValidUntil(), newValue);
        } catch (Exception e) {
            log.error("Error converting value-help-value", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param ids
     * @param locale
     * @return
     */
    public Map<String, Long> findValuesVersion(Collection<String> ids, String locale) {
        return dao.findValuesVersion(ids, locale);
    }

    /**
     * @param id
     * @param locale
     * @return
     */
    public Pair<String, Long> findValueById(String id, String locale) {
        return dao.findById(id, locale);
    }

    /**
     * @param ttl
     * @return
     */
    private java.sql.Timestamp getTimestamp(long ttl) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        if (ttl == ValueHelpDef.TTL_STATIC) {
            return MAX_VALID_UNTIL;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(System.currentTimeMillis()));
        cal.add(Calendar.MINUTE, (int) ttl);
        return new java.sql.Timestamp(cal.getTimeInMillis());
    }
}