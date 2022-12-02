package com.vaadin.example.hackathon233.data.entity;

import java.time.LocalDate;
import javax.persistence.Entity;

@Entity
public class Credit extends AbstractEntity {

    private String name;
    private Integer loan;
    private Integer years;
    private Double interest;
    private LocalDate date;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getLoan() {
        return loan;
    }
    public void setLoan(Integer loan) {
        this.loan = loan;
    }
    public Integer getYears() {
        return years;
    }
    public void setYears(Integer years) {
        this.years = years;
    }
    public Double getInterest() {
        return interest;
    }
    public void setInterest(Double interest) {
        this.interest = interest;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

}
