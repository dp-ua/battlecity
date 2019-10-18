package com.codenjoy.dojo.battlecity.client.statistic;

import java.util.LinkedList;
import java.util.List;

public class StatisticHolder {
    public static final String LINE_END = "/n";
    StatisticComponent main;
    List<StatisticComponent> other;

    public String getOther() {
        StringBuilder result = new StringBuilder();
        for (StatisticComponent statisticComponent : other) {
            result.append(statisticComponent).append(LINE_END);
        }
        return result.toString();
    }
    
    public String show(long durationExcess) {
        StringBuilder result = new StringBuilder();
        String other = getOther();
        if (main == null) result.append("Main not set").append(LINE_END);
        else {
            result.append(main.toString()).append(LINE_END);
        }
        return result.toString();
    }


    public StatisticHolder() {
        clear();
    }

    public void clear() {
        main = null;
        other = new LinkedList<>();
    }


}
