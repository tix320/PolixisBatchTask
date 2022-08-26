package com.polixis.batchtask.job.dto;

import java.time.LocalDate;

public record PersonRecord(String firstName, String lastName, LocalDate birthDate) {

}
