package com.tiago.helpdesk.api.service.impl;

import java.util.Optional;

import com.tiago.helpdesk.api.entity.ChangeStatus;
import com.tiago.helpdesk.api.entity.Ticket;
import com.tiago.helpdesk.api.repository.ChangeStatusRepository;
import com.tiago.helpdesk.api.repository.TicketRepository;
import com.tiago.helpdesk.api.service.TicketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class TicketServiceImpl implements TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ChangeStatusRepository changeStatusRepository;

    @Override
    public Ticket createOrUpdate(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    public Optional<Ticket> findById(String id) {
        return ticketRepository.findById(id);
    }

    @Override
    public void delete(String id) {
        ticketRepository.deleteById(id);
    }

    @Override
    public Page<Ticket> listTicket(int page, int count) {
        PageRequest pages = PageRequest.of(page, count);
        return ticketRepository.findAll(pages);
    }

    @Override
    public ChangeStatus createChangeStatus(ChangeStatus changeStatus) {
        return changeStatusRepository.save(changeStatus);
    }

    @Override
    public Iterable<ChangeStatus> listChangeStatus(String ticketId) {
        return changeStatusRepository.findByTicketIdOrderByDateChangeStatusDesc(ticketId);
    }

    @Override
    public Page<Ticket> findByCurrentUser(int page, int count, String userId) {
        PageRequest pages = PageRequest.of(page, count);
        return ticketRepository.findByUserIdOrderByDateDesc(pages, userId);
    }

    @Override
    public Page<Ticket> findByParameters(int page, int count, String title, String status, String priority) {
        PageRequest pages = PageRequest.of(page, count);
        return ticketRepository.findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingOrderByDateDesc(title, status,
                priority, pages);
    }

    @Override
    public Page<Ticket> findByParametersAndCurrentUser(int page, int count, String title, String status,
            String priority, String userId) {
        PageRequest pages = PageRequest.of(page, count);
        return ticketRepository.findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingAndUserIdOrderByDateDesc(title,
                status, priority, userId, pages);
    }

    @Override
    public Page<Ticket> findByNumber(int page, int count, Integer number) {
        PageRequest pages = PageRequest.of(page, count);
        return ticketRepository.findByNumber(number, pages);
    }

    @Override
    public Iterable<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    public Page<Ticket> findByParametersAndAssignedUser(int page, int count, String title, String status,
            String priority, String assignedUser) {
        PageRequest pages = PageRequest.of(page, count);
        return ticketRepository.findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingAndAssignedUserOrderByDateDesc(title,
                status, priority, assignedUser, pages);
    }

}