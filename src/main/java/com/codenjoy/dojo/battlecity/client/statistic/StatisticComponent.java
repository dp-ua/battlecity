package com.codenjoy.dojo.battlecity.client.statistic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
@AllArgsConstructor
public class StatisticComponent {
    long start;
    long finish;
    String name;

    public long duration(){
        return finish-start;
    }

    @Override
    public String toString() {
        return duration()+"ms [" +name+"]";
    }
}
