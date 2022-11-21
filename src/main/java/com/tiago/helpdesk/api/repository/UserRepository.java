package com.tiago.helpdesk.api.repository;

import com.tiago.helpdesk.api.entity.User;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String>{
    User findByEmail(String email);
}
