package com.splitbuddy.splitbuddy.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.splitbuddy.splitbuddy.models.Group;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    List<Group> findAll();

    List<Group> findByMembers_Id(UUID memberId);

}
