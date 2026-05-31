package com.barberx.core.branch.repository;

import com.barberx.core.branch.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {

    List<Branch> findByShopIdAndDeletedFalse(Long shopId);

    Optional<Branch> findByIdAndDeletedFalse(Long id);
}
