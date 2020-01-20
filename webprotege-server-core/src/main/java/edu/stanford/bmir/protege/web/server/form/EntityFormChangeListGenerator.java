package edu.stanford.bmir.protege.web.server.form;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.stanford.bmir.protege.web.server.change.*;
import edu.stanford.bmir.protege.web.server.frame.EmptyEntityFrameFactory;
import edu.stanford.bmir.protege.web.server.frame.FrameChangeGeneratorFactory;
import edu.stanford.bmir.protege.web.server.frame.FrameUpdate;
import edu.stanford.bmir.protege.web.server.msg.MessageFormatter;
import edu.stanford.bmir.protege.web.server.owlapi.RenameMap;
import edu.stanford.bmir.protege.web.server.renderer.RenderingManager;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.form.data.FormData;
import edu.stanford.bmir.protege.web.shared.form.data.FormEntitySubject;
import edu.stanford.bmir.protege.web.shared.form.data.FormIriSubject;
import edu.stanford.bmir.protege.web.shared.form.data.FormSubject;
import edu.stanford.bmir.protege.web.shared.frame.*;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-01
 */
public class EntityFormChangeListGenerator implements ChangeListGenerator<OWLEntityData> {

    @Nonnull
    private final EntityFormDataConverter entityFormDataConverter;

    @Nonnull
    private final MessageFormatter messageFormatter;

    @Nonnull
    private final FormData pristineFormData;

    @Nonnull
    private final FormData editedFormData;

    @Nonnull
    private final FrameChangeGeneratorFactory frameChangeGeneratorFactory;

    @Nonnull
    private final FormFrameConverter formFrameConverter;

    @Nonnull
    private final EmptyEntityFrameFactory emptyEntityFrameFactory;

    @Nonnull
    private final RenderingManager renderingManager;


    @Inject
    public EntityFormChangeListGenerator(@Nonnull FormData pristineFormData,
                                         @Nonnull FormData editedFormData,
                                         @Nonnull EntityFormDataConverter entityFormDataConverter,
                                         @Nonnull ReverseEngineeredChangeDescriptionGeneratorFactory reverseEngineeredChangeDescriptionGeneratorFactory,
                                         @Nonnull MessageFormatter messageFormatter,
                                         @Nonnull FrameChangeGeneratorFactory frameChangeGeneratorFactory,
                                         @Nonnull FormFrameConverter formFrameConverter,
                                         @Nonnull EmptyEntityFrameFactory emptyEntityFrameFactory,
                                         @Nonnull RenderingManager renderingManager) {
        this.pristineFormData = checkNotNull(pristineFormData);
        this.editedFormData = checkNotNull(editedFormData);
        this.entityFormDataConverter = checkNotNull(entityFormDataConverter);
        this.messageFormatter = checkNotNull(messageFormatter);
        this.frameChangeGeneratorFactory = checkNotNull(frameChangeGeneratorFactory);
        this.formFrameConverter = checkNotNull(formFrameConverter);
        this.emptyEntityFrameFactory = emptyEntityFrameFactory;
        this.renderingManager = renderingManager;
    }

    @Override
    public OntologyChangeList<OWLEntityData> generateChanges(ChangeGenerationContext context) {
        var pristineFormFrame = entityFormDataConverter.convert(pristineFormData);
        var editedFormFrame = entityFormDataConverter.convert(editedFormData);

        if(pristineFormFrame.equals(editedFormFrame)) {
            return emptyChangeList();
        }

        var pristineFramesBySubject = getFormFrameClosureBySubject(pristineFormFrame);
        var editedFramesBySubject = getFormFrameClosureBySubject(editedFormFrame);

        var changes = generateChangesForFormFrames(pristineFramesBySubject, editedFramesBySubject, context);
        if(changes.isEmpty()) {
            return emptyChangeList();
        }
        else {
            return combineIndividualChangeLists(changes);
        }
    }

    private OntologyChangeList<OWLEntityData> emptyChangeList() {
        var formSubject = pristineFormData.getSubject()
                        .orElseThrow();
        var entity = ((FormEntitySubject) formSubject).getEntity();
        var entityData = renderingManager.getRendering(entity);
        return OntologyChangeList.<OWLEntityData>builder().build(entityData);
    }

    /**
     * Combines a list of change lists
     * @param changes the list of changes lists
     * @return the combined list with the subject equal to the subject of the first change in the list
     */
    @Nonnull
    public OntologyChangeList<OWLEntityData> combineIndividualChangeLists(List<OntologyChangeList<OWLEntityData>> changes) {
        var firstChangeList = changes.get(0);
        var combinedChanges = changes.stream()
                                     .map(OntologyChangeList::getChanges)
                                     .flatMap(List::stream)
                                     .collect(toImmutableList());

        return OntologyChangeList.<OWLEntityData>builder()
                .addAll(combinedChanges)
                .build(firstChangeList.getResult());
    }

    private static ImmutableMap<OWLEntity, FormFrame> getFormFrameClosureBySubject(FormFrame formFrame) {
        // Important: ImmutableMap preserves iteration order
        var result = ImmutableMap.<OWLEntity, FormFrame>builder();
        List<FormFrame> framesToProcess = new ArrayList<>();
        framesToProcess.add(formFrame);
        while(!framesToProcess.isEmpty()) {
            var frame = framesToProcess.remove(0);
            frame.getSubject()
                 .accept(new FormSubject.FormDataSubjectVisitor() {
                     @Override
                     public void visit(@Nonnull FormEntitySubject formDataEntitySubject) {
                         result.put(formDataEntitySubject.getEntity(), frame);
                     }

                     @Override
                     public void visit(@Nonnull FormIriSubject formDataIriSubject) {

                     }
                 });
            framesToProcess.addAll(frame.getNestedFrames());
        }
        return result.build();
    }

    private List<OntologyChangeList<OWLEntityData>> generateChangesForFormFrames(ImmutableMap<OWLEntity, FormFrame> pristineFramesBySubject,
                                                                                 ImmutableMap<OWLEntity, FormFrame> editedFramesBySubject,
                                                                                 ChangeGenerationContext context) {

        var resultBuilder = ImmutableList.<OntologyChangeList<OWLEntityData>>builder();
        for(OWLEntity entity : pristineFramesBySubject.keySet()) {
            var pristineFrame = pristineFramesBySubject.get(entity);
            var editedFrame = editedFramesBySubject.get(entity);

            var pristineEntityFrame = formFrameConverter.toEntityFrame(pristineFrame)
                                                        .orElseThrow();

            if(editedFrame == null) {
                // Deleted
                var emptyEditedFrame = emptyEntityFrameFactory.getEmptyEntityFrame(entity);
                var changes = generateChangeListForFrames(pristineEntityFrame, emptyEditedFrame, context);
                resultBuilder.add(changes);
            }
            else {
                // Edited, possibly
                var editedEntityFrame = formFrameConverter.toEntityFrame(editedFrame)
                                                          .orElseThrow();
                var changes = generateChangeListForFrames(pristineEntityFrame, editedEntityFrame, context);
                resultBuilder.add(changes);
            }
        }

        for(OWLEntity entity : editedFramesBySubject.keySet()) {
            var pristineFrame = pristineFramesBySubject.get(entity);
            if(pristineFrame == null) {
                // Added
                var emptyPristineFrame = emptyEntityFrameFactory.getEmptyEntityFrame(entity);
                var addedFormFrame = editedFramesBySubject.get(entity);
                var addedEntityFrame = formFrameConverter.toEntityFrame(addedFormFrame)
                                                         .orElseThrow();
                var changes = generateChangeListForFrames(emptyPristineFrame, addedEntityFrame, context);
                resultBuilder.add(changes);
            }
        }
        return resultBuilder.build();
    }


    private OntologyChangeList<OWLEntityData> generateChangeListForFrames(EntityFrame pristineFrame,
                                                                          EntityFrame editedFrame,
                                                                          ChangeGenerationContext context) {
        var frameUpdate = FrameUpdate.get(pristineFrame, editedFrame);
        var changeGeneratorFactory = frameChangeGeneratorFactory.create(frameUpdate);

        return changeGeneratorFactory.generateChanges(context);
    }

    @Nonnull
    @Override
    public String getMessage(ChangeApplicationResult<OWLEntityData> result) {
        Optional<OWLEntity> owlEntity = result.getSubject()
                                              .asEntity();
        return owlEntity.map(entity -> messageFormatter.format("Edited {0}",
                                                               entity)).orElse("Edited entity");
    }

    @Override
    public OWLEntityData getRenamedResult(OWLEntityData result, RenameMap renameMap) {
        return result;
    }
}
