package com.notestack.controller;

import com.notestack.entity.Note;
import com.notestack.exceptions.NoteNotFoundException;
import com.notestack.exceptions.UserNotFoundException;
import com.notestack.service.NotesService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class NotesController {

    @Autowired
    private NotesService notesService;

    @PostMapping
    private ResponseEntity<?> addNote( @RequestBody Note note) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try{
            notesService.saveNote(username,note);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getNotesByUserName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try{
           List<Note> noteList= notesService.getNotesByUserName(username);

           if( noteList== null || noteList.isEmpty()){
               return ResponseEntity.noContent().build();
           }
           return ResponseEntity.ok(noteList);
        } catch (UserNotFoundException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNoteByUserName(@PathVariable ObjectId noteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            Note deletedNote = notesService.deleteNoteByUserName(username,noteId);
            return new ResponseEntity<>(deletedNote,HttpStatus.OK);
        } catch(UserNotFoundException | NoteNotFoundException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        } catch(Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote(@PathVariable ObjectId noteId, @RequestBody Note note){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try{
            notesService.updateNote(username,note,noteId);
            return ResponseEntity.ok().build();
        }catch (UserNotFoundException | NoteNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
