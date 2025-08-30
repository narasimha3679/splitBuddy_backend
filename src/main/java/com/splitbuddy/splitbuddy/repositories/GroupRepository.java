package com.splitbuddy.splitbuddy.repositories;

import com.splitbuddy.splitbuddy.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByMembers_Id(Long memberId);

}
