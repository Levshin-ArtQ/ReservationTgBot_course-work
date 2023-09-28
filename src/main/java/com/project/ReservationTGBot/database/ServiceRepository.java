package com.project.ReservationTGBot.database;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface ServiceRepository extends CrudRepository<Service, String> {

    @Override
    Set<Service> findAll();
    @Transactional
    @Modifying
    @Query("update services s set s.hasChildType = true where s.code is not null and s.code = :code")
    void makeParentType(@Param("code") String code);
    @Query(value = "SELECT s FROM services s WHERE s.parentType = :parentType")
    List<Service> findAllServicesByStage(@Param("parentType") String parentType);
}
