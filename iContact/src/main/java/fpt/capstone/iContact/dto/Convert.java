package fpt.capstone.iContact.dto;

import fpt.capstone.iContact.dto.request.AddressInformationDTO;
import fpt.capstone.iContact.dto.request.ContactDTO;
import fpt.capstone.iContact.dto.request.ContactExportDTO;
import fpt.capstone.iContact.dto.request.ContactSalutionDTO;
import fpt.capstone.iContact.dto.response.ContactResponse;
import fpt.capstone.iContact.dto.response.PageResponse;
import fpt.capstone.iContact.model.AddressInformation;
import fpt.capstone.iContact.model.Contact;
import fpt.capstone.iContact.model.Salution;
import fpt.capstone.iContact.repository.AddressInformationRepository;
import fpt.capstone.iContact.repository.SalutionRepository;
import fpt.capstone.iContact.service.ContactClientService;
import fpt.capstone.proto.account.AccountDtoProto;
import fpt.capstone.proto.lead.AddressInformationDtoProto;
import fpt.capstone.proto.lead.LeadSalutionDtoProto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Convert {

    private final AddressInformationRepository addressInformationRepository;
    private final SalutionRepository salutionRepository;
    private final ContactClientService contactClientService;


    public ContactResponse entityToContactResponse(Contact contact){
        if (contact==null) return null ;
        AccountDtoProto proto = contactClientService.getAccount(contact.getAccountId());
        return ContactResponse.builder()
                .contactId(contact.getContactId())
                . accountId(contact.getAccountId())
                . accountName(proto.getAccountName())
                . userId(contact.getUserId())
                . firstName(contact.getFirstName())
                . lastName(contact.getLastName())
                . middleName(contact.getMiddleName())
                . report_to(contact.getReport_to())
                . contactSalution(contact.getContactSalution()==null?null:
                        salutionRepository.findById(contact.getContactSalution().getLeadSalutionId()).orElse(null))
                . addressInformation(contact.getAddressInformation()==null?null:
                        addressInformationRepository.findById(contact.getAddressInformation().getAddressInformationId()).orElse(null))
                . title(contact.getTitle())
                . email(contact.getEmail())
                . phone(contact.getPhone())
                . department(contact.getDepartment())
                . mobile(contact.getMobile())
                . fax(contact.getFax())
                . createdBy(contact.getEditBy())
                . createDate(contact.getCreateDate())
                . editDate(contact.getEditDate())
                . editBy(contact.getEditBy())
                . isDeleted(contact.getIsDeleted())
                .build();
    }

    public PageResponse<?> convertToPageResponse(Page<Contact> leads, Pageable pageable) {
//        List<ContactResponse> response = leads.stream().map(this::entityToContactResponse).toList();
        return PageResponse.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .total(leads.getTotalPages())
                .items(leads.getContent())
                .build();
    }

    public AddressInformationDTO entityToAddressinformationDTO(Long id) {
        if(id==null) return null;
        AddressInformation addressInformation =addressInformationRepository.findById(id).orElse(null);
        if(addressInformation==null) return null ;
        return AddressInformationDTO.builder()
                .addressInformationId(addressInformation.getAddressInformationId())
                .street(addressInformation.getStreet())
                .city(addressInformation.getCity())
                .province(addressInformation.getProvince())
                .postalCode(addressInformation.getPostalCode())
                .country(addressInformation.getCountry())
                .build();
    }

    public ContactSalutionDTO entityToContactSalutionDTO(Long id) {
        if(id==null) return null;
        Salution salution =salutionRepository.findById(id).orElse(null);
        if(salution==null) return null;
        return ContactSalutionDTO.builder()
                .leadSalutionId(salution.getLeadSalutionId())
                .leadSalutionName(salution.getLeadSalutionName())
                .build();
    }

    public Contact DTOToEntity(ContactDTO contact) {
        if(contact == null) return null;
        return Contact.builder()
                .contactId(contact.getContactId())
                . accountId(contact.getAccountId())
                . userId(contact.getUserId())
                . firstName(contact.getFirstName())
                . lastName(contact.getLastName())
                . middleName(contact.getMiddleName())
                . report_to(contact.getReport_to())
                . contactSalution(salutionRepository.findById(contact.getContactSalutionId()).orElse(null))
                . addressInformation(DTOToAddressInformation(contact.getAddressInformation()))
                . title(contact.getTitle())
                . email(contact.getEmail())
                . phone(contact.getPhone())
                . department(contact.getDepartment())
                . mobile(contact.getMobile())
                . fax(contact.getFax())
                . createdBy(contact.getCreatedBy())
                . createDate(contact.getCreateDate())
                . editDate(contact.getEditDate())
                . editBy(contact.getEditBy())
                . isDeleted(contact.getIsDeleted())
                .build();
    }
    public AddressInformation DTOToAddressInformation(AddressInformationDTO addressInformation) {
        if (addressInformation == null) return null ;
        return AddressInformation.builder()
                .addressInformationId(addressInformation.getAddressInformationId())
                .street(addressInformation.getStreet())
                .city(addressInformation.getCity())
                .province(addressInformation.getProvince())
                .postalCode(addressInformation.getPostalCode())
                .country(addressInformation.getCountry())
                .build();
    }
    public ContactExportDTO ContactEntityToContactExportDTO(Contact contact) {
        return ContactExportDTO.builder()
                .contactId(contact.getContactId())
                .firstName(Optional.ofNullable(contact.getFirstName()).orElse(null))
                .lastName(Optional.ofNullable(contact.getLastName()).orElse(null))
                .contactSalutionId(Optional.ofNullable(contact.getContactSalution()).map(Salution::getLeadSalutionId).orElse(null))
                .addressInformationId(Optional.ofNullable(contact.getAddressInformation()).map(AddressInformation::getAddressInformationId).orElse(null))
                .phone(Optional.ofNullable(contact.getPhone()).orElse(null))
                .email(Optional.ofNullable(contact.getEmail()).orElse(null))
                .title(Optional.ofNullable(contact.getTitle()).orElse(null))
                .department(Optional.ofNullable(contact.getDepartment()).orElse(null))
                .mobile(Optional.ofNullable(contact.getMobile()).orElse(null))
                .fax(Optional.ofNullable(contact.getFax()).orElse(null))
                .build();
    }
    public List<ContactExportDTO> ContactEntityListToContactExportDTOList(List<Contact> accountList) {
        return accountList.stream()
                .map(this::ContactEntityToContactExportDTO)
                .collect(Collectors.toList());
    }
}
