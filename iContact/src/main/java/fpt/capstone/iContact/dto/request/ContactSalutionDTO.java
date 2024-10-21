package fpt.capstone.iContact.dto.request;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactSalutionDTO {
    private Long leadSalutionId ;
    private String leadSalutionName;
}
