package com.splitbuddy.splitbuddy.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "friendships")
@Getter
@Setter
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("friendships")
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    @JsonBackReference("friendships")
    private User friend;

    private LocalDateTime becameFriendsAt;

}
