package com.ddkolesnik.avitotraiding.repository;

import com.ddkolesnik.avitotraiding.model.TradingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Alexandr Stegnin
 */

@Repository
public interface TradingRepository extends JpaRepository<TradingEntity, Long> {

    boolean existsByUrl(String url);

}
