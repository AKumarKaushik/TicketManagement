package com.enterprise.ai.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TicketJpaRepository extends JpaRepository<TicketEntity, UUID> {

    List<TicketEntity> findByStatus(String status);

    @EntityGraph(attributePaths = "comments")
    @Query("select t from TicketEntity t where t.id = :id")
    Optional<TicketEntity> findWithCommentsById(@Param("id") UUID id);
}
