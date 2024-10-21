package fpt.capstone.iContact.dto.request;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactUserDTO {
    private Long contactsUserId;
    private Long contactId;
    private String userId;
}
