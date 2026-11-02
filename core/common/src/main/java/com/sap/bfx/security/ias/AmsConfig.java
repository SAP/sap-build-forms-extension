package com.sap.bfx.security.ias;

import com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding;
import com.sap.cloud.security.ams.api.AuthorizationManagementService;
import com.sap.cloud.security.ams.core.AuthorizationManagementServiceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Explicitly provides the {@link AuthorizationManagementService} bean by reading the
 * X509 identity service binding from the mounted Secret volume.
 *
 * <p>The AMS {@code spring-boot-starter-ams} auto-configuration relies on
 * {@code DefaultServiceBindingAccessor} to discover the binding, but the SAP BTP
 * Service Operator mounts Secrets as individual flat files (one file per key) without
 * the {@code .metadata} file that the accessor requires. We therefore construct the
 * binding manually and register it as a primary bean so
 * {@code AmsAutoConfiguration.authProvider()} can wire it.
 */
@Configuration
@Conditional(IasEnabledCondition.class)
@Slf4j
public class AmsConfig {

    /**
     * Root path where service bindings are mounted, e.g. {@code /etc/secrets/sapbtp}.
     * Must match {@code SERVICE_BINDING_ROOT} in the pod spec.
     */
    @Value("${SERVICE_BINDING_ROOT:/etc/secrets/sapbtp}")
    private String serviceBindingRoot;

    /**
     * Name of the X509 identity binding directory under {@link #serviceBindingRoot}.
     * This matches the {@code secretName} of the X509 ServiceBinding resource.
     */
    @Value("${forms.security.ams.binding-name:sbfx-identity-service-x509-binding}")
    private String amsBindingName;

    /**
     * Builds the {@link AuthorizationManagementService} from the mounted X509
     * identity binding files.
     *
     * <p>Each file under {@code <serviceBindingRoot>/<amsBindingName>/} is treated
     * as a credential key (filename) → value (file content). The resulting
     * {@link DefaultServiceBinding} is passed to
     * {@link AuthorizationManagementServiceBuilder#fromIdentityServiceBinding}.
     */
    @Bean
    public AuthorizationManagementService authorizationManagementService() throws IOException {
        final Path bindingDir = Paths.get(serviceBindingRoot, amsBindingName);
        log.info("AmsConfig: reading identity binding from '{}'", bindingDir);

        if (!Files.isDirectory(bindingDir)) {
            log.error("AmsConfig: binding directory '{}' does not exist — AMS will not be available", bindingDir);
            return null;
        }

        final Map<String, Object> credentials = new HashMap<>();
        try (Stream<Path> files = Files.list(bindingDir)) {
            files.filter(Files::isRegularFile).forEach(file -> {
                try {
                    final String key = file.getFileName().toString();
                    final String value = Files.readString(file, StandardCharsets.UTF_8).strip();
                    credentials.put(key, value);
                } catch (IOException e) {
                    log.warn("AmsConfig: could not read binding file '{}': {}", file, e.getMessage());
                }
            });
        }

        log.info("AmsConfig: loaded {} credential keys from binding: {}",
                credentials.size(), credentials.keySet());

        final DefaultServiceBinding binding = DefaultServiceBinding.builder()
                .copy(credentials)
                .withName(amsBindingName)
                .withServiceName("identity")
                .withCredentials(credentials)
                .build();

        log.info("AmsConfig: creating AuthorizationManagementService from identity binding");
        return AuthorizationManagementServiceBuilder.fromIdentityServiceBinding(binding);
    }
}
