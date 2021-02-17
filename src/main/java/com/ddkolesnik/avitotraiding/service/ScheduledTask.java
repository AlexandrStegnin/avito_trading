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

    public ScheduledTask(AvitoTradingGrabberService tradingGrabberService,
                         RadGrabberService radGrabberService) {
        this.tradingGrabberService = tradingGrabberService;
        this.radGrabberService = radGrabberService;
    }

    /*
    Для ежедневного запуска:
    1. Получаем максимальную дату публикации из базы данных
    2. Проверяем первые 3 страницы объявлений
    3. Собираем новые объявления
     */
    @Scheduled(cron = "${cron.expression.daily}")
    public void runDaily() {
        log.info("Начинаем сбор объявлений");
        int count = parse();
        count += parseRad();
        log.info("Завершено, собрано объявлений [{} шт]", count);
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

}
