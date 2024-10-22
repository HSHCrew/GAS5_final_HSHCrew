package org.zerock.Altari.user.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ModicationDTO {
    private int modication_id;
    private String modication_name;
    private String modication_info;
    private String interaction_info;
}
