package edu.vrg18.habr_bot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String userName;
    private String firstName;
    private String newPassword;
    private boolean enabled;
}
