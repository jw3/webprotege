package edu.stanford.bmir.protege.web.server.crud.uuid;

import edu.stanford.bmir.protege.web.server.crud.ChangeSetEntityCrudSession;
import edu.stanford.bmir.protege.web.server.crud.EntityCrudKitPlugin;
import edu.stanford.bmir.protege.web.shared.crud.EntityCrudKit;
import edu.stanford.bmir.protege.web.shared.crud.EntityCrudKitPrefixSettings;
import edu.stanford.bmir.protege.web.shared.crud.EntityCrudKitSettings;
import edu.stanford.bmir.protege.web.shared.crud.uuid.UUIDSuffixKit;
import edu.stanford.bmir.protege.web.shared.crud.uuid.UUIDSuffixSettings;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public class UUIDEntityCrudKitPlugin implements EntityCrudKitPlugin<UUIDEntityCrudKitHandler, UUIDSuffixSettings, ChangeSetEntityCrudSession> {

    @Nonnull
    private UUIDSuffixKit kit;

    @Nonnull
    private final UUIDEntityCrudKitHandlerFactory factory;

    @Inject
    public UUIDEntityCrudKitPlugin(@Nonnull UUIDSuffixKit kit,
                                   @Nonnull UUIDEntityCrudKitHandlerFactory factory) {
        this.kit = checkNotNull(kit);
        this.factory = checkNotNull(factory);
    }

    @Override
    public EntityCrudKit<UUIDSuffixSettings> getEntityCrudKit() {
        return kit;
    }

    @Override
    public UUIDEntityCrudKitHandler getEntityCrudKitHandler() {
        return factory.create(new EntityCrudKitPrefixSettings(), new UUIDSuffixSettings());
    }

    @Override
    public UUIDSuffixSettings getDefaultSettings() {
        return new UUIDSuffixSettings();
    }

    @Override
    public UUIDEntityCrudKitHandler getEntityCrudKitHandler(EntityCrudKitSettings<UUIDSuffixSettings> settings) {
        return factory.create(settings.getPrefixSettings(), settings.getSuffixSettings());
    }
}

