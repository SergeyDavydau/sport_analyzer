package com.sport_analyzer.service.treadParser;

import com.sport_analyzer.model.Event;
import com.sport_analyzer.service.AnalyzerService;
import lombok.extern.log4j.Log4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.thymeleaf.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;


@Log4j
public class SportParser implements Runnable {

    //Очередь url для анализа
    private final Queue<String> urlForAnalyze = new LinkedList<>();
    //флаг активности анализа
    private boolean runAnalyze;
    //часть конечной ссылки для получения информации об матчах
    private final String finalAnalyzePartHref = "(.*)competition/open(.*)";

    public SportParser(String url) {
        urlForAnalyze.add(url);
        runAnalyze = true;
    }

    @Override
    public void run() {
        //покуда не достигнута конечная ссылка по которой уже можно получит информацию о матчах
        while (runAnalyze) {
            analyzeKindOfSportBranch();
        }
        startParseData();
    }
    /*Проходиться по очереди url и загружает в очередь новые пулы адресов, пока в очереди не остануться
    конечные адреса, по которым уже будет проведен маппинг в обьекты
     */
    private void analyzeKindOfSportBranch() {
        int sizePull = urlForAnalyze.size();
        for (int i = 0; i < sizePull; ++i) {
            urlForAnalyze.addAll(
                    AnalyzerService.getKindOfSportURL(urlForAnalyze.poll())
            );
        }
        if (urlForAnalyze.size() > 0 && urlForAnalyze.peek().matches(finalAnalyzePartHref) || urlForAnalyze.size() == 0) {
            runAnalyze = false;
        }

    }

    //Добавляет данные в общую карту событий(групировка по типу спорта)
    public void unionData(List<Event> events) {
        try {
            String kindOfSport = events.get(0).getKind();
            if (AnalyzerService.ALL_EVENTS.containsKey(kindOfSport)) {
                AnalyzerService.ALL_EVENTS.get(kindOfSport).addAll(events);
            } else {
                AnalyzerService.ALL_EVENTS.put(kindOfSport, events);
            }
        } catch (Exception e) {
            log.error("Add date error ", new RuntimeException("Planned"));
        }

    }
    //Проходиться по очереди url, где уже остались только ссылки на списки событий и парсит в нужные обьекты
    private void startParseData() {
        while (!urlForAnalyze.isEmpty()) {
            Document document = AnalyzerService.getDocumentByUrl(urlForAnalyze.poll());
            getParseEvent(document.select("table .highlights--item"));
        }
    }
    //Парсинг данных
    private void getParseEvent(List<Element> elementList) {

        List<Event> events = new ArrayList<>();

        for (Element element : elementList) {
            //только те события где есть 2 команды
            if (!isTwoCommand(element)) {
                continue;
            }

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
        //Добавляем в общую карту
        if (!events.isEmpty()) {
            unionData(events);
        }
    }
    //Проверка наличия 2 комманд
    private boolean isTwoCommand(Element element) {
        String commandA = "";
        String commandB = "";
        try {
            commandA = element.select("td .clubs span").textNodes().get(0).toString().trim();
            commandB = element.select("td .clubs span").textNodes().get(1).toString().trim();
        } catch (Exception e) {
            return false;
        }
        return !StringUtils.isEmpty(commandA) && !StringUtils.isEmpty(commandB);
    }

    //Парсинг даты
    private void dateSetter(Element element, Event event) {
        try {
            String stringDate = element.select("td .time").textNodes().get(1).toString().trim();

            SimpleDateFormat sdf = new SimpleDateFormat("d MMM HH:mm", Locale.US);

            event.setDate(sdf.parse(stringDate));// all done
        } catch (Exception exception) {
            log.error("Parse date error ", new RuntimeException("Planned"));
        }
    }

    private void teamSetter(Element element, Event event) {
        try {
            event.setTeamA(element.select("td .clubs span").textNodes().get(0).toString().trim());
            event.setTeamB(element.select("td .clubs span").textNodes().get(1).toString().trim());
        } catch (Exception e) {
            log.error("Parse command error ", new RuntimeException("Planned"));
        }

    }

    private void tournamentAndKindSetter(Element element, Event event) {
        try {
            String rawData = element.select(".meta span").textNodes().toString().replaceAll("[\\[\\]]", "");
            String[] splitData = rawData.split("/");

            if (splitData.length == 3) {
                event.setTournament(splitData[2].trim());
                event.setKind(splitData[0].trim());
            }
        } catch (Exception e) {
            log.error("Parse kind error ", new RuntimeException("Planned"));
        }

    }

    private void urlEventSetter(Element element, Event event) {
        try {
            event.setUrl(element.select("a").attr("href").trim());
        } catch (Exception e) {
            log.error("Parse url error ", new RuntimeException("Planned"));
        }

    }


}
