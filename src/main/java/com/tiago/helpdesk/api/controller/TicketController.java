package com.tiago.helpdesk.api.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.tiago.helpdesk.api.dto.Summary;
import com.tiago.helpdesk.api.entity.ChangeStatus;
import com.tiago.helpdesk.api.entity.Ticket;
import com.tiago.helpdesk.api.entity.User;
import com.tiago.helpdesk.api.enums.ProfileEnum;
import com.tiago.helpdesk.api.enums.StatusEnum;
import com.tiago.helpdesk.api.response.Response;
import com.tiago.helpdesk.api.security.jwt.JwtTokenUtil;
import com.tiago.helpdesk.api.service.TicketService;
import com.tiago.helpdesk.api.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ticket")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @PostMapping()
   // @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Response<Ticket>> createOrUpdate(HttpServletRequest req, @RequestBody Ticket ticket,
            BindingResult result) {
        Response<Ticket> response = new Response<Ticket>();
        try {
            validateCreateTicket(ticket, result);
            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(response);
            }
            ticket.setStatus(StatusEnum.New);
            ticket.setUser(userFromRequest(req));
            ticket.setDate(new Date());
            ticket.setNumber(generateNumber());
            Ticket tickedPersisted = (Ticket) ticketService.createOrUpdate(ticket);
            response.setData(tickedPersisted);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    private Integer generateNumber() {
        return new Random().nextInt(9999);
    }

    private User userFromRequest(HttpServletRequest req) {
        String token = req.getHeader(HttpHeaders.AUTHORIZATION);
        String email = jwtTokenUtil.getUsernameFromToken(token);
        return userService.findByEmail(email);
    }

    private void validateCreateTicket(Ticket ticket, BindingResult result) {
        if (ticket.getTitle() == null) {
            result.addError(new ObjectError("Ticket", "Title not informed"));
        }
    }

    @PutMapping()
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Response<Ticket>> update(HttpServletRequest req, @RequestBody Ticket ticket,
            BindingResult result) {
        Response<Ticket> response = new Response<Ticket>();
        try {
            validateUpdateTicket(ticket, result);
            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(response);
            }

            Ticket currentTicket = ticketService.findById(ticket.getId()).get();
            ticket.setStatus(currentTicket.getStatus());
            ticket.setUser(currentTicket.getUser());
            ticket.setDate(currentTicket.getDate());
            ticket.setNumber(currentTicket.getNumber());
            if (currentTicket.getAssignedUser() != null) {
                ticket.setAssignedUser(currentTicket.getAssignedUser());
            }

            Ticket ticketPersisted = this.ticketService.createOrUpdate(ticket);
            response.setData(ticketPersisted);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
    public ResponseEntity<Response<Ticket>> findById(@PathVariable("id") String id) {
        Response<Ticket> response = new Response<Ticket>();
        Optional<Ticket> ticket = ticketService.findById(id);
        if (!ticket.isPresent()) {
            response.getErrors().add("register not found id:" + id);
            return ResponseEntity.badRequest().body(response);
        }

        List<ChangeStatus> changes = new ArrayList<ChangeStatus>();
        Iterable<ChangeStatus> changesCurrent = ticketService.listChangeStatus(ticket.get().getId());

        for (Iterator<ChangeStatus> iterator = changesCurrent.iterator(); iterator.hasNext();) {
            ChangeStatus changeStatus = (ChangeStatus) iterator.next();
            changeStatus.setTicket(null);
            changes.add(changeStatus);
        }
        ticket.get().setChanges(changes);
        response.setData(ticket.get());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Response<String>> delete(@PathVariable("id") String id) {
        Response<String> response = new Response<String>();
        Optional<Ticket> ticket = ticketService.findById(id);
        if (!ticket.isPresent()) {
            response.getErrors().add("register not found id:" + id);
            return ResponseEntity.badRequest().body(response);
        }
        ticketService.delete(id);
        return ResponseEntity.ok(response);

    }

    private void validateUpdateTicket(Ticket ticket, BindingResult result) {
        if (ticket.getTitle() == null) {
            result.addError(new ObjectError("Ticket", "Title not informed"));
        }
        if (ticket.getId() == null) {
            result.addError(new ObjectError("Ticket", "Id not informed"));
        }
    }

    @GetMapping(value = "{page}/{count}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
    public ResponseEntity<Response<Page<Ticket>>> findAll(HttpServletRequest request, @PathVariable("page") int page,
            @PathVariable("count") int count) {
        Response<Page<Ticket>> response = new Response<Page<Ticket>>();
        Page<Ticket> tickets = null;

        User userRequest = userFromRequest(request);
        if (userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
            tickets = ticketService.listTicket(page, count);
        } else if (userRequest.getProfile().equals(ProfileEnum.ROLE_CUSTOMER)) {
            tickets = ticketService.findByCurrentUser(page, count, userRequest.getId());
        }
        response.setData(tickets);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "{page}/{count}/{number}/{title}/{status}/{priority}/{assigned}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
    public ResponseEntity<Response<Page<Ticket>>> findByParams(HttpServletRequest request,
            @PathVariable("page") int page, @PathVariable("count") int count, @PathVariable("number") Integer number,
            @PathVariable("title") String title, @PathVariable("status") String status,
            @PathVariable("priority") String priority, @PathVariable("assigned") boolean assigned) {

        title = title.equals("uninformed") ? "" : title;
        status = status.equals("uninformed") ? "" : status;
        priority = priority.equals("uninformed") ? "" : priority;
        Response<Page<Ticket>> response = new Response<Page<Ticket>>();
        Page<Ticket> tickets = null;
        if (number > 0) {
            tickets = ticketService.findByNumber(page, count, number);
        } else {
            User userRequest = userFromRequest(request);
            if (userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
                if (assigned) {
                    tickets = ticketService.findByParametersAndAssignedUser(page, count, title, status, priority,
                            userRequest.getId());
                } else {
                    tickets = ticketService.findByParameters(page, count, title, status, priority);
                }
            } else if (userRequest.getProfile().equals(ProfileEnum.ROLE_CUSTOMER)) {
                tickets = ticketService.findByParametersAndCurrentUser(page, count, title, status, priority,
                        userRequest.getId());
            }

        }

        response.setData(tickets);

        return ResponseEntity.ok(response);
    }

    @PutMapping("{id}/{status}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
    public ResponseEntity<Response<Ticket>> changeStatus(@PathVariable("id") String id,
            @PathVariable("status") String status, HttpServletRequest req, @RequestBody Ticket ticket,
            BindingResult result) {
        Response<Ticket> response = new Response<Ticket>();
        try {
            validateChangeStatus(id, status, result);
            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(response);
            }
            Ticket currentTicket = ticketService.findById(id).get();
            currentTicket.setStatus(StatusEnum.valueOf(status));

            if (status.equals("Assigned")) {
                currentTicket.setAssignedUser(userFromRequest(req));
            }
            Ticket ticketPersisted = ticketService.createOrUpdate(currentTicket);
            ChangeStatus changeStatus = new ChangeStatus();
            changeStatus.setUserChange(userFromRequest(req));
            changeStatus.setDateChangeStatus(new Date());
            changeStatus.setStatus(StatusEnum.valueOf(status));
            changeStatus.setTicket(ticketPersisted);
            ticketService.createChangeStatus(changeStatus);
            response.setData(ticketPersisted);
        } catch (Exception ex) {
            response.getErrors().add(ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    private void validateChangeStatus(String id, String status, BindingResult result) {
        if (!StringUtils.hasText(id)) {
            result.addError(new ObjectError("Ticket", "id not informed"));
        }
        if (!StringUtils.hasText(status)) {
            result.addError(new ObjectError("Ticket", "status not informed"));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Response<Summary>> findSummary() {
        Response<Summary> response = new Response<Summary>();
        Summary summary = new Summary();
        int amountNew = 0;
        int amountResolved = 0;
        int amountApproved = 0;
        int amountDisapproved = 0;
        int amountAssigned = 0;
        int amountClosed = 0;

        Iterable<Ticket> tickets = ticketService.findAll();

        if (tickets != null) {
            for (Ticket ticket : tickets) {
                switch (ticket.getStatus()) {

                case New:
                    amountNew++;
                    break;
                case Resolved:
                    amountResolved++;
                    break;
                case Approved:
                    amountApproved++;
                    break;
                case Disapproved:
                    amountDisapproved++;
                    break;
                case Assigned:
                    amountAssigned++;
                    break;
                case Closed:
                    amountClosed++;
                    break;
                }
            }
        }
        summary.setAmountNew(amountNew);
        summary.setAmountApproved(amountApproved);
        summary.setAmountAssigned(amountAssigned);
        summary.setAmountClosed(amountClosed);
        summary.setAmountDisapproved(amountDisapproved);
        summary.setAmountResolved(amountResolved);
        response.setData(summary);
        return ResponseEntity.ok(response);

    }
}