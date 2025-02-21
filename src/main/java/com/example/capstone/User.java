package com.example.capstone;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")  
public class User {
    @Id
    private String id;
    private String name;
    private String email;

    public User() {}  

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
