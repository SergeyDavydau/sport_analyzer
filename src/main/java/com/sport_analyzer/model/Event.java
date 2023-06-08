package com.sport_analyzer.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    Date date;

    String teamA;

    String teamB;

    String tournament;

    String kind;

    String url;
}
