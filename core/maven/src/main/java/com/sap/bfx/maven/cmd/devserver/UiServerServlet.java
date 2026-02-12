package com.sap.bfx.maven.cmd.devserver;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.regex.Pattern;

@Slf4j
public class UiServerServlet extends HttpServlet {

    final static String FOLDER = "devserver";
    final static String NM_FAVICON = "favicon";
    final static String NM_INDEX_JS = "indexJS";
    final Pattern resourcePathPattern = Pattern.compile("^.*(\\/assets\\/.*)$");
    private final Configuration templateCfg;
    private String favicon;
    private String jsIndex;
    private String cssIndex;

    /**
     *
     */
    public UiServerServlet() {
        super();

        // Initialize the template engine (freemarker)
        templateCfg = new Configuration(Configuration.VERSION_2_3_32);
        templateCfg.setClassLoaderForTemplateLoading(UiServerServlet.class.getClassLoader(), FOLDER);
        templateCfg.setDefaultEncoding("UTF-8");
        templateCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        templateCfg.setLogTemplateExceptions(false);
        templateCfg.setWrapUncheckedExceptions(true);
        templateCfg.setFallbackOnNullLoopVariable(false);

        // determine the paths to the files included directly in index.html
        final var resolver = new PathMatchingResourcePatternResolver();
        try {
            final var iconPattern = Pattern.compile("favicon-.*\\.ico");
            final var jsPattern = Pattern.compile("index-.*\\.js");
            final var cssPattern = Pattern.compile("index-.*\\.css");

            favicon = "";
            jsIndex = "";
            cssIndex = "";

            for (var resource : resolver.getResources("classpath*:" + FOLDER + "/**/*.*")) {
                if (iconPattern.matcher(resource.getFilename()).find()) {
                    favicon = resource.getFilename();
                } else if (jsPattern.matcher(resource.getFilename()).find()) {
                    jsIndex = resource.getFilename();
                } else if (cssPattern.matcher(resource.getFilename()).find()) {
                    cssIndex = resource.getFilename();
                }
            }
            log.debug("Found favicon='{}', index.js='{}', index.css='{}'", favicon, jsIndex, cssIndex);
        } catch (IOException e) {
            log.error("Error initializing FrontendController", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param req an {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param res an {@link HttpServletResponse} object that contains the response the servlet sends to the client
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        log.info("handling path '{}'", req.getRequestURI());

        var mimeType = "";
        var path = "";

        // because there could be prefixes, e.g. from k8s ingress we need to filter these. The rule is, that
        // the path needs to start with "assets", any other resource can don't have and cannot return
        final var m = resourcePathPattern.matcher(req.getRequestURI());
        if (m.matches()) {
            path = m.group(1);
            log.debug("Translated path is '{}'", path);

            if (StringUtils.endsWithIgnoreCase(path, ".js")) {
                mimeType = "text/javascript";
            } else if (StringUtils.endsWithIgnoreCase(path, "css")) {
                mimeType = "text/css";
            } else if (StringUtils.endsWithIgnoreCase(path, ".png")) {
                mimeType = MediaType.IMAGE_PNG_VALUE;
            } else if (StringUtils.endsWithIgnoreCase(path, ".ico")) {
                mimeType = "image/x-icon";
            } else if (StringUtils.endsWithIgnoreCase(path, ".woff2")) {
                mimeType = "font/woff2";
            } else if (StringUtils.endsWithIgnoreCase(path, ".woff")) {
                mimeType = "font/woff";
            } else if (StringUtils.endsWithIgnoreCase(path, ".ttf")) {
                mimeType = "font/ttf";
            } else if (StringUtils.endsWithIgnoreCase(path, ".eot")) {
                mimeType = "application/vnd.ms-fontobject";
            } else if (StringUtils.endsWithIgnoreCase(path, "svg")) {
                mimeType = "image/svg+xml";
            }
        }

        // if it is a resource of given type, handle it here
        if (StringUtils.isNotEmpty(mimeType)) {
            try (final var is = this.getClass().getClassLoader().getResourceAsStream(FOLDER + path)) {
                log.trace("  setting mime type to: " + mimeType);
                res.setContentType(mimeType);
                IOUtils.copy(is, res.getOutputStream());
            }
            return;
        }

        final var values = new HashMap<String, Object>();
        values.put(NM_FAVICON, favicon);
        values.put(NM_INDEX_JS, jsIndex);

        final var template = templateCfg.getTemplate("index.html.ftlh");
        try (final var out = new OutputStreamWriter(res.getOutputStream())) {
            res.setContentType(MediaType.TEXT_HTML_VALUE);
            try {
                template.process(values, out);
            } catch (TemplateException e) {
                log.error("Error in index", e);
                throw new IOException(e);
            }
        }
    }
}
