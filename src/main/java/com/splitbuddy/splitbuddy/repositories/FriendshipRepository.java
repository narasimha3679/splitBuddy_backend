package com.splitbuddy.splitbuddy.repositories;

import com.splitbuddy.splitbuddy.models.Friendship;
import com.splitbuddy.splitbuddy.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByUser(User user);

    boolean existsByUserAndFriend(User user, User friend);

}
