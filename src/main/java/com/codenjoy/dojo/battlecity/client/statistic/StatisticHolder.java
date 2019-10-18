package com.codenjoy.dojo.battlecity.client.statistic;

import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

public class StatisticHolder {

    public static final String LINE_END = "\n";
    private static StatisticHolder mInstance;
    @Setter
    StatisticComponent main;
    List<StatisticComponent> other;

    public void addOther(StatisticComponent component) {
        other.add(component);
    }

    public static StatisticHolder getInstance() {
        if (mInstance == null) {
            mInstance = new StatisticHolder();
        }
        return mInstance;
    }

    public String getOther() {
        StringBuilder result = new StringBuilder();
        for (StatisticComponent statisticComponent : other) {
            result.append(statisticComponent).append(LINE_END);
        }
        return result.toString();
    }

    public String getStringToShow(long durationExcess) {
        StringBuilder result = new StringBuilder();
        String other = getOther();
        if (main == null) result.append("Main not set").append(LINE_END)
                .append(other).append(LINE_END);
        else {
            result.append(main.toString()).append(LINE_END);
            if (main.duration() > durationExcess) result.append(other).append(LINE_END);
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
