package com.sap.bfx.api;

import com.sap.bfx.callback.CallbackService;
import com.sap.bfx.callback.ContextFactory;
import com.sap.bfx.callback.LifecycleHookType;
import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.exception.NotFoundException;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.security.SecurityUtils;
import com.sap.bfx.session.AttachmentService;
import com.sap.bfx.session.SessionService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * Controller for handling attachment-related operations.
 */
@RestController
@RequestMapping("api/v1/attachments")
@Slf4j
@Hidden
public class AttachmentController {

    private final static String PARAM_FILE = "file";
    private final static String PARAM_SESSION_ID = "sessionId";
    private final static String PARAM_ROW = "row";
    private final static String PARAM_KEY = "key";
    private final static String PARAM_CATEGORY = "cat";
    private final static String PARAM_DESCRIPTION = "desc";

    private final CallbackService callbackService;
    private final SessionService sessionService;
    private final AttachmentService attachmentService;
    private final TaskExecutor taskExecutor;
    private final ContextFactory contextFactory;
    private final SecurityService securityService;

    /**
     * Constructor for AttachmentController.
     *
     * @param callbackService   the callback service
     * @param sessionService    the session service
     * @param attachmentService the attachment service
     * @param taskExecutor      the task executor
     * @param contextFactory    the context factory
     * @param securityService   the security service
     */
    @Autowired
    public AttachmentController(final CallbackService callbackService, final SessionService sessionService,
                                final AttachmentService attachmentService, final TaskExecutor taskExecutor,
                                final ContextFactory contextFactory, final SecurityService securityService) {
        this.callbackService = callbackService;
        this.sessionService = sessionService;
        this.attachmentService = attachmentService;
        this.taskExecutor = taskExecutor;
        this.contextFactory = contextFactory;
        this.securityService = securityService;
    }

    /**
     * Handles the upload of an attachment.
     *
     * @param file    the file to be uploaded
     * @param request the HTTP servlet request
     * @param token   the authentication token
     * @return a ResponseEntity containing the session result
     * @throws Exception if an error occurs during the upload process
     */
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> upload(@RequestParam(PARAM_FILE) MultipartFile file, HttpServletRequest request,
                                         AbstractAuthenticationToken token) throws Exception {

        log.debug("AttachmentController.upload called.");

        final var charset = Charset.forName(request.getCharacterEncoding());
        final var sessionId = request.getParameter(PARAM_SESSION_ID);
        final var rowId = request.getParameter(PARAM_ROW);
        final var key = request.getParameter(PARAM_KEY);
        final var category = ControllerUtils.getUTF8Param(request, PARAM_CATEGORY, charset);
        final var description = ControllerUtils.getUTF8Param(request, PARAM_DESCRIPTION, charset);

        if (StringUtils.isBlank(sessionId)) {
            throw new BadRequestException("missing session-id");
        }
        if (StringUtils.isBlank(rowId)) {
            throw new BadRequestException("missing row-id");
        }
        if (StringUtils.isBlank(key)) {
            throw new BadRequestException("missing key");
        }
        if (file == null) {
            throw new BadRequestException("missing file");
        }

        // Get the security session from the Spring Security context. This contains information about the authenticated
        // user and their roles/permissions.
        final var securitySession = SecurityUtils.getSecuritySession();

        // load session from store and ensure it exists. If it does not exist, throw a Not.
        final var session = sessionService.findById(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
        }

        // Ensure the user is authorized to upload the attachment. If not, throw an Unauthorized exception.
        securityService.ensureAuthorized(session.getForm().getSd().getName(), securitySession.getUser(),
                EventType.UploadAttachmentAuth, false, rowId, key);

        var context = contextFactory.createContext(securitySession, session.getForm().getSd(), session,
                session.getDisplayState(), session.getLocale(), rowId, key, null);
        var result = callbackService.callLifecycleHook(LifecycleHookType.StartRoundtrip, context, null);

        // TODO(ML) Read and apply Journal if we add some lifecycle/event callbacks here

        attachmentService.addAttachment(session.getForm(), rowId, key, context, file, category, description);

        // callback at end of round-trip
        result = callbackService.callLifecycleHook(LifecycleHookType.EndRoundtrip, context, result);

        // save the session to session store
        final var wg = new CountDownLatch(1);
        taskExecutor.execute(() -> {
            sessionService.save(session);
            wg.countDown();
        });

        final var response = new SessionResponse(session.getId(), result, session.getForm(), session.getJournal());
        var jsonResponse = ControllerUtils.createSessionResult(response);

        // wait until session is stored...
        wg.await();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).cacheControl(CacheControl.noCache())
                             .body(jsonResponse.toByteArray());
    }

    /**
     * Handles the download of an attachment.
     *
     * @param sessionId the session ID
     * @param key       the key associated with the attachment
     * @param id        the ID of the attachment
     * @param response  the HTTP servlet response
     * @param token     the authentication token
     * @throws Exception if an error occurs during the download process
     */
    @GetMapping(value = "{sessionId}/{key}/{id}")
    public void download(@PathVariable("sessionId") final String sessionId, @PathVariable("key") final String key,
                         @PathVariable("id") final String id, HttpServletResponse response,
                         AbstractAuthenticationToken token) throws Exception {

        if (StringUtils.isBlank(sessionId)) {
            throw new BadRequestException("missing session-id");
        }
        if (StringUtils.isBlank(key)) {
            throw new BadRequestException("missing key");
        }
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("missing id");
        }

        // Get the security session from the Spring Security context. This contains information about the authenticated
        // user and their roles/permissions.
        final var securitySession = SecurityUtils.getSecuritySession();

        // load session from store and ensure it exists. If it does not exist, throw a Not.
        final var session = sessionService.findById(sessionId);
        if (session == null) {
            response.setStatus(HttpStatus.REQUEST_TIMEOUT.value());
            return;
        }

        // check if the user is authorized to download the attachment. If not, throw an Unauthorized exception.
        securityService.ensureAuthorized(session.getForm().getSd().getName(), securitySession.getUser(),
                EventType.DownloadAttachmentAuth, false, null, (String) null);

        var context = contextFactory.createContext(securitySession, session.getForm().getSd(), session,
                session.getDisplayState(), session.getLocale(), null, null, null);
        var result = callbackService.callLifecycleHook(LifecycleHookType.StartRoundtrip, context, null);

        final var opt = attachmentService.load(session.getForm(), key, id, context);
        if (opt.isEmpty()) {
            throw new NotFoundException("cannot find attachment with id ''" + id + "'");
        }

        // callback at end of round-trip
        result = callbackService.callLifecycleHook(LifecycleHookType.EndRoundtrip, context, result);

        final var attachment = opt.get().getLeft();

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(
                StringUtils.isBlank(attachment.getContentType()) ? MediaType.APPLICATION_OCTET_STREAM_VALUE :
                        attachment.getContentType());
        response.setContentLength(Math.toIntExact(attachment.getSize()));
        response.setHeader("Content-disposition",
                "attachment; filename=" + URLEncoder.encode(attachment.getFileName(), StandardCharsets.ISO_8859_1));
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        try {
            IOUtils.copy(opt.get().getRight(), response.getOutputStream());
        } catch (Exception e) {
            log.error("Error in downlaod", e);
        } finally {
            IOUtils.closeQuietly(opt.get().getRight());
        }
    }

    /**
     * Handles the deletion of an attachment.
     *
     * @param sessionId the session ID
     * @param rowId     the row ID associated with the attachment
     * @param key       the key associated with the attachment
     * @param id        the ID of the attachment
     * @param token     the authentication token
     * @return a ResponseEntity containing the session result
     * @throws Exception if an error occurs during the deletion process
     */
    @DeleteMapping(value = "{sessionId}/{rowId}/{key}/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> delete(@PathVariable("sessionId") final String sessionId,
                                         @PathVariable("rowId") final String rowId,
                                         @PathVariable("key") final String key, @PathVariable("id") final String id,
                                         AbstractAuthenticationToken token) throws Exception {

        if (StringUtils.isBlank(sessionId)) {
            throw new BadRequestException("missing session-id");
        }
        if (StringUtils.isBlank(rowId)) {
            throw new BadRequestException("missing rowId");
        }
        if (StringUtils.isBlank(key)) {
            throw new BadRequestException("missing key");
        }
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("missing id");
        }

        // Get the security session from the Spring Security context. This contains information about the authenticated
        // user and their roles/permissions.
        final var securitySession = SecurityUtils.getSecuritySession();

        // load session from store and ensure it exists. If it does not exist, throw a Not.
        final var session = sessionService.findById(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
        }

        securityService.ensureAuthorized(session.getForm().getSd().getName(), securitySession.getUser(),
                EventType.DeleteAttachmentAuth, false, rowId, key);

        var context = contextFactory.createContext(securitySession, session.getForm().getSd(), session,
                session.getDisplayState(), session.getLocale(), rowId, key, null);
        var result = callbackService.callLifecycleHook(LifecycleHookType.StartRoundtrip, context, null);

        if (!attachmentService.deleteAttachment(session.getForm(), rowId, key, id, context)) {
            throw new NotFoundException("cannot find attachment with id ''" + id + "'");
        }

        // callback at end of round-trip
        result = callbackService.callLifecycleHook(LifecycleHookType.EndRoundtrip, context, result);

        // save the session to session store
        final var wg = new CountDownLatch(1);
        taskExecutor.execute(() -> {
            sessionService.save(session);
            wg.countDown();
        });

        final var response = new SessionResponse(session.getId(), result, session.getForm(), session.getJournal());
        var jsonResponse = ControllerUtils.createSessionResult(response);

        // wait until session is stored...
        wg.await();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).cacheControl(CacheControl.noCache())
                             .body(jsonResponse.toByteArray());
    }
}
