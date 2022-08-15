package com.forestik;

import com.forestik.service.portfolio.PortfolioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Component
public class PortfolioRunner {

    private final List<PortfolioService> portfolioServices;

    public PortfolioRunner(
            final List<PortfolioService> portfolioServices,
            final @Value("${portfolio.scheduled.period}") long period,
            final @Value("${portfolio.scheduled.initial.delay}") long initialDelay
    ){
        this.portfolioServices = portfolioServices;
        log.info("Portsolio scheduled: every {} millis after {} millis of initial delay", period, initialDelay);
        for (PortfolioService portfolioService : portfolioServices){
            log.info("Using {}", portfolioService.getDescription());
        }
    }

    @Scheduled(
            fixedRateString = "${portfolio.scheduled.period}",
            initialDelayString = "${portfolio.scheduled.initial.delay}"
    )
    public void run() {
        synchronized (PortfolioRunner.class){
            log.info("Portfolio cycle start");
            try {
                for (PortfolioService portfolioService : portfolioServices) {
                    portfolioService.processData();
                }
            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e){
                log.warn(e.getMessage());
                e.printStackTrace();
            }
            log.info("Portfolio cycle complete");
        }
    }

}
