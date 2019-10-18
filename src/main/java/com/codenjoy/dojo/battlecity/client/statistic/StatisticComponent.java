package com.codenjoy.dojo.battlecity.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
@AllArgsConstructor
public class Statistic {
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
