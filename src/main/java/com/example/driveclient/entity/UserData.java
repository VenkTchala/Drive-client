package com.example.driveclient.entity;


import lombok.Getter;
import lombok.Setter;

public class UserData {
    @Getter
    @Setter
    private static String token;

    @Getter
    @Setter
    private static String email;

    @Getter
    @Setter
    private static String firstName;

    @Getter
    @Setter
    private static String lastName;
}
