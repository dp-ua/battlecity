package com.codenjoy.dojo.battlecity.client.objects.implement;

import com.codenjoy.dojo.battlecity.client.objects.Basic;
import com.codenjoy.dojo.battlecity.client.objects.action.Destroy;
import com.codenjoy.dojo.battlecity.client.objects.action.Step;
import com.codenjoy.dojo.services.Point;

public class Wall extends Basic implements Destroy, Step {
    public Wall(Point point) {
        super(point);
    }
}
