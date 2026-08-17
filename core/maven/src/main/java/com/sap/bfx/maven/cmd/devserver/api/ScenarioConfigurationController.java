package com.sap.bfx.maven.cmd.devserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sap.bfx.maven.cmd.devserver.ConfigurationService;
import com.sap.bfx.maven.common.AbstractProcessor;
import com.sap.bfx.maven.common.definition.ValidationMessageSerializer;
import com.sap.bfx.maven.common.services.CheckAndProcessService;
import com.sap.bfx.maven.common.services.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.util.List;

@RestController
@RequestMapping("v1/scenarios")
@Slf4j
public class ScenarioConfigurationController {

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private CheckAndProcessService checkAndProcessService;

//    @Autowired
//    private ApplicationContext appContext;

    @Autowired
    private ConfigurationService cfgService;

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findScenarios() {
        cfgService.getLog().info("calling findScenarios");

        try {
            metadataService.scanDefinitionMetadata(cfgService.getMetadataFolder());
            return ResponseEntity.ok(metadataService.getMetadataAsJson(true));
        } catch (Exception e) {
            cfgService.getLog().error(e);
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @GetMapping(value = "/mixins", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findMixins() {
        cfgService.getLog().info("calling findMixins");

        try {
            metadataService.scanMixinMetadataWithClasspath(cfgService.getMetadataFolder(), cfgService.getProject());
            return ResponseEntity.ok(metadataService.getMixinMetadataAsJson(true));
        } catch (Exception e) {
            cfgService.getLog().error(e);
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @PutMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateScenarios(HttpServletRequest req) {

        try {
            // read the input from request's input stream into metadata-service internal cache
            metadataService.readMetadataFromJson(req.getInputStream());

            // write metadata to files
            metadataService.writeScenarioMetadataFiles(cfgService.getMetadataFolder());

            // send the metadata back as response
            return ResponseEntity.ok(metadataService.getMetadataAsJson(true));

        } catch (Exception e) {
            cfgService.getLog().error(e);
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @PutMapping(value = "/mixins", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateMixins(HttpServletRequest req) {

        try {
            // read the input from request's input stream into metadata-service internal cache
            metadataService.readMixinMetadataFromJson(req.getInputStream());

            // write metadata to files (classpath-loaded mixins are filtered out inside the service)
            metadataService.writeMixinMetadataFiles(cfgService.getMetadataFolder());

            // re-scan so the response reflects the on-disk local state PLUS the read-only classpath mixins
            metadataService.scanMixinMetadataWithClasspath(cfgService.getMetadataFolder(), cfgService.getProject());

            // send the metadata back as response
            return ResponseEntity.ok(metadataService.getMixinMetadataAsJson(true));

        } catch (Exception e) {
            cfgService.getLog().error(e);
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }

    @PutMapping(value = "/check", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> check(HttpServletRequest req) {

        try {
            metadataService.readMetadataFromJson(req.getInputStream());
            MavenProject project = configurationService.getProject();
            List<AbstractProcessor.ValidationMessage> messages =
                    checkAndProcessService.checkAndProcessWithMessages(project);

            ObjectMapper validationMessageResponse = new ObjectMapper();
            var module = new SimpleModule();
            module.addSerializer(AbstractProcessor.ValidationMessage.class, new ValidationMessageSerializer());
            validationMessageResponse.registerModule(module);

            final StringWriter os = new StringWriter();
            validationMessageResponse.writeValue(os, messages);

            return ResponseEntity.ok(os.toString());

        } catch (Exception e) {
            cfgService.getLog().error(e);
            return ResponseEntity.internalServerError().body(e.getLocalizedMessage());
        }
    }
}