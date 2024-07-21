package org.programmingtechie.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerRequest
{
    private String fullName;

    private String address;

    private String phoneNumber;

    private String email;

    private LocalDate dateOfBirth;

    private String gender;
}
