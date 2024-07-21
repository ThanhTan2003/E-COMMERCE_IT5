package org.programmingtechie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CustomerResponse
{
    private String id;

    private String fullName;

    private String address;

    private String phoneNumber;

    private String email;

    private LocalDate dateOfBirth;

    private String gender;
}
