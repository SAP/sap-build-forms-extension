package com.sap.bfx.api;

import com.sap.bfx.callback.CallbackService;
import com.sap.bfx.callback.ContextFactory;
import com.sap.bfx.callback.LifecycleHookType;
import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.exception.NotFoundException;
import com.sap.bfx.security.SecurityService;
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
    private final ControllerUtils utils;
    private final AttachmentService attachmentService;
    private final TaskExecutor taskExecutor;
    private final ContextFactory contextFactory;
    private final SecurityService securityService;

    @Autowired
    public AttachmentController(final CallbackService callbackService, final SessionService sessionService,
                                final ControllerUtils utils, final AttachmentService attachmentService,
                                final TaskExecutor taskExecutor, final ContextFactory contextFactory, final SecurityService securityService) {
        this.callbackService = callbackService;
        this.sessionService = sessionService;
        this.utils = utils;
        this.attachmentService = attachmentService;
        this.taskExecutor = taskExecutor;
        this.contextFactory = contextFactory;
        this.securityService = securityService;
    }

    /**
     * @param file
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> upload(@RequestParam(PARAM_FILE) MultipartFile file, HttpServletRequest request,
                                         AbstractAuthenticationToken token)
            throws Exception {

        log.debug("AttachmentController.upload called.");

        final var charset = Charset.forName(request.getCharacterEncoding());
        final var sessionId = request.getParameter(PARAM_SESSION_ID);
        final var rowId = request.getParameter(PARAM_ROW);
        final var key = request.getParameter(PARAM_KEY);
        final var category = utils.getUTF8Param(request, PARAM_CATEGORY, charset);
        final var description = utils.getUTF8Param(request, PARAM_DESCRIPTION, charset);

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
        securityService.ensureAuthorized(token, EventType.UploadAttachmentAuth, Boolean.FALSE, rowId, key);

        var context = contextFactory.createContext(token, null, null, null, null, rowId, key, null);
        var result = callbackService.callLifecycleHook(LifecycleHookType.StartRoundtrip, context, null);

        // load session from store
        final var session = sessionService.findById(sessionId);

        // TODO(ML) Read and apply Journal if we add some lifecycle/event callbacks here

        // Creating "real" context
        context = contextFactory.createContext(token, session.getForm().getSd(), session, context.getDisplayState(),
                context.getLocale(), rowId, key, null);

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
        var jsonResponse = utils.createSessionResult(response);

        // wait until session is stored...
        wg.await();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.noCache())
                .body(jsonResponse.toByteArray());
    }

    /**
     * @param sessionId
     * @param key
     * @param id
     * @param response
     * @throws Exception
     */
    @GetMapping(value = "{sessionId}/{key}/{id}")
    public void download(@PathVariable("sessionId") final String sessionId,
                         @PathVariable("key") final String key,
                         @PathVariable("id") final String id,
                         HttpServletResponse response,
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
        securityService.ensureAuthorized(token, EventType.DownloadAttachmentAuth, Boolean.FALSE, null, (String) null);

        var context = contextFactory.createContext(token, null, null, null, null, null, null, null);
        var result = callbackService.callLifecycleHook(LifecycleHookType.StartRoundtrip, context, null);

        // load session from store
        final var session = sessionService.findById(sessionId);

        // Creating "real" context
        context = contextFactory.createContext(token, session.getForm().getSd(), session, context.getDisplayState(),
                context.getLocale(), null, null, null);

        final var opt = attachmentService.load(session.getForm(), key, id, context);
        if (opt.isEmpty()) {
            throw new NotFoundException("cannot find attachment with id ''" + id + "'");
        }

        // callback at end of round-trip
        result = callbackService.callLifecycleHook(LifecycleHookType.EndRoundtrip, context, result);

        final var attachment = opt.get().getLeft();

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(StringUtils.isBlank(attachment.getContentType())
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : attachment.getContentType());
        response.setContentLength(Math.toIntExact(attachment.getSize()));
        response.setHeader("Content-disposition", "attachment; filename=" +
                URLEncoder.encode(attachment.getFileName(), StandardCharsets.ISO_8859_1));
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
     * @param sessionId
     * @param rowId
     * @param key
     * @param id
     * @throws Exception
     */
    @DeleteMapping(value = "{sessionId}/{rowId}/{key}/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> delete(@PathVariable("sessionId") final String sessionId,
                                         @PathVariable("rowId") final String rowId,
                                         @PathVariable("key") final String key,
                                         @PathVariable("id") final String id,
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
        securityService.ensureAuthorized(token, EventType.DeleteAttachmentAuth, Boolean.FALSE, rowId, key);

        var context = contextFactory.createContext(token, null, null, null, null, rowId, key, null);
        var result = callbackService.callLifecycleHook(LifecycleHookType.StartRoundtrip, context, null);

        // load session from store
        final var session = sessionService.findById(sessionId);

        // Creating "real" context
        context = contextFactory.createContext(token, session.getForm().getSd(), session, context.getDisplayState(),
                context.getLocale(), rowId, key, null);

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
        var jsonResponse = utils.createSessionResult(response);

        // wait until session is stored...
        wg.await();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.noCache())
                .body(jsonResponse.toByteArray());
    }
}
