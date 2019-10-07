package com.codenjoy.dojo.battlecity.client.objects;

import com.codenjoy.dojo.battlecity.client.objects.implement.Wall;
import com.codenjoy.dojo.battlecity.client.objects.implement.Free;
import com.codenjoy.dojo.battlecity.model.Elements;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;

import static com.codenjoy.dojo.services.Direction.STOP;

public class ObjectDetector {
    public Basic getObject(Point point, Elements element) {
        Basic obj = new Basic(point);
        int power = element.getPower();
        Direction direction = STOP;

        switch (element) {
            case BANG:
            case NONE:
                obj = new Free(point);
                break;
            case BATTLE_WALL:
                power = 100;
            case CONSTRUCTION:
            case CONSTRUCTION_DESTROYED_DOWN:
            case CONSTRUCTION_DESTROYED_UP:
            case CONSTRUCTION_DESTROYED_LEFT:
            case CONSTRUCTION_DESTROYED_RIGHT:
            case CONSTRUCTION_DESTROYED_DOWN_TWICE:
            case CONSTRUCTION_DESTROYED_UP_TWICE:
            case CONSTRUCTION_DESTROYED_LEFT_TWICE:
            case CONSTRUCTION_DESTROYED_RIGHT_TWICE:
            case CONSTRUCTION_DESTROYED_LEFT_RIGHT:
            case CONSTRUCTION_DESTROYED_UP_DOWN:
            case CONSTRUCTION_DESTROYED_UP_LEFT:
            case CONSTRUCTION_DESTROYED_RIGHT_UP:
            case CONSTRUCTION_DESTROYED_DOWN_LEFT:
            case CONSTRUCTION_DESTROYED_DOWN_RIGHT:
            case CONSTRUCTION_DESTROYED:
                obj = new Wall(point);
                break;
            case BULLET:
                break;
            case TANK_UP:
            case TANK_RIGHT:
            case TANK_DOWN:
            case TANK_LEFT:
                obj=new Free(point);
                break;
            case OTHER_TANK_UP:
                break;
            case OTHER_TANK_RIGHT:
                break;
            case OTHER_TANK_DOWN:
                break;
            case OTHER_TANK_LEFT:
                break;
            case AI_TANK_UP:
                break;
            case AI_TANK_RIGHT:
                break;
            case AI_TANK_DOWN:
                break;
            case AI_TANK_LEFT:
                break;
        }

        obj.setDirection(direction);
        obj.setPower(power);
        return obj;
    }
}
