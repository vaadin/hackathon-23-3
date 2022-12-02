package com.vaadin.example.hackathon233.data.service;

import com.vaadin.example.hackathon233.data.entity.Credit;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditRepository extends JpaRepository<Credit, UUID> {

}