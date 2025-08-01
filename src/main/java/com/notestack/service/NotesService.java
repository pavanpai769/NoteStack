package com.notestack.service;

import com.notestack.entity.Note;
import com.notestack.entity.User;
import com.notestack.exceptions.NoteNotFoundException;
import com.notestack.repository.NotesRepository;
import com.notestack.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotesService {

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    public void saveNote(String userName,Note note) {

            User userInDb = userService.getUserByUsername(userName);
            Note noteInDb = notesRepository.save(note);

            if(userInDb.getNotesList() ==null){
                userInDb.setNotesList(new ArrayList<>());
            }
            userInDb.getNotesList().add(noteInDb);
            userRepository.save(userInDb);
    }

    public List<Note> getNotesByUserName(String userName) {
        User userInDb = userService.getUserByUsername(userName);
        return userInDb.getNotesList();

    }

    public Note deleteNoteByUserName(String username, ObjectId noteId) {

        User user = userService.getUserByUsername(username);
        List<Note> notesList = user.getNotesList();
        Note needToDelete = notesList.stream()
                            .filter(noteInList -> noteInList.getId().equals(noteId))
                            .findFirst()
                            .orElseThrow(()->new NoteNotFoundException("No such note found for user "+ username));

        notesRepository.delete(needToDelete);
        notesList.remove(needToDelete);
        userRepository.save(user);
        return needToDelete;

    }

    public void updateNote(String username,Note note, ObjectId noteId) {

        User user = userService.getUserByUsername(username);
        List<Note> notesList = user.getNotesList();

        Note needToUpdate = notesList.stream()
                            .filter(noteInDb-> noteInDb.getId().equals(noteId))
                            .findFirst()
                            .orElseThrow( ()-> new NoteNotFoundException("No such note found for user "+ username));

        needToUpdate.setContent(note.getContent());
        needToUpdate.setTitle(note.getTitle());
        notesRepository.save(needToUpdate);
    }

}
