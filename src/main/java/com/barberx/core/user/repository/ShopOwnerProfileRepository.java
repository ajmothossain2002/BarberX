package com.barberx.core.user.repository;

import com.barberx.core.user.entity.ShopOwnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopOwnerProfileRepository extends JpaRepository<ShopOwnerProfile, Long> {

    Optional<ShopOwnerProfile> findByUserIdAndDeletedFalse(Long userId);
}
