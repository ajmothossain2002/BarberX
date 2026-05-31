package com.barberx.core.branch.controller;

import com.barberx.common.constants.AppConstants;
import com.barberx.common.response.ApiResponse;
import com.barberx.core.branch.dto.request.BranchHoursRequest;
import com.barberx.core.branch.dto.request.CreateBranchRequest;
import com.barberx.core.branch.dto.request.UpdateBranchRequest;
import com.barberx.core.branch.dto.response.BranchHoursResponse;
import com.barberx.core.branch.dto.response.BranchResponse;
import com.barberx.core.branch.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller exposing endpoints for managing branches under a shop.
 * Standard ownership checks are performed at the service layer.
 */
@RestController
@RequestMapping(AppConstants.API_V1 + "/shops/{shopId}/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "APIs for managing physical branches of a shop")
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Create a new branch", description = "Creates a new branch for the specified shop. Requires OWNER or ADMIN role.")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @PathVariable Long shopId,
            @Valid @RequestBody CreateBranchRequest request) {
        BranchResponse branch = branchService.createBranch(shopId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(branch, "Branch created successfully"));
    }

    @GetMapping
    @Operation(summary = "List branches", description = "Returns all active branches for the specified shop.")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranchesByShopId(
            @PathVariable Long shopId) {
        List<BranchResponse> branches = branchService.getBranchesByShopId(shopId);
        return ResponseEntity.ok(ApiResponse.success(branches, "Branches retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID", description = "Returns details of the specified branch, including operating hours.")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(
            @PathVariable Long shopId,
            @PathVariable Long id) {
        BranchResponse branch = branchService.getBranchById(shopId, id);
        return ResponseEntity.ok(ApiResponse.success(branch, "Branch retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update branch", description = "Updates branch details. Requires OWNER or ADMIN role.")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBranchRequest request) {
        BranchResponse branch = branchService.updateBranch(shopId, id, request);
        return ResponseEntity.ok(ApiResponse.success(branch, "Branch updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Delete branch", description = "Soft-deletes the specified branch. Requires OWNER or ADMIN role.")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(
            @PathVariable Long shopId,
            @PathVariable Long id) {
        branchService.deleteBranch(shopId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Branch deleted successfully"));
    }

    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Toggle branch active status", description = "Enables or disables the branch. Requires OWNER or ADMIN role.")
    public ResponseEntity<ApiResponse<BranchResponse>> toggleBranchActive(
            @PathVariable Long shopId,
            @PathVariable Long id) {
        BranchResponse branch = branchService.toggleBranchActive(shopId, id);
        return ResponseEntity.ok(ApiResponse.success(branch, "Branch active status updated successfully"));
    }

    @PutMapping("/{id}/hours")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update branch hours", description = "Full replace of operating hours for the branch. Requires OWNER or ADMIN role.")
    public ResponseEntity<ApiResponse<List<BranchHoursResponse>>> updateBranchHours(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @Valid @RequestBody BranchHoursRequest request) {
        List<BranchHoursResponse> hours = branchService.updateBranchHours(shopId, id, request);
        return ResponseEntity.ok(ApiResponse.success(hours, "Branch hours updated successfully"));
    }

    @GetMapping("/{id}/hours")
    @Operation(summary = "Get branch hours", description = "Returns operating hours for the branch.")
    public ResponseEntity<ApiResponse<List<BranchHoursResponse>>> getBranchHours(
            @PathVariable Long shopId,
            @PathVariable Long id) {
        List<BranchHoursResponse> hours = branchService.getBranchHours(shopId, id);
        return ResponseEntity.ok(ApiResponse.success(hours, "Branch hours retrieved successfully"));
    }
}
