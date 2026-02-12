package com.sap.bfx.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


@Component
public class ControllerUtils {

    public static final String SLASH = "/";
    private final ObjectMapper sessionResponseOm;

    /**
     *
     */
    public ControllerUtils() {
        sessionResponseOm = new ObjectMapper();
        var module = new SimpleModule();
        module.addSerializer(SessionResponse.class, new SessionResponse.SessionResponseSerializer());
        sessionResponseOm.registerModule(module);
    }

    /**
     * @param response
     * @return
     * @throws Exception
     */
    public ByteArrayOutputStream createSessionResult(final SessionResponse response) throws Exception {
        final var os = new ByteArrayOutputStream();
        sessionResponseOm.writeValue(os, response);
        return os;
    }

    /**
     * @param request
     * @param nm
     * @param charset
     * @return
     */
    public String getUTF8Param(final HttpServletRequest request, final String nm, final Charset charset) {
        final var input = request.getParameter(nm);

        if (StringUtils.isNotBlank(input)) {
            return new String(input.getBytes(charset), StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * @param principalName
     * @return
     */
    public String getSimplifiedPrincipalName(final String principalName) {
        if (!principalName.contains(SLASH)) {
            return principalName;
        }
        return principalName.substring(principalName.lastIndexOf(SLASH) + 1);
    }
}

