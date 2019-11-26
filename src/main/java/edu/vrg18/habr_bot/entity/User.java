package edu.vrg18.habr_bot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class User {

    private UUID id;
    private String userName;
    private String newPassword;
    private String firstName;
    private String lastName;
    private boolean enabled;
}
