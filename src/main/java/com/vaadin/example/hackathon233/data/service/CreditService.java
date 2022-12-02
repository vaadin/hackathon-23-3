package com.vaadin.example.hackathon233.data.service;

import com.vaadin.example.hackathon233.data.entity.Credit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CreditService {

    private final CreditRepository repository;

    @Autowired
    public CreditService(CreditRepository repository) {
        this.repository = repository;
    }

    public Optional<Credit> get(UUID id) {
        return repository.findById(id);
    }

    public Credit update(Credit entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Credit> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
