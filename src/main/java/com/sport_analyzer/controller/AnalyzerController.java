package com.sport_analyzer.controller;

import com.sport_analyzer.service.AnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AnalyzerController {

    @Autowired
    AnalyzerService analyzerService;

    @GetMapping("/")
    public String mainView(Model model) {
        //запуск анализа
        analyzerService.loadEventInformation();

        //Отправляем мап с событиями
        model.addAttribute("eventMap", AnalyzerService.ALL_EVENTS);
        return "mainView";

    }


}
