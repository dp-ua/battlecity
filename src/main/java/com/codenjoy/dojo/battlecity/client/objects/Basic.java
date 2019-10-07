package com.codenjoy.dojo.battlecity.client.objects;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Basic {
    @Getter
    Point point;

    @Getter
    @Setter
    Direction direction;
    @Getter
    @Setter
    int power = -1;

    List<Basic> links;

    public Basic(Point point) {
        this.point = point;
        links = new ArrayList<>();
    }

    public int getExtraMove() {
        return power == -1 ? 0 : power;
    }
}
