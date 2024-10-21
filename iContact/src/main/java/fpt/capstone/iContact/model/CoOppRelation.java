package fpt.capstone.iContact.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="opportunity_contact")
public class CoOppRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opportunity_contact_id")
    private Long cooppId;
    @Column(name = "opportunity_id")
    private Long opportunityId;
    @Column(name = "opportunity_contact_role_id")
    private Long opportunityContactRoleId;
    @Column(name = "contact_id")
    private Long contactId ;
}
