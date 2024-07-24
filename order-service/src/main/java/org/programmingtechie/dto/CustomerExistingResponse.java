package org.programmingtechie.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerExistingResponse {
    private String phoneNumber;
    private Boolean isExisting;
}
