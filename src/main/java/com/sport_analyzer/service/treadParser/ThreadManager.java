package com.sport_analyzer.service.treadParser;

import com.sport_analyzer.service.AnalyzerService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Log4j
public class ThreadManager {

    //Запуск потоков
    public void runThread() {

        //Создаем фиксированное количество потоков
        ExecutorService executorService = Executors.newFixedThreadPool(AnalyzerService.URL_FOR_ANALYZE.size());

        //назначаем потоки
        for (String url : AnalyzerService.URL_FOR_ANALYZE) {
            executorService.execute(new SportParser(url));
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

}
