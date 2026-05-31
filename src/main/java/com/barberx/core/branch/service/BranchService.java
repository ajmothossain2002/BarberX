package com.barberx.core.branch.service;

import com.barberx.core.branch.dto.request.BranchHoursRequest;
import com.barberx.core.branch.dto.request.CreateBranchRequest;
import com.barberx.core.branch.dto.request.UpdateBranchRequest;
import com.barberx.core.branch.dto.response.BranchHoursResponse;
import com.barberx.core.branch.dto.response.BranchResponse;

import java.util.List;

/**
 * Service interface for branch management operations.
 */
public interface BranchService {

    BranchResponse createBranch(Long shopId, CreateBranchRequest request);

    List<BranchResponse> getBranchesByShopId(Long shopId);

    BranchResponse getBranchById(Long shopId, Long id);

    BranchResponse updateBranch(Long shopId, Long id, UpdateBranchRequest request);

    void deleteBranch(Long shopId, Long id);

    BranchResponse toggleBranchActive(Long shopId, Long id);

    List<BranchHoursResponse> updateBranchHours(Long shopId, Long branchId, BranchHoursRequest request);

    List<BranchHoursResponse> getBranchHours(Long shopId, Long branchId);
}
