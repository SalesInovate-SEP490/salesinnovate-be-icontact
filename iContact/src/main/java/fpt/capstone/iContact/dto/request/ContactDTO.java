package fpt.capstone.iContact.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Builder
public class ContactDTO {
    private Long contactId ;
    private Long accountId ;
    private String userId ;
    private String firstName ;
    private String lastName ;
    private String middleName ;
    private Long report_to ;
    private Long contactSalutionId ;
    private AddressInformationDTO addressInformation ;
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
