package com.example.capstone.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.capstone.User;

public interface UserRepository extends MongoRepository<User, String> {}
