package fpt.capstone.iContact.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private Long contactId ;
    @Column(name = "account_id")
    private Long accountId ;
    @Column(name = "user_id")
    private String userId ;
    @Column(name = "first_name")
    private String firstName ;
    @Column(name = "last_name")
    private String lastName ;
    @Column(name = "middle_name")
    private String middleName ;
    @Column(name = "report_to")
    private Long report_to ;
    @Column(name = "title")
    private String title ;
    @Column(name = "email")
    private String email ;
    @Column(name = "phone")
    private String phone;
    @Column(name = "department")
    private String department ;
    @Column(name = "mobile")
    private String mobile ;
    @Column(name = "fax")
    private String fax ;
    @Column(name = "created_by")
    private String createdBy ;
    @Column(name = "create_date")
    private LocalDateTime createDate;
    @Column(name = "edit_date")
    private LocalDateTime editDate ;
    @Column(name = "edit_by")
    private String editBy ;
    @Column(name = "is_deleted")
    private Integer isDeleted ;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "contact_salution_id")
    private Salution contactSalution;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "address_information_id")
    private AddressInformation addressInformation;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.DETACH, CascadeType.MERGE,
                    CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "contacts_user",
            joinColumns = @JoinColumn(name = "contact_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<Users> users ;
}
