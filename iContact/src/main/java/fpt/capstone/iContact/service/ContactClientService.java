package fpt.capstone.iContact.service;

import com.google.protobuf.Descriptors;
import fpt.capstone.iContact.dto.request.AddressInformationDTO;
import fpt.capstone.proto.account.AccountDtoProto;
import fpt.capstone.proto.account.AccountServiceGrpc;
import fpt.capstone.proto.account.GetAccountRequest;
import fpt.capstone.proto.account.GetAccountResponse;
import fpt.capstone.proto.lead.*;
import fpt.capstone.proto.opportunity.*;
import fpt.capstone.proto.user.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ContactClientService {

    @GrpcClient("iLead")
    LeadServiceGrpc.LeadServiceBlockingStub stub ;

    @GrpcClient("iOpportunity")
    OpportunityServiceGrpc.OpportunityServiceBlockingStub stubOpp ;

    @GrpcClient("iUser")
    UserServiceGrpc.UserServiceBlockingStub stubUser ;

    @GrpcClient("iAccount")
    AccountServiceGrpc.AccountServiceBlockingStub stubAccount ;

    public LeadDtoProto getLead (Long leadId){
        GetLeadRequest request = GetLeadRequest.newBuilder()
                .setLeadId(leadId)
                .build();
        GetLeadResponse response = stub.getLead(request);
        return response.getResponse();
    }

    public OpportunityDtoProto getOpportunity (Long opportunityId){
        GetOpportunityRequest request = GetOpportunityRequest.newBuilder()
                .setOpportunityId(opportunityId)
                .build();
        GetOpportunityResponse response = stubOpp.getOpportunity(request);
        return response.getResponse();
    }

    public boolean convertCampaignFromLeadToContact (String userID,Long leadId, Long contactId){
        ConvertCampaignFromLeadToContactRequest request = ConvertCampaignFromLeadToContactRequest.newBuilder()
                .setUserId(userID)
                .setLeadId(leadId)
                .setContactId(contactId)
                .build();
        ConvertCampaignFromLeadToContactResponse response = stubOpp.convertCampaignFromLeadToContact(request);
        return response.getResponse();
    }

    public boolean deleteContactRole (Long contactId, Long opportunityId){
        DeleteContactRoleRequest request = DeleteContactRoleRequest.newBuilder()
                .setContactId(contactId)
                .setOpportunityId(opportunityId)
                .build();
        DeleteContactRoleResponse response = stubOpp.deleteContactRole(request);
        return response.getResponse();
    }

    public UserDtoProto getUser (String userId){
        GetUserRequest request = GetUserRequest.newBuilder()
                .setUserId(userId)
                .build();
        GetUserResponse response = stubUser.getUser(request);
        return response.getResponse();
    }

    public boolean convertLogCallToAccCo(Long leadId, Long accountId, Long contactId){
        ConvertLogCallToAccCoRequest request = ConvertLogCallToAccCoRequest.newBuilder()
                .setLeadId(leadId)
                .setAccountId(accountId)
                .setContactId(contactId)
                .build();
        ConvertLogCallToAccCoResponse response = stubUser.convertLogCallToAccCo(request);
        return response.getResponse();
    }

    public boolean convertLogEmailToAccCo(Long leadId, Long accountId, Long contactId){
        ConvertLogEmailToAccCoRequest request = ConvertLogEmailToAccCoRequest.newBuilder()
                .setLeadId(leadId)
                .setAccountId(accountId)
                .setContactId(contactId)
                .build();
        ConvertLogEmailToAccCoResponse response = stubUser.convertLogEmailToAccCo(request);
        return response.getResponse();
    }
    public boolean convertEventToAccCo(Long leadId, Long accountId, Long contactId){
        ConvertLogEventToAccCoRequest request = ConvertLogEventToAccCoRequest.newBuilder()
                .setLeadId(leadId)
                .setAccountId(accountId)
                .setContactId(contactId)
                .build();
        ConvertLogEventToAccCoResponse response = stubUser.convertLogEventToAccCo(request);
        return response.getResponse();
    }

    public boolean createNotification (String userId, String content, Long linkedId,
                                       Long notificationType, List<String> listUser){
        CreateNotificationRequest.Builder requestBuilder = CreateNotificationRequest.newBuilder()
                .setUserId(userId)
                .setContent(content)
                .setLinkId(linkedId)
                .setNotificationType(notificationType);

        // Thêm từng phần tử từ listUser vào yêu cầu
        for (String user : listUser) {
            requestBuilder.addListUser(user);  // Sử dụng addListUser cho trường `repeated`
        }

        CreateNotificationResponse response = stubUser.createNotification(requestBuilder.build());
        return response.getResponse();
    }

    public AccountDtoProto getAccount (Long accountId){
        GetAccountRequest request = GetAccountRequest.newBuilder()
                .setAccountId(accountId)
                .build();
        GetAccountResponse response = stubAccount.getAccount(request);
        return response.getResponse();
    }

    public List<String> getUserRoles (String userId){
        GetUserRolesRequest request = GetUserRolesRequest.newBuilder()
                .setUserId(userId)
                .build();
        GetUserRolesResponse response = stubUser.getUserRoles(request);
        return response.getListRolesList();
    }

}
