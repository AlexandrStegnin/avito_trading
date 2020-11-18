package com.ddkolesnik.avitotraiding.service;

import com.ddkolesnik.avitotraiding.model.TradingEntity;
import com.ddkolesnik.avitotraiding.repository.TradingRepository;
import org.springframework.stereotype.Service;

/**
 * @author Alexandr Stegnin
 */

@Service
public class TradingService {

    private final TradingRepository tradingRepository;

    public TradingService(TradingRepository tradingRepository) {
        this.tradingRepository = tradingRepository;
    }

    public TradingEntity create(TradingEntity tradingEntity) {
        return tradingRepository.save(tradingEntity);
    }

}
