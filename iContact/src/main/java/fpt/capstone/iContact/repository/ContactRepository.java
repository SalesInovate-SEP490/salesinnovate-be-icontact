package fpt.capstone.iContact.repository;

import fpt.capstone.iContact.model.Contact;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact,Long>,JpaSpecificationExecutor<Contact> {
}
