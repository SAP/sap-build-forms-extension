package com.sap.bfx.session;

import com.sap.bfx.callback.AbstractAdapterHandlingService;
import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.AttachmentAdapter;
import com.sap.bfx.callback.Context;
import com.sap.bfx.definition.AttachmentElementDefinition;
import com.sap.bfx.definition.UIElementType;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.exception.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling attachments
 *
 */
@Service
@Slf4j
public class AttachmentService extends AbstractAdapterHandlingService<AttachmentAdapter> {

    /**
     * @param applicationContext
     */
    @Autowired
    public AttachmentService(final ApplicationContext applicationContext) {
        super(applicationContext, AttachmentAdapter.class);
    }

    /**
     * Add an attachment to a form element
     *
     * @param form
     * @param rowId
     * @param key
     * @param ctx
     * @param file
     * @param category
     * @param description
     * @return
     */
    public Attachment addAttachment(final Form form, final String rowId, final String key,
                                    final Context<? extends AccessClass> ctx, final MultipartFile file,
                                    final String category, final String description) {

        final var elementDef = ctx.getScenarioDefinition().findElementByKey(key);
        if (elementDef.getType() != UIElementType.Attachment) {
            throw new BadRequestException("Cannot add attachment to '" + key + "' because type is not attachment");
        }

        final var attachmentDef = (AttachmentElementDefinition) elementDef;
        final var element = FormUtils.findElementByRowAndKey(form, rowId, key);
        final var attachments = (Attachments) element.getValue();
        final var attachment = new Attachment();

        // TODO(ML)
        // checks
        // element.clearMessage();
        // for (var rule : attachmentDef.getValidationRules()) {
        // rule.validate()
        // }

        final var adapter = getAdapter(attachmentDef.getAdapter());
        if (adapter == null) {
            throw new BadRequestException("Adapter '" + attachmentDef.getAdapter() + "' not found!");
        }
        try {
            attachment.setId(UUID.randomUUID().toString());
            attachment.setRef(adapter.save(attachment.getId(), file.getInputStream(), file.getName(),
                    file.getContentType(), file.getSize(), category, description));
            attachment.setFileName(FilenameUtils.getName(file.getOriginalFilename()));
            attachment.setSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setCategory(category);
            attachment.setDescription(description);
            final var optPos = attachments.stream().max(Comparator.comparingInt(Attachment::getPos));
            attachment.setPos(optPos.isPresent() ? optPos.get().getPos() + 10 : 0);

            attachments.add(attachment);
            form.getJournal().updateValue(element, rowId, attachments, true);

            return attachment;
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Load an attachment from a form element
     *
     * @param form
     * @param key
     * @param id
     * @param ctx
     * @return
     */
    public Optional<Pair<Attachment, InputStream>> load(final Form form, final String key, final String id,
                                                        final Context<? extends AccessClass> ctx) {

        final var elementDef = ctx.getScenarioDefinition().findElementByKey(key);
        if (elementDef.getType() != UIElementType.Attachment) {
            throw new BadRequestException("Cannot load attachment to '" + key + "' because type is not attachment");
        }

        final var optAttachment = form.findAttachmentById(key, id);

        if (optAttachment.isPresent()) {
            final var adapter = this.getAdapter(((AttachmentElementDefinition) elementDef).getAdapter());
            final var is = adapter.load(optAttachment.get().getLeft().getRef());

            return Optional.of(new ImmutablePair<>(optAttachment.get().getLeft(), is));
        }

        return Optional.empty();
    }

    /**
     * Delete an attachment from a form element
     *
     * @param form
     * @param rowId
     * @param key
     * @param id
     * @param ctx
     * @return
     */
    public boolean deleteAttachment(final Form form, final String rowId, final String key, final String id,
                                    final Context<? extends AccessClass> ctx) {
        final var elementDef = ctx.getScenarioDefinition().findElementByKey(key);
        if (elementDef.getType() != UIElementType.Attachment) {
            throw new BadRequestException("Cannot load attachment to '" + key + "' because type is not attachment");
        }

        final var attachmentDef = (AttachmentElementDefinition) elementDef;
        final var adapter = getAdapter(attachmentDef.getAdapter());
        if (adapter == null) {
            throw new BadRequestException("Adapter '" + attachmentDef.getAdapter() + "' not found!");
        }

        final var optAttachment = form.findAttachmentById(key, id);
        if (optAttachment.isPresent()) {
            try {
                adapter.delete(optAttachment.get().getLeft().getRef());
                final var attachments = optAttachment.get().getRight();
                attachments.remove(optAttachment.get().getLeft());
                final var element = FormUtils.findElementByRowAndKey(form, rowId, key);
                form.getJournal().updateValue(element, rowId, element.getValue(), true);
                return true;
            } catch (Exception e) {
                throw ExceptionUtils.from(e);
            }
        }

        return false;
    }
}
