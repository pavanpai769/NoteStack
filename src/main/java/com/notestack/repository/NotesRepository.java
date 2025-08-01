package com.notestack.repository;

import com.notestack.entity.Note;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface NotesRepository extends MongoRepository<Note, ObjectId> {
    Optional<Note> findNotesById(ObjectId id);
}
