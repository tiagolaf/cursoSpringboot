package com.tiago.helpdesk.api.repository;

import com.tiago.helpdesk.api.entity.ChangeStatus;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChangeStatusRepository extends MongoRepository<ChangeStatus, String> {
 
    Iterable<ChangeStatus> findByTicketIdOrderByDateChangeStatusDesc(String ticketId);
}
