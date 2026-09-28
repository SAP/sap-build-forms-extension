package com.sap.bfx.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for controller-related operations.
 */
public final class ControllerUtils {
    
    private static final ObjectMapper sessionResponseOm;

    static {
        sessionResponseOm = new ObjectMapper();
        var module = new SimpleModule();
        module.addSerializer(SessionResponse.class, new SessionResponse.SessionResponseSerializer());
        sessionResponseOm.registerModule(module);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private ControllerUtils() {
    }

    /**
     * Creates a ByteArrayOutputStream containing the serialized SessionResponse.
     *
     * @param response the SessionResponse to serialize
     * @return a ByteArrayOutputStream containing the serialized response
     * @throws Exception if an error occurs during serialization
     */
    public static ByteArrayOutputStream createSessionResult(final SessionResponse response) throws Exception {
        final var os = new ByteArrayOutputStream();
        sessionResponseOm.createGenerator(os).writeObject(response);
        return os;
    }

    /**
     * Retrieves a UTF-8 encoded parameter from the HttpServletRequest.
     *
     * @param request the HttpServletRequest
     * @param nm      the name of the parameter
     * @param charset the character set to use for decoding
     * @return the UTF-8 encoded parameter value, or null if not found or blank
     */
    public static String getUTF8Param(final HttpServletRequest request, final String nm, final Charset charset) {
        final var input = request.getParameter(nm);

        if (StringUtils.isNotBlank(input)) {
            return new String(input.getBytes(charset), StandardCharsets.UTF_8);
        }
        return null;
    }
}

