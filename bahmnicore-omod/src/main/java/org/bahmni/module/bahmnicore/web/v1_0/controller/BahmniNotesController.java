package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.service.NoteService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;


@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/note")
public class BahmniNotesController extends BaseRestController {


    @Autowired
    private NoteService noteService;

    @RequestMapping(method = RequestMethod.GET)
    public Note getNote(@RequestParam(value = "noteDate", required = true) Date noteDate, @RequestParam(value = "noteType", required = true) String noteType, @RequestParam("locationId") Integer locationId) throws Exception {
        return noteService.getNote(noteDate, noteType, locationId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "notes")
    public List<Note> getNotes(@RequestParam(value = "startDate", required = true) Date startDate,@RequestParam(value = "endDate", required = true) Date endDate, @RequestParam(value = "noteType", required = true) String noteType) throws Exception {
        return noteService.getNotes(startDate, endDate, noteType);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Note save(@RequestBody Note note) throws Exception {
        return noteService.createNote(note);
    }

    @RequestMapping(method = RequestMethod.POST, value = "notes")
    public List<Note> save(@RequestBody List<Note> notes) {
        return noteService.createNotes(notes);
    }

    /* how to identify update and create */
//    @RequestMapping(method = RequestMethod.POST)
//    @ResponseBody
//    public Note update(@RequestBody Note note) {
//        return noteService.updateNote(note);
//    }

//    /* void for multiple entries */
//    @RequestMapping(method = RequestMethod.POST)
//    @ResponseBody
//    public Note delete(@RequestBody Note note, @RequestParam(value = "reason") String reason ) {
//        return noteService.voidNote(note, reason);
//    }

}
