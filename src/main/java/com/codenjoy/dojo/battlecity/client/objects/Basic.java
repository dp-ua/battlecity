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
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Basic {
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

    public Basic(Point point) {
        this.point = point;
        links = new ArrayList<>();
    }

    public int getExtraMove() {
        return power == -1 ? 1 : power*5;
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
