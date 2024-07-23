package org.programmingtechie.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ImportHistoryRequest
{
    private String productId;

    private Integer quantity;

    private String note;
}
