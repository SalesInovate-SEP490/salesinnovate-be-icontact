package fpt.capstone.iContact.service;

import fpt.capstone.iContact.dto.request.ContactDTO;
import fpt.capstone.iContact.dto.request.ContactUserDTO;
import fpt.capstone.iContact.dto.request.ConvertFromLeadDTO;
import fpt.capstone.iContact.dto.response.ContactResponse;
import fpt.capstone.iContact.dto.response.PageResponse;
import fpt.capstone.iContact.dto.response.UserResponse;
import fpt.capstone.iContact.model.Contact;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface ContactService {

    Long createFromLead (String userId, ConvertFromLeadDTO dto);

    Long existingFromLead(String userId,long leadId,long contactId, long  accountId,List<ContactUserDTO> userDTOS);

    PageResponse<?> getAllContactsWithSortByDefault(String userId,int pageNo, int pageSize);

    PageResponse<?> getAllContactByAccount(String userId,int pageNo, int pageSize, long id);
    PageResponse<?> getAllContactByOpportunity(String userId,int pageNo, int pageSize, long id);

    ContactResponse getContactDetail(long id);
    ContactResponse getContactDetailByUser(String userId,long id);

    boolean deleteContact(String userId,long id);

    Long createContact(String userId,ContactDTO contactDTO);

    boolean patchContact(String userId,ContactDTO contactDTO,long id);

    PageResponse<?> filterContact(String userId,Pageable pageable, String[] search);
    ByteArrayInputStream getExportFileData() throws IOException;
    List<Contact> searchContactRole (String search, Long opportunityId);
    boolean addUseToContact(String userId,Long contactId,List<ContactUserDTO> userDTOS);
    List<UserResponse> getListUserInContact(Long contactId);
    boolean assignUserFollowingAccount(Long accountId,List<String> listUsers);
}
