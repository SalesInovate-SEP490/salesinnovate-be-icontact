package fpt.capstone.iContact.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="contacts_user")
public class ContactsUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contacts_user_id")
    private Long contactsUserId;
    @Column(name = "contact_id")
    private Long contactId;
    @Column(name = "user_id")
    private String userId;
}
