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

    public ScheduledTask(AvitoTradingGrabberService tradingGrabberService) {
        this.tradingGrabberService = tradingGrabberService;
    }

    /*
    Для ежедневного запуска:
    1. Получаем максимальную дату публикации из базы данных
    2. Проверяем первые 3 страницы объявлений
    3. Собираем новые объявления
     */
    @Scheduled(cron = "${cron.expression.daily}")
    public void runDaily() {
        log.info("Начинаем ЕЖЕДНЕВНЫЙ сбор объявлений");
        int count = parse();
        log.info("Завершено, собрано объявлений [{} шт]", count);
    }

    private int parse() {
        int count = tradingGrabberService.parse(Company.SBER.getSystemName(), City.TYUMEN.getName());
        count += tradingGrabberService.parse(Company.SBER.getSystemName(), City.EKB.getName());
        count += tradingGrabberService.parse(Company.OPENING.getSystemName(), City.TYUMEN.getName());
        count += tradingGrabberService.parse(Company.OPENING.getSystemName(), City.EKB.getName());
        count += tradingGrabberService.parse(Company.RT.getSystemName(), City.TYUMEN.getName());
        count += tradingGrabberService.parse(Company.RT.getSystemName(), City.EKB.getName());
        return count;
    }

}
