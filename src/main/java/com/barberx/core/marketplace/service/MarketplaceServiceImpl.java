package com.barberx.core.marketplace.service;

import com.barberx.common.exception.CustomException;
import com.barberx.core.shop.dto.response.ShopResponse;
import com.barberx.core.shop.entity.Shop;
import com.barberx.core.shop.enums.VerificationStatus;
import com.barberx.core.shop.mapper.ShopMapper;
import com.barberx.core.shop.repository.ShopRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation handling marketplace search and filtering queries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceServiceImpl implements MarketplaceService {

    private final ShopRepository shopRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ShopResponse> searchShops(String search, String businessType, String city, Pageable pageable) {
        log.debug("Searching marketplace shops: search='{}', type='{}', city='{}'", search, businessType, city);

        Specification<Shop> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. MUST be verified APPROVED
            predicates.add(cb.equal(root.get("verificationStatus"), VerificationStatus.APPROVED));

            // 2. MUST NOT be soft-deleted
            predicates.add(cb.equal(root.get("deleted"), false));

            // 3. Search query matches name, address, city, or state
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate addressMatch = cb.like(cb.lower(root.get("address")), pattern);
                Predicate cityMatch = cb.like(cb.lower(root.get("city")), pattern);
                Predicate stateMatch = cb.like(cb.lower(root.get("state")), pattern);
                predicates.add(cb.or(nameMatch, addressMatch, cityMatch, stateMatch));
            }

            // 4. Business Type filter
            if (businessType != null && !businessType.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("businessType")), businessType.trim().toLowerCase()));
            }

            // 5. City filter
            if (city != null && !city.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return shopRepository.findAll(spec, pageable)
                .map(ShopMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopDetails(Long id) {
        log.debug("Fetching public details for shop: {}", id);
        Shop shop = shopRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Shop not found with ID: " + id, HttpStatus.NOT_FOUND));

        if (shop.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new CustomException("This shop is not publicly active", HttpStatus.FORBIDDEN);
        }

        // Return shop details with its schedule/hours
        return ShopMapper.toResponse(shop, true);
    }
}
