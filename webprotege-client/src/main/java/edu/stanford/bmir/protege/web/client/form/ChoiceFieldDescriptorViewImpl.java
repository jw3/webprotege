package edu.stanford.bmir.protege.web.client.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RadioButton;
import edu.stanford.bmir.protege.web.client.editor.ValueListEditor;
import edu.stanford.bmir.protege.web.client.editor.ValueListFlexEditorImpl;
import edu.stanford.bmir.protege.web.client.ui.Counter;
import edu.stanford.bmir.protege.web.shared.form.field.ChoiceDescriptor;
import edu.stanford.bmir.protege.web.shared.form.field.ChoiceFieldType;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.List;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-18
 */
public class ChoiceFieldDescriptorViewImpl extends Composite implements ChoiceFieldDescriptorView {

    interface ChoiceFieldDescriptorViewImplUiBinder extends UiBinder<HTMLPanel, ChoiceFieldDescriptorViewImpl> {

    }

    private static ChoiceFieldDescriptorViewImplUiBinder ourUiBinder = GWT.create(ChoiceFieldDescriptorViewImplUiBinder.class);

    @UiField(provided = true)
    ValueListEditor<ChoiceDescriptor> choiceListEditor;

    @UiField
    RadioButton segmentedRadio;

    @UiField
    RadioButton radioRadio;

    @UiField
    RadioButton checkBoxRadio;

    @UiField(provided = true)
    static Counter counter = new Counter();

    @Inject
    public ChoiceFieldDescriptorViewImpl(Provider<ChoiceDescriptorPresenter> choiceDescriptorPresenterProvider) {
        choiceListEditor = new ValueListFlexEditorImpl<>(choiceDescriptorPresenterProvider::get);
        choiceListEditor.setEnabled(true);
        choiceListEditor.setNewRowMode(ValueListEditor.NewRowMode.MANUAL);
        counter.increment();
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Nonnull
    @Override
    public ChoiceFieldType getWidgetType() {
        if(checkBoxRadio.getValue()) {
            return ChoiceFieldType.RADIO_BUTTON;
        }
        else if(radioRadio.getValue()) {
            return ChoiceFieldType.RADIO_BUTTON;
        }
        else {
            return ChoiceFieldType.SEGMENTED_BUTTON;
        }
    }

    @Override
    public void setWidgetType(@Nonnull ChoiceFieldType widgetType) {
        if(widgetType == ChoiceFieldType.CHECK_BOX) {
            checkBoxRadio.setValue(true);
        }
        else if(widgetType == ChoiceFieldType.RADIO_BUTTON) {
            radioRadio.setValue(true);
        }
        else {
            segmentedRadio.setValue(true);
        }
    }

    @Override
    public void setChoiceDescriptors(@Nonnull List<ChoiceDescriptor> choiceDescriptors) {
        choiceListEditor.setValue(choiceDescriptors);
    }

    @Nonnull
    @Override
    public List<ChoiceDescriptor> getChoiceDescriptors() {
        return choiceListEditor.getEditorValue().orElse(Collections.emptyList());
    }
}