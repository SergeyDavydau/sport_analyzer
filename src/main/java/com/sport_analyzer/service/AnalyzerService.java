package com.sport_analyzer.service;

import com.sport_analyzer.model.Event;


import lombok.extern.log4j.Log4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Log4j
public class AnalyzerService {

    @Value("${analyze.url}")
    private String analyzeURL;

    public List<Event> getEventInformation() {

        try {
            //подключение к сайту
            Connection connection = Jsoup.connect(analyzeURL)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
            //Нод со всеми данными
            List<Element> titleElement = connection.get().select("table .highlights--item");
            //Парсим и формируем список событий
            return getParseEvent(titleElement);

        } catch (Exception e) {
            log.error("Parse date error", new RuntimeException("Planned"));
        }
        return null;
    }

    private List<Event> getParseEvent(List<Element> elementList) {

        List<Event> events = new ArrayList<>();

        for (Element element : elementList) {

            Event event = new Event();
            //Парсинг и назначение даты
            dateSetter(element, event);
            //Парсинг и назначение команд
            teamSetter(element, event);
            //Парсинг и назначение турнира и вида спорта
            tournamentAndKindSetter(element, event);
            //Парсинг и назначение ссылки
            urlEventSetter(element, event);

            events.add(event);
        }

        return events;
    }

    //Парсинг даты
    private void dateSetter(Element element, Event event) {

        String stringDate = element.select("td .time").textNodes().get(1).toString().trim();

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM HH:mm", Locale.US);
        try {
            event.setDate(sdf.parse(stringDate));// all done
        } catch (ParseException exception) {
            log.error("Parse date error", new RuntimeException("Planned"));
        }
    }

    private void teamSetter(Element element, Event event) {
        event.setTeamA(element.select("td .clubs span").textNodes().get(0).toString().trim());
        event.setTeamB(element.select("td .clubs span").textNodes().get(1).toString().trim());
    }

    private void tournamentAndKindSetter(Element element, Event event) {
        String rawData = element.select(".meta span").textNodes().toString().replaceAll("[\\[\\]]", "");
        String[] splitData = rawData.split("/");

        if (splitData.length == 3) {
            event.setTournament(splitData[2].trim());
            event.setKind(splitData[0].trim());
        }
    }

    private void urlEventSetter(Element element, Event event) {
        event.setUrl(element.select("a").attr("href").trim());
    }

}
