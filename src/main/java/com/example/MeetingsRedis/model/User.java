package com.example.MeetingsRedis.model;

import java.util.Objects;

public class User {
    private final String email;
    private String name;
    private int age;
    private String gender;

    public User(String email, String name, int age, String gender) {
        this.email = Objects.requireNonNull(email);
        this.name = Objects.requireNonNull(name);
        this.age = age;
        this.gender = gender;
    }

    public User(String email) {
        this.email = email;
    }

    // Getters
    public String getEmail() { return email; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }

    // Setters (except for email, which is immutable)
    public void setName(String name) { this.name = Objects.requireNonNull(name); }
    public void setAge(int age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }

    @Override
    public String toString() {
        return String.format("User[email=%s, name=%s, age=%d, gender=%s]",
                email, name, age, gender);
    }
}