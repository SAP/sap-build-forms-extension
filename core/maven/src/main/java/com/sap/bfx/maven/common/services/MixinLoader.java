package com.sap.bfx.maven.common.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sap.bfx.definition.MetaFileElementDefinition;
import com.sap.bfx.definition.MixinDefinition;
import com.sap.bfx.definition.MixinDefinitionDeserializer;
import com.sap.bfx.utils.PropertyFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

abstract class MixinLoader {

    protected MixinService.ProcessingInfo processingInfo;
    protected MetaFileElementDefinition mixin;
    protected Log log;

    /**
     * @param processingInfo
     * @param mixin
     * @param log
     */
    protected MixinLoader(final MixinService.ProcessingInfo processingInfo, final MetaFileElementDefinition mixin,
                          final Log log) {
        this.processingInfo = processingInfo;
        this.mixin = mixin;
        this.log = log;
    }

    /**
     * @param processingInfo
     * @param mixin
     * @param log
     * @return
     */
    public static MixinLoader create(final MixinService.ProcessingInfo processingInfo,
                                     final MetaFileElementDefinition mixin, final Log log) {
        final var path = StringUtils.lowerCase(StringUtils.trim(mixin.getPath()));
        if (StringUtils.startsWith(path, "file:")) {
            return new MixinFileLoader(processingInfo, mixin, log);
        } else if (StringUtils.startsWith(path, "classpath:")) {
            return new MixinClasspathLoader(processingInfo, mixin, log);
        }
        throw new IllegalArgumentException("path for mixin doesn't with 'file:' nor 'classpath:'");
    }

    /**
     * @param is
     * @return
     * @throws Exception
     */
    public static MixinDefinition readElementDef(final InputStreamReader is) throws Exception {
        final var om = new ObjectMapper(new YAMLFactory()).configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final var module = new SimpleModule();
        module.addDeserializer(MixinDefinition.class, new MixinDefinitionDeserializer(true));
        om.registerModule(module);

        return om.readValue(is, MixinDefinition.class);
    }

    /**
     * @param is
     * @param locale
     * @param prefix
     * @param mixinDef
     * @throws Exception
     */
    protected static void readTexts(final InputStream is, final Locale locale, final String prefix,
                                    final MixinDefinition mixinDef)
            throws Exception {

        final var texts = mixinDef.getTexts().computeIfAbsent(locale, k -> new HashMap<>());
        PropertyFileUtils.readTexts(is, prefix + "_", texts);
    }

    /**
     * @return
     * @throws Exception
     */
    public abstract MixinDefinition load() throws Exception;
}
