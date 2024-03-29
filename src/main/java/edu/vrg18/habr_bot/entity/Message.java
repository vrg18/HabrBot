package edu.vrg18.habr_bot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Message {

    private UUID id;
    private User author;
    private Room room;
    private String text;
}
