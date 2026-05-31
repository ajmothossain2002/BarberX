package com.barberx.core.branch.repository;

import com.barberx.core.branch.entity.BranchHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchHoursRepository extends JpaRepository<BranchHours, Long> {

    List<BranchHours> findByBranchIdAndIsDeletedFalse(Long branchId);

    void deleteByBranchId(Long branchId);
}
