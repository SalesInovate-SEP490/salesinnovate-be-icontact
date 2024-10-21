package fpt.capstone.iContact.service.impl;

import fpt.capstone.iContact.dto.Convert;
import fpt.capstone.iContact.dto.request.AddressInformationDTO;
import fpt.capstone.iContact.dto.request.ContactDTO;
import fpt.capstone.iContact.dto.request.ContactUserDTO;
import fpt.capstone.iContact.dto.request.ConvertFromLeadDTO;
import fpt.capstone.iContact.dto.response.ContactResponse;
import fpt.capstone.iContact.dto.response.PageResponse;
import fpt.capstone.iContact.dto.response.UserResponse;
import fpt.capstone.iContact.exception.ResourceNotFoundException;
import fpt.capstone.iContact.model.*;
import fpt.capstone.iContact.repository.*;
import fpt.capstone.iContact.repository.specification.ContactSpecificationsBuilder;
import fpt.capstone.iContact.service.ContactClientService;
import fpt.capstone.iContact.service.ContactService;

import fpt.capstone.iContact.service.ExcelUploadService;
import fpt.capstone.proto.lead.LeadDtoProto;
import fpt.capstone.proto.opportunity.OpportunityDtoProto;
import fpt.capstone.proto.user.UserDtoProto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fpt.capstone.iContact.util.AppConst.SEARCH_SPEC_OPERATOR;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactClientService contactClientService;
    private final ContactRepository contactRepository;
    private final CoOppRelationRepository coOppRelationRepository;
    private final Convert convert;
    private final SalutionRepository salutionRepository;
    private final AddressInformationRepository addressInformationRepository;
    private final SearchRepository searchRepository;
    private final ExcelUploadService excelUploadService;
    private final ContactsUserRepository contactsUserRepository;

    @Override
    @Transactional
    public Long createFromLead(String userId, ConvertFromLeadDTO dto) {
        LeadDtoProto proto = contactClientService.getLead(dto.getLeadId());
        try {
            // add new contact
            Contact contact = Contact.builder()
                    .accountId(dto.getAccountId())
                    .userId(userId)
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .middleName(dto.getMiddleName())
                    .title(proto.getTitle())
                    .email(proto.getEmail())
                    .phone(proto.getPhone())
                    .contactSalution(salutionRepository.findById(proto.getSalution().getLeadSalutionId()).orElse(null))
                    .addressInformation(addressInformationRepository
                            .findById(proto.getAddressInfor().getAddressInformationId()).orElse(null))
                    .createdBy(userId)
                    .editBy(userId)
                    .createDate(LocalDateTime.now())
                    .editDate(LocalDateTime.now())
                    .isDeleted(0)
                    .build();
            contactRepository.save(contact);

            if (!contactClientService.convertCampaignFromLeadToContact(userId,dto.getLeadId(), contact.getContactId()))
                throw new RuntimeException("Can not convert campaign from lead to contact");

//            //Them quan he giua nguoi convert voiws contact
//            ContactsUser user = ContactsUser.builder()
//                    .contactId(contact.getContactId())
//                    .userId(userId)
//                    .build();
//
//            //Them quan he giua user voi contact
//            for (ContactUserDTO userDTO : dto.getUserDTOS()) {
//                Specification<ContactsUser> spec = new Specification<ContactsUser>() {
//                    @Override
//                    public Predicate toPredicate(Root<ContactsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//                        List<Predicate> predicates = new ArrayList<>();
//                        predicates.add(criteriaBuilder.equal(root.get("contactId"), contact.getContactId()));
//                        predicates.add(criteriaBuilder.equal(root.get("userId"), userDTO.getUserId()));
//                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//                    }
//                };
//                if (contactsUserRepository.exists(spec)) continue;
//                ContactsUser contactsUser = ContactsUser.builder()
//                        .contactId(contact.getContactId())
//                        .userId(userDTO.getUserId())
//                        .build();
//                contactsUserRepository.save(contactsUser);
//            }
            boolean checkLogCall = contactClientService.convertLogCallToAccCo(
                    dto.getLeadId(),dto.getAccountId(),contact.getContactId());
            if(!checkLogCall) throw new RuntimeException("Can not convert log call from lead to contact,account");
            boolean checkLogEmail = contactClientService.convertLogEmailToAccCo(
                    dto.getLeadId(),dto.getAccountId(),contact.getContactId());
            if(!checkLogEmail) throw new RuntimeException("Can not convert log email from lead to contact,account");
            boolean checkLogEvent = contactClientService.convertEventToAccCo(
                    dto.getLeadId(),dto.getAccountId(),contact.getContactId());
            if(!checkLogEvent) throw new RuntimeException("Can not convert log event from lead to contact,account");

            return contact.getContactId();
        } catch (Exception e) {
            log.info(e.getMessage(), e.getCause());
        }
        return null;
    }

    @Override
    @Transactional
    public Long existingFromLead(String userId, long leadId,long contactId, long accountId, List<ContactUserDTO> userDTOS) {
        //check Account relation
        Contact contact = getContactById(contactId);
        if (contact != null && contact.getIsDeleted() == 0) {
            //Them quan he giua user voi contact
            for (ContactUserDTO userDTO : userDTOS) {
                Specification<ContactsUser> spec = new Specification<ContactsUser>() {
                    @Override
                    public Predicate toPredicate(Root<ContactsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), contact.getContactId()));
                        predicates.add(criteriaBuilder.equal(root.get("userId"), userDTO.getUserId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                if (contactsUserRepository.exists(spec)) continue;
                ContactsUser contactsUser = ContactsUser.builder()
                        .contactId(contact.getContactId())
                        .userId(userDTO.getUserId())
                        .build();
                contactsUserRepository.save(contactsUser);

                boolean checkLogCall = contactClientService.convertLogCallToAccCo(
                        leadId,accountId,contactId);
                if(!checkLogCall) throw new RuntimeException("Can not convert log call from lead to contact,account");
                boolean checkLogEmail = contactClientService.convertLogEmailToAccCo(
                        leadId,accountId,contactId);
                if(!checkLogEmail) throw new RuntimeException("Can not convert log email from lead to contact,account");
                boolean checkLogEvent = contactClientService.convertEventToAccCo(
                        leadId,accountId,contactId);
                if(!checkLogEvent) throw new RuntimeException("Can not convert log event from lead to contact,account");

            }
            return contact.getContactId();
        }
        return null;
    }

    @Override
    public PageResponse<?> getAllContactsWithSortByDefault(String userId, int pageNo, int pageSize) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.DESC, "createDate"));

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));

        Specification<Contact> spec = new Specification<Contact>() {
            @Override
            public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Join<Contact, Users> join = root.join("users", JoinType.INNER);
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                predicates.add(criteriaBuilder.notEqual(root.get("isDeleted"), 1));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        Page<Contact> leads = contactRepository.findAll(spec, pageable);
        return convert.convertToPageResponse(leads, pageable);
    }

    @Override
    public PageResponse<?> getAllContactByAccount(String userId, int pageNo, int pageSize, long id) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.DESC, "createDate"));

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));

        Specification<Contact> spec = new Specification<Contact>() {
            @Override
            public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
//                Join<Contact, Users> join = root.join("users", JoinType.INNER);
//                predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                predicates.add(criteriaBuilder.notEqual(root.get("isDeleted"), 1));
                predicates.add(criteriaBuilder.equal(root.get("accountId"), id));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };

        Page<Contact> contacts = contactRepository.findAll(spec, pageable);
        return convert.convertToPageResponse(contacts, pageable);
    }

    @Override
    public PageResponse<?> getAllContactByOpportunity(String userId, int pageNo, int pageSize, long id) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.DESC, "createDate"));

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));

        Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
            @Override
            public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("opportunityId"), id));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        List<CoOppRelation> list = coOppRelationRepository.findAll(spec);
        if (!list.isEmpty()) {
            List<Contact> contacts = new ArrayList<>();
            for (CoOppRelation relation : list) {
                Specification<Contact> spec2 = new Specification<Contact>() {
                    @Override
                    public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        Join<Contact, Users> join = root.join("users", JoinType.INNER);
                        predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                        predicates.add(criteriaBuilder.notEqual(root.get("isDeleted"), 1));
                        predicates.add(criteriaBuilder.equal(root.get("accountId"), relation.getContactId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                if (!contactRepository.exists(spec2)) continue;
                contacts.add(contactRepository.findById(relation.getContactId()).orElse(null));
            }
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), contacts.size());

            Page<Contact> opportunitiesPage = new PageImpl<>(contacts.subList(start, end), pageable, contacts.size());
            return convert.convertToPageResponse(opportunitiesPage, pageable);
        }
        return null;
    }

    @Override
    public ContactResponse getContactDetail(long id) {
        Contact contact = getContactById(id);
        return convert.entityToContactResponse(contact);
    }

    @Override
    public ContactResponse getContactDetailByUser(String userId, long id) {
        try {
            Specification<Contact> spec = new Specification<Contact>() {
                @Override
                public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    Join<Contact, Users> join = root.join("users", JoinType.INNER);
                    predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                    predicates.add(criteriaBuilder.notEqual(root.get("isDeleted"), 1));
                    predicates.add(criteriaBuilder.equal(root.get("accountId"), id));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            boolean existed = contactRepository.exists(spec);
            if (!existed) throw new RuntimeException("Can not get contact detail");
            Contact contact = getContactById(id);
            return convert.entityToContactResponse(contact);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean deleteContact(String userId, long id) {
        Specification<Contact> spec1 = new Specification<Contact>() {
            @Override
            public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                Join<Contact, Users> join = root.join("users", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                predicates.add(criteriaBuilder.notEqual(root.get("isDeleted"), 1));
                predicates.add(criteriaBuilder.equal(root.get("accountId"), id));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        boolean existed = contactRepository.exists(spec1);
        if (!existed) throw new RuntimeException("Can not delete contact ");
        Contact contact = getContactById(id);
        if (contact != null) {
            contact.setIsDeleted(1);
            Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
                @Override
                public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), id));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CoOppRelation> list = coOppRelationRepository.findAll(spec);
            for (CoOppRelation relation : list) {
                if (!contactClientService.deleteContactRole(id, relation.getOpportunityId()))
                    throw new RuntimeException("Can not delete relation between opportunity and contact");
            }
            contactRepository.save(contact);
            return true;
        }
        return false;
    }

    @Override
    public Long createContact(String userId, ContactDTO contactDTO) {
        Contact contact = convert.DTOToEntity(contactDTO);
        if (contact != null) {
            contact.setCreatedBy(userId);
            contact.setEditBy(userId);
            contact.setCreateDate(LocalDateTime.now());
            contact.setEditDate(LocalDateTime.now());
            contact.setIsDeleted(0);
            if (contact.getAddressInformation().getAddressInformationId() == null) {
                AddressInformation addressInformation = convert.DTOToAddressInformation(contactDTO.getAddressInformation());
                addressInformationRepository.save(addressInformation);
                contact.setAddressInformation(addressInformation);
            }
            contactRepository.save(contact);
            //Thêm quan hệ giữa contact và user
            ContactsUser contactsUser = ContactsUser.builder()
                    .contactId(contact.getContactId())
                    .userId(userId)
                    .build();
            contactsUserRepository.save(contactsUser);
            return contact.getContactId();
        }
        return null;
    }

    @Override
    @Transactional
    public boolean patchContact(String userId, ContactDTO leadDTOOld, long id) {

        Map<String, Object> patchMap = getPatchData(leadDTOOld);
        if (patchMap.isEmpty()) {
            return true;
        }

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find contact with id: " + id));
        checkRelationContactAndUser(userId,contact.getContactId());
        if (contact != null) {
            for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Field fieldDTO = ReflectionUtils.findField(ContactDTO.class, key);

                if (fieldDTO == null) {
                    continue;
                }

                fieldDTO.setAccessible(true);
                Class<?> type = fieldDTO.getType();

                try {
                    if (type == long.class && value instanceof String) {
                        value = Long.parseLong((String) value);
                    } else if (type == Long.class && value instanceof String) {
                        value = Long.valueOf((String) value);
                    }
                } catch (NumberFormatException e) {
                    return false;
                }

                switch (key) {
                    case "contactSalutionId":
                        contact.setContactSalution(salutionRepository.findById((Long) value).orElse(null));
                        break;
                    case "addressInformation":
                        AddressInformationDTO dto = (AddressInformationDTO) value;
                        if (!Objects.equals(dto.getStreet(), contact.getAddressInformation().getStreet()))
                            contact.getAddressInformation().setStreet(dto.getStreet());
                        if (!Objects.equals(dto.getCity(), contact.getAddressInformation().getCity()))
                            contact.getAddressInformation().setCity(dto.getCity());
                        if (!Objects.equals(dto.getProvince(), contact.getAddressInformation().getProvince()))
                            contact.getAddressInformation().setProvince(dto.getProvince());
                        if (!Objects.equals(dto.getPostalCode(), contact.getAddressInformation().getPostalCode()))
                            contact.getAddressInformation().setPostalCode(dto.getPostalCode());
                        if (!Objects.equals(dto.getCountry(), contact.getAddressInformation().getCountry()))
                            contact.getAddressInformation().setCountry(dto.getCountry());
                        break;
                    default:
                        if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                            Field field = ReflectionUtils.findField(Contact.class, fieldDTO.getName());
                            assert field != null;
                            field.setAccessible(true);
                            ReflectionUtils.setField(field, contact, value);
                        } else {
                            return false;
                        }
                }
            }
            contact.setEditDate(LocalDateTime.now());
            contact.setEditBy(userId);
            contactRepository.save(contact);

            //Thêm xử lý notification cho patch lead
            List<String> listUser = new ArrayList<>();
            Specification<ContactsUser> spec1 = new Specification<ContactsUser>() {
                @Override
                public Predicate toPredicate(Root<ContactsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("contactId"), id));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ContactsUser> leadUsers = contactsUserRepository.findAll(spec1);
            for(ContactsUser user : leadUsers){
                listUser.add(user.getUserId());
            }
            contactClientService.createNotification(userId,"The Contact you were assigned has been updated."
                    ,id,1L,listUser);
            return true;
        }

        return false;
    }

    @Override
    public PageResponse<?> filterContact(String userId, Pageable pageable, String[] search) {
        ContactSpecificationsBuilder builder = new ContactSpecificationsBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
            for (String l : search) {
                Matcher matcher = pattern.matcher(l);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
                }
            }

            Page<Contact> leadPage = searchRepository.searchUserByCriteriaWithJoin(userId, builder.params, pageable);
            return convert.convertToPageResponse(leadPage, pageable);
        }
        return getAllContactsWithSortByDefault(userId, pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    public ByteArrayInputStream getExportFileData() throws IOException {
        List<Contact> accounts = contactRepository.findAll();
        ByteArrayInputStream byteArrayInputStream = excelUploadService.dataToExecel(accounts);
        return byteArrayInputStream;
    }

    @Override
    public List<Contact> searchContactRole(String search, Long opportunityId) {
        OpportunityDtoProto proto = contactClientService.getOpportunity(opportunityId);
        final String searchString = (search == null) ? "" : search; // Use a final variable
        Specification<Contact> spec = new Specification<Contact>() {
            @Override
            public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> orPredicates = new ArrayList<>();
                List<Predicate> andPredicates = new ArrayList<>();

                // Adding predicates for OR condition
                orPredicates.add(criteriaBuilder.like(root.get("firstName"), "%" + searchString + "%"));
                orPredicates.add(criteriaBuilder.like(root.get("lastName"), "%" + searchString + "%"));
                // Combine OR predicates
                Predicate orCombined = criteriaBuilder.or(orPredicates.toArray(new Predicate[0]));
                // Adding predicates for AND condition
//                andPredicates.add(criteriaBuilder.equal(root.get("accountId"), proto.getAccountId()));
                andPredicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));
                // Combine OR and AND predicates
                andPredicates.add(orCombined);
                return criteriaBuilder.and(andPredicates.toArray(new Predicate[0]));
            }
        };
        return contactRepository.findAll(spec);

    }

    @Override
    public boolean addUseToContact(String userId, Long contactId, List<ContactUserDTO> userDTOS) {
        try {
            Contact contact = contactRepository.findById(contactId).orElse(null);
            if (contact == null) throw new RuntimeException("Cannot find contact");
            checkRelationContactAndUser(userId,contact.getContactId());
            List<String> listUser = new ArrayList<>();

            for (ContactUserDTO dto : userDTOS) {
                Specification<Contact> spec = new Specification<Contact>() {
                    @Override
                    public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Join<Contact, Users> join = root.join("users", JoinType.INNER);
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(join.get("userId"), dto.getUserId()));
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), contactId));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                boolean existed = contactRepository.exists(spec);
                if (existed) continue;
                ContactsUser contactsUser = ContactsUser.builder()
                        .userId(dto.getUserId())
                        .contactId(contactId)
                        .build();
                contactsUserRepository.save(contactsUser);
                listUser.add(dto.getUserId());
            }
            contactClientService.createNotification(userId,"You have been assigned to the new Contact."
                    ,contactId,1L,listUser);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<UserResponse> getListUserInContact(Long contactId) {
        try {
            List<UserResponse> responses = new ArrayList<>();
            Optional<Contact> accounts = contactRepository.findById(contactId);
            if (accounts.isEmpty()) throw new RuntimeException("Can not find leads");
            Specification<ContactsUser> spec = new Specification<ContactsUser>() {
                @Override
                public Predicate toPredicate(Root<ContactsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("contactId"), contactId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ContactsUser> users = contactsUserRepository.findAll(spec);
            for (ContactsUser user : users) {
                UserDtoProto proto = contactClientService.getUser(user.getUserId());
                if (proto.getUserId().isEmpty()) continue;
                UserResponse userResponse = UserResponse.builder()
                        .userId(proto.getUserId())
                        .userName(proto.getUserName())
                        .firstName(proto.getFirstName())
                        .lastName(proto.getLastName())
                        .email(proto.getEmail())
                        .build();
                responses.add(userResponse);
            }
            return responses;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean assignUserFollowingAccount(Long accountId, List<String> listUsers) {
        try{
            Specification<Contact> spec = new Specification<Contact>() {
                @Override
                public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("accountId"), accountId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<Contact> contacts = contactRepository.findAll(spec);
            for(Contact contact : contacts){
                for (String userId : listUsers) {
                    Specification<Contact> spec2 = new Specification<Contact>() {
                        @Override
                        public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            Join<Contact, Users> join = root.join("users", JoinType.INNER);
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                            predicates.add(criteriaBuilder.equal(root.get("contactId"), contact.getContactId()));
                            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                        }
                    };
                    boolean existed = contactRepository.exists(spec2);
                    if (existed) continue;
                    ContactsUser contactsUser = ContactsUser.builder()
                            .userId(userId)
                            .contactId(contact.getContactId())
                            .build();
                    contactsUserRepository.save(contactsUser);
                }
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private Contact getContactById(long contactId) {
        Contact contact = contactRepository.findById(
                contactId).orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
        return (contact.getIsDeleted() != null && contact.getIsDeleted() == 0) ? contact : null;
    }

    private Map<String, Object> getPatchData(Object obj) {
        Class<?> objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();
        Map<String, Object> patchMap = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    patchMap.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                log.info(e.getMessage());
            }
        }
        return patchMap;
    }

    private void checkRelationContactAndUser (String userId,Long contactId){
        Specification<ContactsUser> spec = new Specification<ContactsUser>() {
            @Override
            public Predicate toPredicate(Root<ContactsUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("contactId"), contactId));
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        boolean exists = contactsUserRepository.exists(spec);
        if(!exists) throw new RuntimeException("Did not been assigned to this contact ");
    }
}
