package com.sap.bfx.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom wrapper for HttpServletRequest that allows adding custom headers to the request.
 * This is useful for scenarios where you need to modify or add headers before processing the request further.
 */
public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String> customHeaders;

    /**
     * Constructs a new CustomHttpServletRequestWrapper.
     *
     * @param request the original HttpServletRequest to wrap
     */
    public CustomHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.customHeaders = new HashMap<>();
    }

    /**
     * Adds a custom header to the request.
     *
     * @param name  the name of the header
     * @param value the value of the header
     */
    public void addHeader(String name, String value) {
        this.customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        return this.customHeaders.containsKey(name) ? this.customHeaders.get(name) : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return this.customHeaders.containsKey(name) ?
                Collections.enumeration(Collections.singletonList(this.customHeaders.get(name))) :
                super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        var allHeaders = Collections.list(super.getHeaderNames());
        allHeaders.addAll(this.customHeaders.keySet());
        return Collections.enumeration(allHeaders);
    }
}
