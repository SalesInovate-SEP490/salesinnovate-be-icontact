package fpt.capstone.iContact.repository;

import fpt.capstone.iContact.model.Contact;
import fpt.capstone.iContact.model.ContactsUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContactsUserRepository extends JpaRepository<ContactsUser,Long>, JpaSpecificationExecutor<ContactsUser> {
}
