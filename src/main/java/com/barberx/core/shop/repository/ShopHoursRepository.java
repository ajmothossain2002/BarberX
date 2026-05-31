package com.barberx.core.shop.repository;

import com.barberx.core.shop.entity.ShopHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopHoursRepository extends JpaRepository<ShopHours, Long> {

    List<ShopHours> findByShopIdAndIsDeletedFalse(Long shopId);

    void deleteByShopId(Long shopId);
}
