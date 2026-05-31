package com.barberx.core.branch.service;

import com.barberx.common.exception.CustomException;
import com.barberx.common.util.UserContextUtil;
import com.barberx.core.branch.dto.request.BranchHoursRequest;
import com.barberx.core.branch.dto.request.CreateBranchRequest;
import com.barberx.core.branch.dto.request.UpdateBranchRequest;
import com.barberx.core.branch.dto.response.BranchHoursResponse;
import com.barberx.core.branch.dto.response.BranchResponse;
import com.barberx.core.branch.entity.Branch;
import com.barberx.core.branch.entity.BranchHours;
import com.barberx.core.branch.mapper.BranchMapper;
import com.barberx.core.branch.repository.BranchHoursRepository;
import com.barberx.core.branch.repository.BranchRepository;
import com.barberx.core.shop.entity.Shop;
import com.barberx.core.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for branch management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchHoursRepository branchHoursRepository;
    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public BranchResponse createBranch(Long shopId, CreateBranchRequest request) {
        log.info("Creating branch '{}' for shop {}", request.getName(), shopId);
        Shop shop = findActiveShopOrThrow(shopId);
        enforceOwnership(shop);

        Branch branch = BranchMapper.toEntity(request, shop);
        branch = branchRepository.save(branch);

        log.info("Branch created successfully with ID {}", branch.getId());
        return BranchMapper.toResponse(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getBranchesByShopId(Long shopId) {
        log.debug("Fetching branches for shop {}", shopId);
        findActiveShopOrThrow(shopId);

        return branchRepository.findByShopIdAndDeletedFalse(shopId).stream()
                .map(BranchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Long shopId, Long id) {
        log.debug("Fetching branch {} for shop {}", id, shopId);
        findActiveShopOrThrow(shopId);
        Branch branch = findActiveBranchOrThrow(id);
        validateBranchBelongsToShop(branch, shopId);

        return BranchMapper.toResponse(branch, true);
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long shopId, Long id, UpdateBranchRequest request) {
        log.info("Updating branch {} for shop {}", id, shopId);
        Shop shop = findActiveShopOrThrow(shopId);
        enforceOwnership(shop);

        Branch branch = findActiveBranchOrThrow(id);
        validateBranchBelongsToShop(branch, shopId);

        BranchMapper.updateEntity(branch, request);
        branch = branchRepository.save(branch);

        log.info("Branch {} updated successfully", id);
        return BranchMapper.toResponse(branch);
    }

    @Override
    @Transactional
    public void deleteBranch(Long shopId, Long id) {
        log.info("Soft-deleting branch {} for shop {}", id, shopId);
        Shop shop = findActiveShopOrThrow(shopId);
        enforceOwnership(shop);

        Branch branch = findActiveBranchOrThrow(id);
        validateBranchBelongsToShop(branch, shopId);

        String currentUserEmail = UserContextUtil.getCurrentUserEmail();
        branch.softDelete(currentUserEmail);
        branchRepository.save(branch);

        log.info("Branch {} soft-deleted successfully", id);
    }

    @Override
    @Transactional
    public BranchResponse toggleBranchActive(Long shopId, Long id) {
        log.info("Toggling active status for branch {} in shop {}", id, shopId);
        Shop shop = findActiveShopOrThrow(shopId);
        enforceOwnership(shop);

        Branch branch = findActiveBranchOrThrow(id);
        validateBranchBelongsToShop(branch, shopId);

        branch.setActive(!branch.isActive());
        branch = branchRepository.save(branch);

        log.info("Branch {} active status toggled to {}", id, branch.isActive());
        return BranchMapper.toResponse(branch);
    }

    @Override
    @Transactional
    public List<BranchHoursResponse> updateBranchHours(Long shopId, Long branchId, BranchHoursRequest request) {
        log.info("Replacing operating hours for branch {} in shop {}", branchId, shopId);
        Shop shop = findActiveShopOrThrow(shopId);
        enforceOwnership(shop);

        Branch branch = findActiveBranchOrThrow(branchId);
        validateBranchBelongsToShop(branch, shopId);

        // Clear existing hours (orphanRemoval is active on the cascade relationship)
        branch.getBranchHours().clear();

        List<BranchHours> newHours = new ArrayList<>();
        for (BranchHoursRequest.BranchHourEntry entry : request.getHours()) {
            BranchHours hours = BranchHours.builder()
                    .branch(branch)
                    .dayOfWeek(entry.getDayOfWeek().toUpperCase())
                    .openingTime(parseTime(entry.getOpeningTime()))
                    .closingTime(parseTime(entry.getClosingTime()))
                    .isHoliday(entry.isHoliday())
                    .build();
            newHours.add(hours);
        }

        branch.getBranchHours().addAll(newHours);
        branchRepository.save(branch);

        log.info("Branch hours updated successfully for branch {}", branchId);
        return newHours.stream()
                .map(BranchMapper::toHoursResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchHoursResponse> getBranchHours(Long shopId, Long branchId) {
        log.debug("Fetching operating hours for branch {} in shop {}", branchId, shopId);
        findActiveShopOrThrow(shopId);
        Branch branch = findActiveBranchOrThrow(branchId);
        validateBranchBelongsToShop(branch, shopId);

        return branchHoursRepository.findByBranchIdAndIsDeletedFalse(branchId).stream()
                .map(BranchMapper::toHoursResponse)
                .toList();
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    private Shop findActiveShopOrThrow(Long id) {
        return shopRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Shop not found with ID: " + id, HttpStatus.NOT_FOUND));
    }

    private Branch findActiveBranchOrThrow(Long id) {
        return branchRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Branch not found with ID: " + id, HttpStatus.NOT_FOUND));
    }

    private void validateBranchBelongsToShop(Branch branch, Long shopId) {
        if (!branch.getShop().getId().equals(shopId)) {
            throw new CustomException("Branch does not belong to this shop", HttpStatus.BAD_REQUEST);
        }
    }

    private void enforceOwnership(Shop shop) {
        var context = UserContextUtil.getContext();
        if (context == null || context.getUserId() == null) {
            throw new CustomException("Authentication required", HttpStatus.UNAUTHORIZED);
        }

        boolean isAdmin = context.getRoles() != null &&
                (context.getRoles().contains("ROLE_ADMIN") || context.getRoles().contains("ADMIN"));

        if (!isAdmin && !shop.getOwner().getId().equals(context.getUserId())) {
            throw new CustomException("You do not have permission to modify this shop", HttpStatus.FORBIDDEN);
        }
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            throw new CustomException("Invalid time format: " + time + ". Expected HH:mm", HttpStatus.BAD_REQUEST);
        }
    }
}
