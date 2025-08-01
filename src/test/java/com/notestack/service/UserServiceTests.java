package com.notestack.service;

import com.notestack.entity.Note;
import com.notestack.entity.User;
import com.notestack.exceptions.UserAlreadyExistsException;
import com.notestack.exceptions.UserNotFoundException;
import com.notestack.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotesService notesService;

    @InjectMocks
    private UserService userService;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhileSavingUserWithEmptyUsername() {

        User userWithEmptyUsername = User.builder().username("   ").password("1234").build();
        Exception userNameEmptyException = assertThrows(IllegalArgumentException.class, () -> {
            userService.saveUser(userWithEmptyUsername);
        });

        assertEquals("Username cannot be empty", userNameEmptyException.getMessage());

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(userWithEmptyUsername);
    }

    @Test
    public void shouldSaveUserForTheFirstTime() {

        User user = User.builder().username("ram").password("1234").build();

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);

        userService.saveUser(user);

        assertTrue(user.getRoles().contains("USER"));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        assertTrue(passwordEncoder.matches("1234", user.getPassword()));

        verify(userRepository, times(1)).existsByUsername(user.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void shouldThrowUserAlreadyExistsExceptionWhenUsernameIsAlreadyExists() {
        User user = User.builder().username("ram").password("1234").build();

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        Exception e = assertThrows(UserAlreadyExistsException.class, ()->{
            userService.saveUser(user);
        });

        assertEquals("user with username "+user.getUsername()+" already exists", e.getMessage());

        verify(userRepository, times(1)).existsByUsername(user.getUsername());
        verify(userRepository, never()).save(user);
    }

    @Test
    public void shouldReturnUserWhenUserExists() {
        User user = User.builder().username("ram").password("1234").build();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User userInDb = userService.getUserByUsername(user.getUsername());
        assertEquals(userInDb, user);

        verify(userRepository, times(1)).findByUsername(user.getUsername());
    }

    @Test
    public void shouldThrowUserNotFoundExceptionWhenUsernameIsNotExists() {
        User user = User.builder().username("ram").password("1234").build();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        Exception userNotFoundException = assertThrows(UserNotFoundException.class, ()->{
            userService.getUserByUsername(user.getUsername());
        });

        assertEquals("user with username "+ user.getUsername() +" not found", userNotFoundException.getMessage());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
    }

    @Test
    public void deleteUserByUsernameWithNotes(){
        ObjectId note1Id = new ObjectId();
        ObjectId note2Id = new ObjectId();

        Note note1 = Note.builder().title("Sample Note").content("Sample Content").id(note1Id).build();
        Note note2 = Note.builder().title("Another sample Note").content("Sample Content").id(note2Id).build();

        User user = User.builder().username("ram").password("1234").notesList(List.of(note1,note2)).build();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User userInDb =  userService.deleteUserByUserName(user.getUsername());
        assertEquals(userInDb, user);

        verify(notesService).deleteNoteByUserName(user.getUsername(), note1Id);
        verify(notesService).deleteNoteByUserName(user.getUsername(), note2Id);
        verify(userRepository).delete(user);
    }

    @Test
    public void deleteUserByUsernameWithoutNotes(){
        User  user = User.builder().username("ram").password("1234").build();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        User userInDb =  userService.deleteUserByUserName(user.getUsername());
        assertEquals(userInDb, user);

        verify(notesService, never()).deleteNoteByUserName(anyString(), any(ObjectId.class));
    }

}
