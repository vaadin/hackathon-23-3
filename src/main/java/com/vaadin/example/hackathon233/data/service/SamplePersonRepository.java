package com.vaadin.example.hackathon233.data.service;

import com.vaadin.example.hackathon233.data.entity.SamplePerson;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SamplePersonRepository extends JpaRepository<SamplePerson, UUID> {

}