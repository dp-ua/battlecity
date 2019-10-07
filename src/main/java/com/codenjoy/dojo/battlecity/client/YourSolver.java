package com.codenjoy.dojo.battlecity.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
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

import com.codenjoy.dojo.battlecity.model.Elements;
import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.RandomDice;

import java.util.*;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {

    private Dice dice;
    private Board board;


    Set<Point> getDangerousPoints() {
        Set<Point> result = new HashSet<>();
        List<Point> bullets = board.getBullets();
        for (Point p : bullets) {
            Set<Point> pointAround = getPointAround(p);
            result.addAll(pointAround);

        }
        List<Point> enemies = board.getEnemies();
        for (Point p : enemies) {
            Direction direction = getDirection(p);
            if (direction != null) {
                p.change(direction);
                result.add(p);
            }
        }
        return result;
    }

    Set<Point> getPointAround(Point point) {
        Direction direction = Direction.UP;
        Set<Point> result = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            Point copy = point.copy();
            copy.change(direction);
            result.add(copy);
            direction = direction.clockwise();
        }
        return result;
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


    List<Direction> getDirectionsFromPointToPoint(Point from, Point to) {
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

    boolean canIStepToPoint(Point point) {
        Set<Point> dangerousPoints = getDangerousPoints();
        if (dangerousPoints.contains(point)) return false;
        if (board.isBarrierAt(point)) return false;
        if (board.getBullets().contains(point)) return false;
//        if (board.getEnemies().contains(point)) return false;
        // TODO: 10/7/2019 is dangerous point return false

        return true;
    }

    Set<Point> getFreePointsForMove(Point point) {
        Set<Point> result = new HashSet<>();
        Set<Point> pointAround = getPointAround(point);
        for (Point p : pointAround) {
            if (canIStepToPoint(p)) result.add(p);
        }
        return result;
    }


    Point getClosestPoint(List<Point> targets, Point point) {

        if (targets.size() == 0) return point;
        Point result = targets.get(0);
        for (Point p : targets)
            if (point.distance(p) < point.distance(result)) result = p;
        return result;
    }


    Direction getDirection(Point point) {
        Elements at = board.getAt(point);
        int i = at.name().lastIndexOf("_");
        if (i == -1) return null;
        String direction = at.name().substring(i + 1);
        return Direction.valueOf(direction);
    }

    List<Direction> getDirectionsWhereISeeEnemies(Point point) {
        List<Direction> result = new ArrayList<>();
        Direction direction = Direction.UP;
        List<Point> enemies = board.getEnemies();
        for (int i = 0; i < 4; i++) {
            Point copy = point.copy();
            copy.change(direction);
            while (!board.isBarrierAt(copy)) {
                if (enemies.contains(copy)) {
                    result.add(direction);
                    break;
                }
                copy.change(direction);
            }
            direction = direction.clockwise();
        }
        return result;
    }

    String getNextMove() {
        String result;
        Point me = board.getMe();
        System.out.println("Closest target: " + getClosestPoint(board.getEnemies(), me));
        Set<Point> freePointsForMove = getFreePointsForMove(me);

        List<Direction> directionsWhereISeeEnemies = getDirectionsWhereISeeEnemies(me);
        System.out.println("I see:" + Arrays.toString(directionsWhereISeeEnemies.toArray()));


        Point target = getClosestPoint(board.getEnemies(), me);
        Point closestPointToTarget = getClosestPoint(new ArrayList(Arrays.asList(freePointsForMove.toArray())), target);
        List<Direction> directionsFromPointToPoint = getDirectionsFromPointToPoint(me, closestPointToTarget);

        Direction direction;
        if (directionsFromPointToPoint.size() > 0) direction = directionsFromPointToPoint.get(0);
        else direction = Direction.STOP;

        if (directionsWhereISeeEnemies.contains(direction)) return "ACT," + direction.toString();
        else return direction.toString();
    }

    @Override
    public String get(Board board) {
        this.board = board;
        if (board.isGameOver()) return "";

        return getNextMove();
    }

    public static void main(String[] args) {
        WebSocketRunner.runClient(
                // paste here board page url from browser after registration
                "http://codenjoy.com/codenjoy-contest/board/player/nj3p5h4t9uzgr0junj52?code=6551112659237526156",
                new YourSolver(new RandomDice()),
                new Board());
    }

    public YourSolver(Dice dice) {
        this.dice = dice;
    }
}
