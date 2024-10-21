package fpt.capstone.iContact.dto.request;

import fpt.capstone.iContact.model.AddressInformation;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
@Builder
public class ContactExportDTO {
    private Long contactId ;
    private String firstName ;
    private String lastName ;
    private String middleName ;
    private Long report_to ;
    private Long contactSalutionId ;
    private Long addressInformationId ;
    private String title ;
    private String email ;
    private String phone;
    private String department ;
    private String mobile ;
    private String fax ;
}
