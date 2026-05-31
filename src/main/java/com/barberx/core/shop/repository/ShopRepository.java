package com.barberx.core.shop.repository;

import com.barberx.core.shop.entity.Shop;
import com.barberx.core.shop.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long>, JpaSpecificationExecutor<Shop> {

    List<Shop> findByOwnerIdAndDeletedFalse(Long ownerId);

    Optional<Shop> findByIdAndDeletedFalse(Long id);

    Page<Shop> findAllByDeletedFalse(Pageable pageable);

    Page<Shop> findAllByVerificationStatusAndDeletedFalse(VerificationStatus status, Pageable pageable);
}
