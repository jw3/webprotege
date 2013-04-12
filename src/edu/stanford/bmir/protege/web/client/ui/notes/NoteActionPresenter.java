package edu.stanford.bmir.protege.web.client.ui.notes;

import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.widgets.MessageBox;
import edu.stanford.bmir.protege.web.shared.notes.DiscussionThread;
import edu.stanford.bmir.protege.web.shared.notes.NoteId;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 09/04/2013
 */
public class NoteActionPresenter {

    private NoteActionView view;

    private NoteId currentNoteId;

    public NoteActionPresenter(NoteActionView noteActionView) {
        this.view = noteActionView;
        view.setReplyToNoteHandler(new ReplyToNoteHandler() {
            @Override
            public void handleReplyToNote() {
                MessageBox.alert("Reply to note " + currentNoteId);
            }
        });
        view.setDeleteNoteHandler(new DeleteNoteHandler() {
            @Override
            public void handleDeleteNote() {
                MessageBox.alert("Delete note " + currentNoteId);
            }
        });
    }

    public void setNoteId(NoteId noteId, DiscussionThread context) {
        this.currentNoteId = noteId;
        view.setCanDelete(!context.hasReplies(noteId));
    }

    public NoteActionView getView() {
        return view;
    }

    public Widget getWidget() {
        return view.getWidget();
    }
}
