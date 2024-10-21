package fpt.capstone.iContact.dto.request;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AddressInformationDTO {
    private Long addressInformationId ;
    private String street;
    private String city ;
    private String province;
    private String postalCode;
    private String country ;


}
