package com.sport_analyzer.service;

import com.sport_analyzer.model.Event;


import com.sport_analyzer.service.treadParser.ThreadManager;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j
@Getter
public class AnalyzerService {

    @Autowired
    ThreadManager threadManager;
    @Value("${analyze.url}")
    public String analyzeURL;
    //Все события который будут отправлены на отображение
    public static final Map<String, List<Event>> ALL_EVENTS = new ConcurrentHashMap<>();
    //Очередь ссылок на виды спорта, по которым будут запущены потоки;
    public static final List<String> URL_FOR_ANALYZE = new ArrayList<>();
    //Шаблон для поиска нужных ссылок
    public static final String REGEX_PARTS_OF_URL = "((.*)sports/categories(.*))|((.*)sports/leagues(.*))|((.*)competition/open(.*))";

    public void loadEventInformation() {
        loadEventInformation(analyzeURL);
    }

    public void loadEventInformation(String url) {
        //получаем лист url для анадиза
        URL_FOR_ANALYZE.addAll(getKindOfSportURL(url));
        //Запускаем паралельные запросы на поиск информации и парсинг данных
        threadManager.runThread();
    }

    //Возвращает список ссылок находящихся по переданному url
    public static List<String> getKindOfSportURL(String actionUrl) {

        List<TextNode> startElement = getDocumentByUrl(actionUrl).select(".std-pd, .vh-40").select("a").textNodes();

        Set<String> urlList = new LinkedHashSet<>();

        String url = "";
        for (TextNode element : startElement) {
            url = Optional.of(element.parent().attr("href")).orElse("");
            if (url.matches(REGEX_PARTS_OF_URL)) {
                urlList.add(url);
            }
        }

        return new ArrayList<>(urlList);
    }

    public static Document getDocumentByUrl(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .get();
        } catch (IOException exception) {
            log.error("Error get data by url = " + url, new RuntimeException("Planned"));
        }
        return null;
    }

}
