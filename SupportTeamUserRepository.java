package com.helpdesk.repository;

import com.helpdesk.entity.SupportTeamUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTeamUserRepository extends JpaRepository<SupportTeamUser, Long> {

    List<SupportTeamUser> findByIsActiveTrue();

    Optional<SupportTeamUser> findByUsername(String username);

    boolean existsByUsername(String username);
}

