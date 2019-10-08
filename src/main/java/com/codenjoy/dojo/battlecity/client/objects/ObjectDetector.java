package com.codenjoy.dojo.battlecity.client.objects;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 - 2019 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.battlecity.client.objects.implement.*;
import com.codenjoy.dojo.battlecity.model.Elements;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;

import static com.codenjoy.dojo.services.Direction.*;

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
                obj=new Bullet(point);
                break;
            case TANK_UP:
                direction=UP;
                obj = new Me(point);
                break;
            case TANK_RIGHT:
                direction=RIGHT;
                obj = new Me(point);
                break;
            case TANK_DOWN:
                direction=DOWN;
                obj = new Me(point);
                break;
            case TANK_LEFT:
                direction=LEFT;
                obj = new Me(point);
                break;

            case OTHER_TANK_UP:
            case AI_TANK_UP:
                direction = UP;
                obj = new Enemy((point));
                break;
            case OTHER_TANK_RIGHT:
            case AI_TANK_RIGHT:
                direction = RIGHT;
                obj = new Enemy((point));
                break;
            case OTHER_TANK_DOWN:
            case AI_TANK_DOWN:
                direction = DOWN;
                obj = new Enemy((point));
                break;
            case OTHER_TANK_LEFT:
            case AI_TANK_LEFT:
                direction = LEFT;
                obj = new Enemy((point));
                break;
        }

        obj.setDirection(direction);
        obj.setPower(power);
        return obj;
    }




}
