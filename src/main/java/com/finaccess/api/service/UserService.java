package com.finaccess.api.service;

import com.finaccess.api.DTO.UserResponse;
import com.finaccess.api.exception.ResourceNotFoundException;
import com.finaccess.api.model.Role;
import com.finaccess.api.model.User;
import com.finaccess.api.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //get all users
    public List<UserResponse> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    //get user by id
    public UserResponse getUserById(Long id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    //update role
    public UserResponse updateRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setRole(newRole);
        return UserResponse.from(userRepository.save(user));
    }

    //activate or deactivate
    public UserResponse updateStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(active);
        return UserResponse.from(userRepository.save(user));
    }

    //delete user
    public void deleteUser(Long id){
        User user = userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
}
