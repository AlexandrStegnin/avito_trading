package com.ddkolesnik.avitotraiding.service;

import com.codeborne.selenide.WebDriverRunner;
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

    private final Fund72GrabberService fund72GrabberService;

    private final SberAstGrabberService sberAstGrabberService;

    public ScheduledTask(AvitoTradingGrabberService tradingGrabberService,
                         RadGrabberService radGrabberService,
                         RtsGrabberService rtsGrabberService,
                         Fund72GrabberService fund72GrabberService,
                         SberAstGrabberService sberAstGrabberService) {
        this.tradingGrabberService = tradingGrabberService;
        this.radGrabberService = radGrabberService;
        this.rtsGrabberService = rtsGrabberService;
        this.fund72GrabberService = fund72GrabberService;
        this.sberAstGrabberService = sberAstGrabberService;
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
        int countFito = parseFito();
        log.info("Собрано лотов ФИТО: {}", countFito);
        int countSberAst = parseSberAst();
        log.info("Собрано лотов Сбер АСТ: {}", countSberAst);
        log.info("Завершено, собрано лотов [{} шт]", count + countRad + countRts + countFito + countSberAst);
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
        int count = rtsGrabberService.parse(City.TYUMEN);
        count += rtsGrabberService.parse(City.EKB);
        return count;
    }

    private int parseFito() {
        return fund72GrabberService.parse(City.TYUMEN);
    }

    private int parseSberAst() {
        int count = sberAstGrabberService.parse(City.TYUMEN);
        count += sberAstGrabberService.parse(City.EKB);
        WebDriverRunner.closeWebDriver();
        return count;
    }
}
