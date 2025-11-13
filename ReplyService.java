package com.helpdesk.service;

import com.helpdesk.entity.Reply;
import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.User;
import com.helpdesk.repository.ReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReplyService {

    @Autowired
    private ReplyRepository replyRepository;

    // Create a new reply
    public Reply createReply(Reply reply) {
        return replyRepository.save(reply);
    }

    // Create reply with ticket and user
    public Reply createReply(String content, Ticket ticket, User user) {
        Reply reply = new Reply(content, ticket, user);
        return replyRepository.save(reply);
    }

    // Get reply by ID
    public Optional<Reply> getReplyById(Long id) {
        return replyRepository.findById(id);
    }

    // Get all replies for a ticket
    public List<Reply> getRepliesByTicket(Long ticketId) {
        return replyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    // Get all replies by a user
    public List<Reply> getRepliesByUser(Long userId) {
        return replyRepository.findByUserId(userId);
    }

    // Count replies for a ticket
    public long countRepliesByTicket(Long ticketId) {
        return replyRepository.countByTicketId(ticketId);
    }

    // Update reply
    public Reply updateReply(Reply reply) {
        return replyRepository.save(reply);
    }

    // Delete reply
    public void deleteReply(Long id) {
        replyRepository.deleteById(id);
    }

    // Get all replies
    public List<Reply> getAllReplies() {
        return replyRepository.findAll();
    }
}

