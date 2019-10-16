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

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Basic {
    public static final int ACT_DELAY = 5;
    @Getter
    Point point;

    @Getter
    @Setter
    Direction direction;
    @Getter
    @Setter
    int power = -1;

    @Getter
    List<Basic> links;

    public int getAttackRange() {
        return 0;
    }

    public List<Point> getBadPoints() {
        List<Point> result = new ArrayList<>();

        Set<Direction> workDirections = new HashSet<>();

        if (direction == Direction.STOP) {
            Direction d = Direction.UP;
            workDirections.addAll(Direction.onlyDirections());
        } else workDirections.add(direction);
        for (Direction d : workDirections) {
            Point copy = point.copy();
            for (int i = 0; i < getAttackRange(); i++) {
                copy.change(d);
                result.add(new PointImpl(copy));
            }
        }
        return result;
    }

    public Basic(Point point) {
        this.point = point;
        links = new ArrayList<>();
    }

    public int getExtraMove() {
        return power == -1 ? 1 : power * ACT_DELAY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Basic basic = (Basic) o;
        return getPoint().equals(basic.getPoint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPoint());
    }
}
