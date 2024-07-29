package org.programmingtechie.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    private String name;
    private String statusBusiness;
}
