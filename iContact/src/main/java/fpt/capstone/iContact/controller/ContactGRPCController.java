package fpt.capstone.iContact.controller;

import fpt.capstone.iContact.dto.response.ContactResponse;
import fpt.capstone.iContact.service.ContactService;
import fpt.capstone.proto.account.AccountDtoProto;
import fpt.capstone.proto.account.GetAccountResponse;
import fpt.capstone.proto.contact.*;
import fpt.capstone.proto.user.ConvertLogCallToOppResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class ContactGRPCController extends ContactServiceGrpc.ContactServiceImplBase {
    @Autowired
    ContactService contactService;

    @Override
    public void getContact(GetContactRequest request, StreamObserver<GetContactResponse> responseObserver){
        long contactId = request.getContactId();
        ContactResponse contactResponse = contactService.getContactDetail(contactId);
        try {
            GetContactResponse getContactResponse ;

            if(contactResponse != null){
                ContactDtoProto proto = ContactDtoProto.newBuilder()
                        .setContactId(contactResponse.getContactId()==null?0L:contactResponse.getContactId())
                        .setAccountId(contactResponse.getAccountId()==null?0L:contactResponse.getAccountId())
                        .setFirstName(contactResponse.getFirstName()==null?"":contactResponse.getFirstName())
                        .setLastName(contactResponse.getLastName()==null?"":contactResponse.getLastName())
                        .setMiddleName(contactResponse.getMiddleName()==null?"":contactResponse.getMiddleName())
                        .setTitle(contactResponse.getTitle()==null?"":contactResponse.getTitle())
                        .setEmail(contactResponse.getEmail()==null?"":contactResponse.getEmail())
                        .setPhone(contactResponse.getPhone()==null?"":contactResponse.getPhone())
                        .setDepartment(contactResponse.getDepartment()==null?"":contactResponse.getDepartment())
                        .setMobile(contactResponse.getMobile()==null?"":contactResponse.getMobile())
                        .setFax(contactResponse.getFax()==null?"":contactResponse.getFax())
                        .setIsDelete(contactResponse.getIsDeleted()==null?0:contactResponse.getIsDeleted())
                        .build();
                getContactResponse = GetContactResponse.newBuilder()
                        .setResponse(proto)
                        .build();
            }else{
                getContactResponse = GetContactResponse.getDefaultInstance();
            }
            responseObserver.onNext(getContactResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }

    @Override
    public void assignUserFollowingAccount(AssignUserFollowingAccountRequest request,
                                           StreamObserver<AssignUserFollowingAccountResponse> responseObserver){
        long accountId = request.getAccountId();
        List<String> listUser = request.getListUserList();
        boolean checkAssign = contactService.assignUserFollowingAccount(accountId,listUser);
        try {
            AssignUserFollowingAccountResponse response = AssignUserFollowingAccountResponse.newBuilder()
                    .setResponse(checkAssign)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
    }
}
