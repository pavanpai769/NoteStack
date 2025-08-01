package com.notestack.repository;

import com.notestack.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByUsername(String userName);
    boolean  existsByUsername(String  userName);
}
