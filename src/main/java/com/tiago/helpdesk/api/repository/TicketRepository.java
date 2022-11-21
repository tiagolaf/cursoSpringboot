package com.tiago.helpdesk.api.repository;

import com.tiago.helpdesk.api.entity.Ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketRepository extends MongoRepository<Ticket, String> {
    Page<Ticket> findByUserIdOrderByDateDesc(Pageable page, String userId);

    Page<Ticket> findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingOrderByDateDesc(
            String title, String status, String priority, Pageable pages);

    Page<Ticket> findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingAndUserIdOrderByDateDesc(
            String title, String status, String priority, String userId, Pageable pages);

    Page<Ticket> findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingAndAssignedUserOrderByDateDesc(
            String title, String status, String priority, String userId, Pageable pages);

    Page<Ticket> findByNumber(Integer number, Pageable pages);
}
