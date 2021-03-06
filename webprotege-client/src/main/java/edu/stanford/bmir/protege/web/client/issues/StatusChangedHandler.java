package edu.stanford.bmir.protege.web.client.issues;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Oct 2016
 */
@FunctionalInterface
public interface StatusChangedHandler {

    void handleStatusChanged();
}
