package com.tiago.helpdesk.api.service.impl;

import com.tiago.helpdesk.api.entity.User;
import com.tiago.helpdesk.api.repository.UserRepository;
import com.tiago.helpdesk.api.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository repository;
    
    @Override
    public User findByEmail(String email) {
        return this.repository.findByEmail(email);
    }

    @Override
    public User createOrUpdate(User user) {
        return this.repository.save(user);
    }

    @Override
    public User findById(String id) {
        return this.repository.findById(id).get();
    }

    @Override
    public void delete(String id) {
        this.repository.deleteById(id);
    }

    @Override
    public Page<User> findAll(int page, int count) {
        Pageable pages = PageRequest.of(page, count);
        return this.repository.findAll(pages);
    }
    
}
