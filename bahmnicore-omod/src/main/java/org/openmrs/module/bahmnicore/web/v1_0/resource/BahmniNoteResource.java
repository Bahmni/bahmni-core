//package org.openmrs.module.bahmnicore.web.v1_0.resource;
//
//import lombok.SneakyThrows;
//import org.bahmni.module.bahmnicore.model.Note;
//import org.bahmni.module.bahmnicore.service.NoteService;
//import org.openmrs.api.context.Context;
//import org.openmrs.module.webservices.rest.web.RequestContext;
//import org.openmrs.module.webservices.rest.web.RestConstants;
//import org.openmrs.module.webservices.rest.web.annotation.Resource;
//import org.openmrs.module.webservices.rest.web.representation.*;
//import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
//import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
//import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
//import org.openmrs.module.webservices.rest.web.response.ResponseException;
//
//import java.util.Collection;
//import java.util.Date;
//
//
//@Resource(name = RestConstants.VERSION_1 + "/note", supportedClass = Note.class, supportedOpenmrsVersions = {"2.5.* - 2.*"}, order = 0)
//public class BahmniNoteResource extends DelegatingCrudResource<Note> {
//    @Override
//    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
//        DelegatingResourceDescription description = new DelegatingResourceDescription();
//        description.addProperty("uuid");
//        description.addProperty("noteId");
//        description.addProperty("text");
//        description.addProperty("priority");
//        description.addProperty("noteType");
//        description.addProperty("noteDate");
//        description.addProperty("locationId");
//        description.addSelfLink();
//        if (rep instanceof DefaultRepresentation) {
//            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
//            return description;
//        } else if (rep instanceof FullRepresentation) {
//            description.addProperty("dateChanged");
//            description.addProperty("dateCreated");
//            description.addProperty("patient", Representation.DEFAULT);
//            return description;
//        }
//        return null;
//    }
//
//    @SneakyThrows
//    @Override
//    public Note getByUniqueId(String id) {
//        return Context.getService(NoteService.class).getNote(Integer.valueOf(id));
//    }
//
//
//    @Override
//    protected void delete(Note delegate, String reason, RequestContext requestContext) throws ResponseException {
//        if (delegate.getVoided()) {
//            return;
//        }
//        Context.getService(NoteService.class).voidNote(delegate,reason);
//    }
//
//
//    @Override
//    public Note newDelegate() {
//        return new Note();
//    }
//
//    @SneakyThrows
//    @Override
//    public Note save(Note delegate) {
//        Context.getService(NoteService.class).createNote(delegate);
//        return delegate;
//    }
//
//    @Override
//    public void purge(Note notes, RequestContext requestContext) throws ResponseException {
//        throw new ResourceDoesNotSupportOperationException();
//    }
//
////    public Note getNote(Date noteDate, String noteType, Integer locationId) throws Exception {
////        return Context.getService(NoteService.class).getNote(noteDate, noteType, locationId );
////
////    }
////
////    //list of notes for a week
////
////    public Collection<Note> getNote(Date noteDate, String noteType, Integer locationId) throws Exception {
////        return Context.getService(NoteService.class).getNote(noteDate, noteType, locationId );
////
////    }
//
//}
