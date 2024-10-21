package fpt.capstone.iContact.controller;

import fpt.capstone.iContact.dto.request.ContactDTO;
import fpt.capstone.iContact.dto.request.ContactUserDTO;
import fpt.capstone.iContact.dto.request.ConvertFromLeadDTO;
import fpt.capstone.iContact.dto.response.ResponseData;
import fpt.capstone.iContact.dto.response.ResponseError;
import fpt.capstone.iContact.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService ;

    @PostMapping("/create-from-lead")
    public ResponseData<Long> createFromLead( @RequestBody ConvertFromLeadDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            long contactId = contactService.createFromLead(userId,dto);
            return new ResponseData<>(1,HttpStatus.CREATED.value(), contactId);
        } catch (Exception e) {
            log.error("errorMessage={}", e.getMessage(), e.getCause());
            return new ResponseError(0,HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/existing-from-lead/{leadId}/{contactId}/{accountId}")
    public ResponseData<Long> existingFromLead( @PathVariable long leadId,@PathVariable long contactId ,@PathVariable long accountId,@RequestBody List<ContactUserDTO> userDTOS) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            long existingContactId = contactService.existingFromLead(userId,leadId,contactId,accountId,userDTOS);
            return new ResponseData<>(1,HttpStatus.CREATED.value(), existingContactId);
        } catch (Exception e) {
            log.error("errorMessage={}", e.getMessage(), e.getCause());
            return new ResponseError(0,HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/list-contact")
    public ResponseData<?> getListContact(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                          @RequestParam(defaultValue = "20", required = false) int pageSize){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    contactService.getAllContactsWithSortByDefault(userId,pageNo, pageSize));
        } catch (Exception e) {

            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/list-contact-by-account")
    public ResponseData<?> getListContactByAccount(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                          @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                   @RequestParam long id){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), contactService.getAllContactByAccount(userId,pageNo, pageSize,id));
        } catch (Exception e) {

            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/list-contact-by-opportunity")
    public ResponseData<?> getAllContactByOpportunity(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                          @RequestParam(defaultValue = "10", required = false) int pageSize,
                                                   @RequestParam long opportunityId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), contactService.getAllContactByOpportunity(userId,pageNo, pageSize,opportunityId));
        } catch (Exception e) {

            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/contact-detail")
    public ResponseData<?> getContactDetail(@RequestParam long id){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(), contactService.getContactDetail(id));
        } catch (Exception e) {

            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @DeleteMapping("/delete-contact")
    public ResponseData<?> deleteContact(@RequestParam long id){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return contactService.deleteContact(userId,id)?
                    new ResponseData<>( HttpStatus.OK.value(),"Delete success",1 ):
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Delete fail");
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @PostMapping("/create-contact")
    public ResponseData<?> createContact(@RequestBody ContactDTO contactDTO){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            Long contactId = contactService.createContact(userId,contactDTO);
            return new ResponseData<>( HttpStatus.OK.value(),"Create contact success",
                    contactId,1);
        }catch (Exception e){
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Delete fail");
        }
    }

    @GetMapping("/filter-contact")
    public ResponseData<?> filterLeadsWithSpecifications(Pageable pageable,
                                                         @RequestParam(required = false) String[] search) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    contactService.filterContact(userId,pageable, search));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "list source fail");
        }
    }

    @GetMapping("/search-contact-role")
    public ResponseData<?> searchContactRole(@RequestParam long opportunityId,
                                                         @RequestParam(required = false) String search) {
        try {
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    contactService.searchContactRole(search, opportunityId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "search for contact role fail");
        }
    }

    @PatchMapping("/patch-contact")
    public ResponseData<?> patchContact(@RequestBody ContactDTO contactDTO,@RequestParam long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return contactService.patchContact(userId,contactDTO,id)?
                new ResponseData<>( HttpStatus.OK.value(),"Patch contact success",1):
                new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "Patch contact fail");
    }
    @GetMapping("/export-file")
    public ResponseEntity<Resource> getFileExport() throws IOException {
        String filename = "contacts.xlsx";
        ByteArrayInputStream fileExport = contactService.getExportFileData();
        InputStreamResource file = new InputStreamResource(fileExport);

        ResponseEntity<Resource> response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);

        // Debugging headers
        HttpHeaders headers = response.getHeaders();
        System.out.println("Content-Disposition: " + headers.get(HttpHeaders.CONTENT_DISPOSITION));
        System.out.println("Content-Type: " + headers.get(HttpHeaders.CONTENT_TYPE));

        return response;
    }

    @PostMapping("/add-users/{contactId}")
    public ResponseData<?> addUserToLead(@PathVariable Long contactId,@RequestBody List<ContactUserDTO> userDTOS) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return contactService.addUseToContact(userId,contactId,userDTOS) ?
                    new ResponseData<>(1, HttpStatus.OK.value(), "add users success") :
                    new ResponseError(0, HttpStatus.BAD_REQUEST.value(), "add users fail");
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/get-user/{contactId}")
    public ResponseData<?> getListUserInLead(@PathVariable Long contactId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            return new ResponseData<>(1, HttpStatus.OK.value(),
                    contactService.getListUserInContact(contactId));
        } catch (Exception e) {
            return new ResponseError(0, HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
