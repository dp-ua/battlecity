package com.codenjoy.dojo.battlecity.client;

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

import com.codenjoy.dojo.battlecity.client.objects.Basic;
import com.codenjoy.dojo.battlecity.client.objects.ObjectDetector;
import com.codenjoy.dojo.battlecity.client.objects.action.Attack;
import com.codenjoy.dojo.battlecity.client.objects.action.Death;
import com.codenjoy.dojo.battlecity.client.objects.action.Step;
import com.codenjoy.dojo.battlecity.client.objects.implement.Bullet;
import com.codenjoy.dojo.battlecity.client.objects.implement.Free;
import com.codenjoy.dojo.battlecity.client.statistic.StatisticComponent;
import com.codenjoy.dojo.battlecity.client.statistic.StatisticHolder;
import com.codenjoy.dojo.battlecity.model.Elements;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class BoardState {
    public final int MAX_MOVES_ANALYZE;

    StatisticHolder statisticHolder = StatisticHolder.getInstance();

    @Getter
    int tick = 0;
    @Getter@Setter
    int lastShoot = -100;
    private Basic[][] newState;
    private Basic[][] oldState;
    Board board;
    Board oldBoard;
    ObjectDetector detector = new ObjectDetector();
    Set<Point> badPoints;
    Set<Point> freePoints;

    public BoardState(int max_moves_analyze) {
        MAX_MOVES_ANALYZE = max_moves_analyze;
    }

    private Basic[][] getState() {
        long start = System.currentTimeMillis();
        char[][] field = board.getField();
        Basic[][] state = new Basic[field.length][field[0].length];

        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                char c = field[i][j];
                Elements at = board.getAt(i, j);
                Basic object = detector.getObject(new PointImpl(i, j), Elements.valueOf(c));
                state[i][j] = object;

                if (object instanceof Death) {
                    badPoints.add(object.getPoint());
                }
                if (object instanceof Attack) {
                    badPoints.addAll(object.getBadPoints());
                }
                if(object instanceof Free){
                    freePoints.add(object.getPoint());
                }
            }
        }
        long finish = System.currentTimeMillis();
        statisticHolder.addOther(new StatisticComponent(start, finish, "boardAnalize: getState"));
        return state;
    }

    private Point getNextPoint(Point point) {
        Point copy = point.copy();
        copy.change(Direction.RIGHT);
        if (board.isOutOfField(copy.getX(), copy.getY())) {
            copy.change(Direction.UP);
            copy.setX(0);
        }
        return copy;
    }


    List<Bullet> getBulletsList(Basic[][] state) {
        List<Bullet> result = new ArrayList<>();
        if (state == null) return result;
        for (Basic[] i : state)
            for (Basic obj : i)
                try {
                    Bullet b = (Bullet) obj;
                    result.add(b);
                } catch (ClassCastException e) {//ignore. It is Ok}
                }
        return result;
    }

    public List<Point> getPointsAround(Point point) {
        List<Point> result = new ArrayList<>();
        Direction direction = Direction.UP;
        for (int i = 0; i < 4; i++) {
            Point copy = point.copy();
            copy.change(direction);
            if (!board.isOutOfField(copy.getX(), copy.getY())) result.add(copy);
            direction = direction.clockwise();
        }
        return result;
    }

    public Basic getBasicByPoint(Point point) {
        if (board.isOutOfField(point.getX(), point.getY())) return null;
        Basic basic = newState[point.getX()][point.getY()];
        return basic;
    }

    public Basic getBasicByDirection(Point point, Direction direction) {
        Point copy = point.copy();
        copy.change(direction);
        if (board.isOutOfField(copy.getX(), copy.getY())) return null;
        Basic basic = newState[copy.getX()][copy.getY()];
        return basic;
    }

    public Map<Basic, Pair<Direction, Integer>> getWaysToAllAccessiblePoints(Point point) {
        long start = System.currentTimeMillis();
        Map<Basic, Pair<Direction, Integer>> result = new HashMap<>();
        result.put(newState[point.getX()][point.getY()], new Pair<>(Direction.STOP, 0));

        boolean newLinkBeenAdded = true;
        int moveCout = 0;
        while (newLinkBeenAdded) {
            newLinkBeenAdded = false;
            Map<Basic, Pair<Direction, Integer>> temp = new HashMap<>();
            for (Map.Entry<Basic, Pair<Direction, Integer>> entry : result.entrySet()) {
                Basic object = entry.getKey();
                Integer power = entry.getValue().getValue();
                Direction direction = entry.getValue().getKey();
                for (Basic link : object.getLinks()) {
                    Direction addDirection = direction;
                    if (direction == Direction.STOP)
                        addDirection = getDirectionsFromPointToPoint(object.getPoint(), link.getPoint()).get(0);

                    temp.put(link, new Pair(addDirection, link.getExtraMove() + power));
                }
            }

            for (Map.Entry<Basic, Pair<Direction, Integer>> basicPairEntry : temp.entrySet()) {
                if (result.containsKey(basicPairEntry.getKey())) {
                    Pair<Direction, Integer> objectInResult = result.get(basicPairEntry.getKey());
                    if (objectInResult.getValue() > basicPairEntry.getValue().getValue()) {
                        result.put(basicPairEntry.getKey(), basicPairEntry.getValue());
                        newLinkBeenAdded = true;
                    }
                } else {
                    result.put(basicPairEntry.getKey(), basicPairEntry.getValue());
                    newLinkBeenAdded = true;
                }
            }
            moveCout++;
            if (moveCout > MAX_MOVES_ANALYZE) break;
        }
        long finish = System.currentTimeMillis();
        statisticHolder.addOther(new StatisticComponent(start, finish, "boardAnalize: getWaysToAllAccessiblePoints"));
        return result;
    }

    private void linksAnalize() {
        long start = System.currentTimeMillis();

        Point point = new PointImpl(0, 0);

        while (true) {
            int x = point.getX();
            int y = point.getY();

            Basic object = newState[x][y];
            List<Point> pointsAround = getPointsAround(point);
            for (Point p : pointsAround) {
                Basic basic = newState[p.getX()][p.getY()];

                if (!badPoints.contains(p)) //Добавляем проверку на плохую точку для шага
                    if (basic instanceof Step) object.getLinks().add(basic);
            }
            point = getNextPoint(point);
            if (board.isOutOfField(point.getX(), point.getY())) break;
        }
        long finish = System.currentTimeMillis();
        statisticHolder.addOther(new StatisticComponent(start, finish, "boardAnalize: linksAnalize"));
    }

    Direction getDirectionByBiasX(int biasX) {
        if (biasX < 0) return Direction.RIGHT;
        if (biasX > 0) return Direction.LEFT;
        return null;
    }

    Direction getDirectionByBiasY(int biasY) {
        if (biasY < 0) return Direction.UP;
        if (biasY > 0) return Direction.DOWN;
        return null;
    }

    public List<Direction> getDirectionsFromPointToPoint(Point from, Point to) {
        List<Direction> result = new LinkedList<>();
        int biasX = from.getX() - to.getX();
        int biasY = from.getY() - to.getY();
        Direction directionByBiasX = getDirectionByBiasX(biasX);
        Direction directionByBiasY = getDirectionByBiasY(biasY);
        if (Math.abs(biasX) < Math.abs(biasY)) {
            if (directionByBiasX != null) result.add(directionByBiasX);
            if (directionByBiasY != null) result.add(directionByBiasY);
        } else {
            if (directionByBiasY != null) result.add(directionByBiasY);
            if (directionByBiasX != null) result.add(directionByBiasX);
        }
        return result;
    }

    private void compareFreeAndBadPoints(){
        for (Point badPoint : badPoints) {
            if (freePoints.contains(badPoint)) freePoints.remove(badPoint);
        }
    }

    public void analize(Board board) {
        long start = System.currentTimeMillis();
        oldBoard = this.board;
        this.board = board;
        badPoints = new HashSet<>();
        freePoints = new HashSet<>();
        oldState = newState;
        newState = getState();
        compareFreeAndBadPoints();
        linksAnalize();
        tick++;
        long finish = System.currentTimeMillis();
        statisticHolder.addOther(new StatisticComponent(start, finish, "boardAnalize: mainAnalize"));
    }


}
