package com.codenjoy.dojo.battlecity.client.objects.implement;

import com.codenjoy.dojo.battlecity.client.objects.Basic;
import com.codenjoy.dojo.battlecity.client.objects.action.Step;
import com.codenjoy.dojo.services.Point;

public class Free extends Basic implements Step {
    public Free(Point point) {
        super(point);
    }

    @Override
    public String toString() {
        return "Free{" +getPoint()+"}";
    }
}
