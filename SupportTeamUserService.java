package com.helpdesk.service;

import com.helpdesk.entity.SupportTeamUser;
import com.helpdesk.repository.SupportTeamUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupportTeamUserService {

    @Autowired
    private SupportTeamUserRepository supportTeamUserRepository;

    public SupportTeamUser createUser(SupportTeamUser user) {
        if (supportTeamUserRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("User with username '" + user.getUsername() + "' already exists");
        }
        return supportTeamUserRepository.save(user);
    }

    public Optional<SupportTeamUser> getUserById(Long id) {
        return supportTeamUserRepository.findById(id);
    }

    public Optional<SupportTeamUser> getUserByUsername(String username) {
        return supportTeamUserRepository.findByUsername(username);
    }

    public List<SupportTeamUser> getAllUsers() {
        return supportTeamUserRepository.findAll();
    }

    public List<SupportTeamUser> getActiveUsers() {
        return supportTeamUserRepository.findByIsActiveTrue();
    }

    public SupportTeamUser updateUser(SupportTeamUser user) {
        return supportTeamUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        Optional<SupportTeamUser> userOpt = supportTeamUserRepository.findById(id);
        if (userOpt.isPresent()) {
            SupportTeamUser user = userOpt.get();
            user.setIsActive(false);
            supportTeamUserRepository.save(user);
        }
    }

    public boolean isAuthorized(String username) {
        return supportTeamUserRepository.findByUsername(username).isPresent();
    }
}

