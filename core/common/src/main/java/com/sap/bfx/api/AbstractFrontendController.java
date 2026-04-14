package com.sap.bfx.api;

import com.sap.bfx.exception.FormsCoreException;
import com.sap.bfx.security.Constants;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.Principal;
import java.util.HashMap;
import java.util.regex.Pattern;

@Slf4j
public abstract class AbstractFrontendController {

    final static String FOLDER = "frontend";
    final static String NM_HAS_TOKEN = "hasToken";
    final static String NM_ACCESS_TOKEN = "accessToken";
    final static String NM_REFRESH_TOKEN = "refreshToken";
    final static String NM_FAVICON = "favicon";
    final static String NM_INDEX_JS = "indexJS";
    final static String NM_INDEX_CSS = "indexCSS";
    final static String NM_CONTEXT_PATH = "contextPath";
    final Pattern resourcePathPattern = Pattern.compile("^.*(\\/assets\\/.*)$");
    private final ApplicationContext applicationContext;
    private final Configuration templateCfg;
    @Value("${forms.security.auth.type:none}")
    public String AUTH_TYPE;
    @Value("${forms.security.auth.client-id:none}")
    public String AUTH_CLIENT_ID;
    @Value("${forms.contextPath:#{null}}")
    private String contextPath;
    private String favicon;
    private String jsIndex;
    private String cssIndex;
    @Value("${forms.api-doc.enabled:false}")
    private boolean apiDocEnabled;
    @Value("${forms.api-doc.path:/api-docs}")
    private String apiDocPath;

    protected AbstractFrontendController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        // Initialize the template engine (freemarker)
        templateCfg = new Configuration(Configuration.VERSION_2_3_32);
        templateCfg.setClassLoaderForTemplateLoading(AbstractFrontendController.class.getClassLoader(), FOLDER);
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
            throw new FormsCoreException("Error initializing FrontendController", e);
        }
    }

    @RequestMapping(value = "/**")
    public String handler(HttpServletRequest req, HttpServletResponse res, Principal principal) {

        log.debug("Handling resource '{}'", req.getRequestURI());

        // Set header to allow including the content via iframe
        res.setHeader("Content-Security-Policy", "frame-ancestors http://localhost:* https://*.cloud.sap https://*.ondemand.com");

        if (apiDocEnabled && StringUtils.equals(req.getRequestURI(), apiDocPath)) {
            log.info("API documentation is enabled, redirecting to {}", apiDocPath);
            return "forward:/v3/api-docs";
        }

        var mimeType = "";
        var path = "";

        // because there could be prefixes, e.g. from k8s ingress we need to filter
        // these. The rule is, that
        // the path needs to start with "assets", any other resource can don't have and
        // cannot return
        final var m = resourcePathPattern.matcher(req.getRequestURI());
        if (m.matches()) {
            path = m.group(1);
            log.debug("Translated path is '{}'", path);

            if (StringUtils.endsWithIgnoreCase(path, ".js")
                    || StringUtils.endsWithIgnoreCase(path, ".mjs")) {
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
                res.addHeader("Cache-Control", "public, immutable, max-age=604800");
                IOUtils.copy(is, res.getOutputStream());
            } catch (IOException e) {
                throw new FormsCoreException("Error reading resource '" + path + "'", e);
            }
            return null;
        }

        // handling of index...
        String accessToken = null;
        String refreshToken = null;

        if (StringUtils.equalsIgnoreCase(AUTH_TYPE, Constants.AUTH_TYPE_OIDC)) {
            var oAuth2AuthorizedClientRepository = applicationContext.getBean(OAuth2AuthorizedClientRepository.class);
            var oauth2Token = (OAuth2AuthenticationToken) principal;
            var userInfo = (DefaultOidcUser) (oauth2Token).getPrincipal();
            accessToken = (oAuth2AuthorizedClientRepository.loadAuthorizedClient(AUTH_CLIENT_ID, oauth2Token, req))
                    .getAccessToken().getTokenValue();
            refreshToken = (oAuth2AuthorizedClientRepository.loadAuthorizedClient(AUTH_CLIENT_ID, oauth2Token, req))
                    .getRefreshToken().getTokenValue();
            log.debug("User: '{}' logged in with ID Token: '{}'", userInfo.getName(), accessToken);
        }

        final var values = new HashMap<String, Object>();
        values.put(NM_HAS_TOKEN, StringUtils.isNotBlank(accessToken));
        values.put(NM_ACCESS_TOKEN, accessToken);
        values.put(NM_REFRESH_TOKEN, refreshToken);
        values.put(NM_FAVICON, favicon);
        values.put(NM_INDEX_JS, jsIndex);
        values.put(NM_INDEX_CSS, cssIndex);
        values.put(NM_CONTEXT_PATH, StringUtils.isBlank(contextPath) ? "/" : contextPath);

        try (final var out = new OutputStreamWriter(res.getOutputStream())) {
            final var template = templateCfg.getTemplate("index.html.ftlh");
            res.setContentType(MediaType.TEXT_HTML_VALUE);
            res.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            template.process(values, out);
        } catch (Exception e) {
            throw new FormsCoreException("Error processing index", e);
        }

        return null;
    }
}