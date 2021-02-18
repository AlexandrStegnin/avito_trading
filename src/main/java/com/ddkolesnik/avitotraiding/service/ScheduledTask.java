package com.ddkolesnik.avitotraiding.service;

import com.ddkolesnik.avitotraiding.utils.City;
import com.ddkolesnik.avitotraiding.utils.Company;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Alexandr Stegnin
 */

@Slf4j
@Component
public class ScheduledTask {

    private final AvitoTradingGrabberService tradingGrabberService;

    private final RadGrabberService radGrabberService;

    private final RtsGrabberService rtsGrabberService;

    public ScheduledTask(AvitoTradingGrabberService tradingGrabberService,
                         RadGrabberService radGrabberService,
                         RtsGrabberService rtsGrabberService) {
        this.tradingGrabberService = tradingGrabberService;
        this.radGrabberService = radGrabberService;
        this.rtsGrabberService = rtsGrabberService;
    }

    @Scheduled(cron = "${cron.expression.daily}")
    public void runDaily() {
        log.info("Начинаем сбор объявлений");
        int count = parse();
        log.info("Собрано лотов АВИТО: {}", count);
        int countRad = parseRad();
        log.info("Собрано лотов РАД: {}", countRad);
        int countRts = parseRts();
        log.info("Собрано лотов РТС: {}", countRts);
        log.info("Завершено, собрано объявлений [{} шт]", count + countRad + countRts);
    }

    private int parse() {
        int count = tradingGrabberService.parse(Company.SBER, City.TYUMEN);
        count += tradingGrabberService.parse(Company.SBER, City.EKB);
        count += tradingGrabberService.parse(Company.OPENING, City.TYUMEN);
        count += tradingGrabberService.parse(Company.OPENING, City.EKB);
        count += tradingGrabberService.parse(Company.RT, City.TYUMEN);
        count += tradingGrabberService.parse(Company.RT, City.EKB);
        return count;
    }

    private int parseRad() {
        int count = radGrabberService.parse(null, City.TYUMEN);
        count += radGrabberService.parse(null, City.EKB);
        return count;
    }

    private int parseRts() {
        return rtsGrabberService.parse();
    }

}
