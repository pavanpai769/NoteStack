package com.notestack.service;

import com.notestack.entity.User;
import com.notestack.exceptions.UserAlreadyExistsException;
import com.notestack.exceptions.UserNotFoundException;
import com.notestack.repository.UserRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotesService notesService;

    public final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveUser( @NonNull User user){
        if(user.getUsername().trim().isEmpty()){
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if(userRepository.existsByUsername(user.getUsername())){
            throw new UserAlreadyExistsException("user with username "+user.getUsername()+" already exists");
        }
        user.setRoles(List.of("USER"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void createAdmin(User user){
        if(user== null || user.getUsername().trim().isEmpty()){
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if(userRepository.existsByUsername(user.getUsername())){
            throw new UserAlreadyExistsException("user with username "+user.getUsername()+" already exists");
        }
        user.setRoles(List.of("ADMIN","USER"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void makeAdmin(String username){
        if(!userRepository.existsByUsername(username)){
            throw new UserNotFoundException("user with username "+username+" not found");
        }
        User user = getUserByUsername(username);
        if(user.getRoles().contains("ADMIN")){
            throw new UserAlreadyExistsException("user with username "+username+" is already admin");
        }
        user.getRoles().add("ADMIN");
        userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserByUsername(String userName) throws UserNotFoundException {
        return userRepository.findByUsername(userName).orElseThrow(()->
                new UserNotFoundException("user with username "+userName+" not found"));
    }

    public User deleteUserByUserName(String userName){
        User user = getUserByUsername(userName);
        if(user.getNotesList() != null) {
            user.getNotesList().forEach(note -> notesService.deleteNoteByUserName(user.getUsername(), note.getId()));
        }
        userRepository.delete(user);
        return user;
    }

    public void updateUserByUserName(String userName, User user)  throws UserNotFoundException{
        User userInDb = getUserByUsername(userName);
        userInDb.setUsername(user.getUsername());
        userInDb.setPassword(passwordEncoder.encode(user.getPassword()));
        userInDb.setNotesList(user.getNotesList());
        userRepository.save(userInDb);
    }

}
