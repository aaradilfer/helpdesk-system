package com.helpdesk.repository;

import com.helpdesk.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    
    // Find all replies for a specific ticket, ordered by creation date
    List<Reply> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
    
    // Find all replies by a specific user
    List<Reply> findByUserId(Long userId);
    
    // Count replies for a specific ticket
    long countByTicketId(Long ticketId);
}

