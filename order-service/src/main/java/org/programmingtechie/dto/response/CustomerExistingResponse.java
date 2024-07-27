package org.programmingtechie.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerExistingResponse {
    private Boolean isExisting;

    private String id;

    private String fullName;

    private String address;

    private String phoneNumber;

    private String email;

    private LocalDate dateOfBirth;

    private String gender;
}
