package fpt.capstone.iContact.dto.response;

import fpt.capstone.iContact.dto.request.AddressInformationDTO;
import fpt.capstone.iContact.dto.request.ContactSalutionDTO;
import fpt.capstone.iContact.model.AddressInformation;
import fpt.capstone.iContact.model.Salution;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactResponse {
    private Long contactId ;
    private Long accountId ;
    private String accountName ;
    private String userId ;
    private String firstName ;
    private String lastName ;
    private String middleName ;
    private Long report_to ;
    private Salution contactSalution ;
    private AddressInformation addressInformation ;
    private String suffix ;
    private String title ;
    private String email ;
    private String phone;
    private String department ;
    private String mobile ;
    private String fax ;
    private String createdBy ;
    private LocalDateTime createDate;
    private LocalDateTime editDate ;
    private String editBy ;
    private Integer isDeleted ;
}
