package fpt.capstone.iContact.repository;

import fpt.capstone.iContact.model.CoOppRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CoOppRelationRepository extends JpaRepository<CoOppRelation,Long>, JpaSpecificationExecutor<CoOppRelation> {
}
