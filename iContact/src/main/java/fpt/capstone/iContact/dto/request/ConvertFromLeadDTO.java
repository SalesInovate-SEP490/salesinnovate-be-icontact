package fpt.capstone.iContact.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConvertFromLeadDTO {
    long leadId;
    long accountId;
    long salution;
    String firstName;
    String middleName;
    String lastName;
    List<ContactUserDTO> userDTOS;
}
