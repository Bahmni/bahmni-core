package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.contract.NoteRequestResponse;
import org.bahmni.module.bahmnicore.mapper.NoteMapper;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.service.NoteService;
import org.bahmni.module.bahmnicore.validator.NoteValidator;
import org.openmrs.api.context.Context;
import org.openmrs.module.auditlog.util.DateUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/notes")
public class BahmniNotesController extends BaseRestController {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private NoteValidator noteValidator;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<NoteRequestResponse> getNotes(@RequestParam(value = "noteStartDate") String noteStartDateString, @RequestParam(value = "noteEndDate", required = false) String noteEndDateString,
                                       @RequestParam(value = "noteType") String noteType) throws Exception {
        Date noteStartDate = DateUtil.convertToLocalDateFromUTC(noteStartDateString);
        if (noteEndDateString != null) {
            Date noteEndDate = DateUtil.convertToLocalDateFromUTC(noteEndDateString);
            List<Note> notes = Context.getService(NoteService.class).getNotes(noteStartDate, noteEndDate, noteType);
            return notes.stream().map(note -> noteMapper.mapResponse(note)).collect(Collectors.toList());
        }

        Note note = Context.getService(NoteService.class).getNote(noteStartDate, noteType);
        List<NoteRequestResponse> noteResponses = new ArrayList<>();
        if (note != null) {
            noteResponses.add(noteMapper.mapResponse(note));
        }
        return noteResponses;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public List<NoteRequestResponse> save(@Valid @RequestBody List<NoteRequestResponse> noteRequests) throws Exception {
        List<Note> notes = new ArrayList<>();
        notes = noteRequests.stream().map(noteRequest -> {
            Errors noteRequestErrors = new BeanPropertyBindingResult(noteRequest, "noteRequest");
            noteValidator.validate(noteRequest, noteRequestErrors);
            if (!noteRequestErrors.getAllErrors().isEmpty()) {
                throw new RuntimeException(noteRequestErrors.getAllErrors().get(0).toString());
            }
            return noteMapper.mapRequest(noteRequest);
        }).collect(Collectors.toList());
        List<Note> listOfNotes = Context.getService(NoteService.class).createNotes(notes);
        return listOfNotes.stream().map(note -> noteMapper.mapResponse(note)).collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}")
    @ResponseBody
    public NoteRequestResponse update(@Valid @PathVariable("id") String id, @RequestBody NoteRequestResponse noteRequestResponse) {
        Integer noteId = Integer.valueOf(id);
        return noteMapper.mapResponse(Context.getService(NoteService.class).updateNote(noteId, noteRequestResponse));
    }

    @RequestMapping(method = RequestMethod.DELETE,  value = "/{id}")
    @ResponseBody
    public NoteRequestResponse delete(@PathVariable("id") String id, @RequestParam(value = "reason", required = false) String reason ) {
        Integer noteId = Integer.valueOf(id);
        return noteMapper.mapResponse(Context.getService(NoteService.class).voidNote(noteId, reason));
    }


}
