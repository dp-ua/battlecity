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

import com.codenjoy.dojo.battlecity.client.objects.Basic;
import com.codenjoy.dojo.battlecity.client.objects.action.Destroy;
import com.codenjoy.dojo.battlecity.client.objects.implement.Enemy;
import com.codenjoy.dojo.battlecity.model.Elements;
import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.RandomDice;
import javafx.util.Pair;
import sun.security.krb5.internal.crypto.Des;

import java.util.*;

import static com.codenjoy.dojo.services.Direction.STOP;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {
    private Dice dice;
    private Board board;
    BoardState boardState = new BoardState();

    public static final int SCAN_RANGE_ATTACK = 12;
    public static final int FREE_MOVES_BEHIND = 6;
    public static final boolean NO_TARGET_TO_AI = true;

    class CacheMap<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        CacheMap(int capacity) {
            super(capacity + 1, 1.1f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return this.size() > capacity;
        }
    }

    private Direction getWayToClosestTarget(List<Point> targets, Point point) {
        Direction direction = STOP;
        int moves = Integer.MAX_VALUE;
        Basic finalPoint = null;
        Map<Basic, Pair<Direction, Integer>> waysToAllAccessiblePoints = boardState.getWaysToAllAccessiblePoints(point);
        for (Point p : targets) {
            Basic basicByPoint = boardState.getBasicByPoint(p);
            if (waysToAllAccessiblePoints.containsKey(basicByPoint)) {
                Pair<Direction, Integer> directionIntegerPair = waysToAllAccessiblePoints.get(basicByPoint);
                Integer movesToPoint = directionIntegerPair.getValue();
                if (movesToPoint < moves) {
                    direction = directionIntegerPair.getKey();
                    moves = movesToPoint;
                    finalPoint = basicByPoint;
                }
            }
        }
        if (finalPoint != null) {
            System.out.println("I go to: " + finalPoint + " by: " + direction);
        }
        return direction;
    }

    List<Direction> getDirectionsWhereISeeEnemies(Point point) {
        List<Direction> result = new ArrayList<>();
        for (Direction direction : Direction.onlyDirections()) {
            List<Point> enemies = board.getEnemies();
            Point copy = point.copy();
            copy.change(direction);
            int count = 0;
            while (!board.isBarrierAt(copy) && count < SCAN_RANGE_ATTACK) {
                if (enemies.contains(copy)) {
                    result.add(direction);
                    break;
                }
                copy.change(direction);
            }
        }
        return result;
    }


    public List<Point> getTargets() {
        List<Point> result = new ArrayList<>();
        List<Point> enemiesALL = board.getEnemies();
        List<Point> enemisWithoutAI = new ArrayList<>();
        List<Point> whatEnemiesNeedToUse;

        for (Point point : enemiesALL) {
            Basic enemy = boardState.getBasicByPoint(point);
            if (enemy instanceof Enemy) enemisWithoutAI.add(point);
            Direction enemyDirection = enemy.getDirection();
            Point copyEnemyPoint = point.copy();
            if (Direction.onlyDirections().contains(enemyDirection))
                for (int i = 0; i < FREE_MOVES_BEHIND; i++) {
                    copyEnemyPoint.change(enemyDirection.inverted());
                    if (board.getAt(copyEnemyPoint.getX(), copyEnemyPoint.getY()).equals(Elements.NONE))
                        result.add(copyEnemyPoint.copy());
                    else break;
                }
        }
        whatEnemiesNeedToUse = NO_TARGET_TO_AI ? enemisWithoutAI : enemiesALL;
        if (result.contains(board.getMe())) result = whatEnemiesNeedToUse;
        else result.addAll(whatEnemiesNeedToUse);
        return result;
    }

    List<Point> getSafePointsAround(Point point) {
        List<Point> result = new ArrayList<>();
        List<Point> enemies = board.getEnemies();
        for (Point p : boardState.getPointsAround(point))
            if (!boardState.badPoints.contains(p))
                if (!enemies.contains(p))
                    if (!board.isBarrierAt(p))
                        result.add(p);
        return result;
    }


    String getNextMove() {
        String result = "";
        Point me = board.getMe();
        Basic meBasic = boardState.getBasicByPoint(me);
        Direction myActiveDirection = meBasic.getDirection();

        List<Direction> directionsWhereISeeEnemies = getDirectionsWhereISeeEnemies(me);
        System.out.println("Enemies on:" + Arrays.toString(directionsWhereISeeEnemies.toArray()));
        System.out.println("I look:[" + myActiveDirection.toString() + "]");

        List<Point> targets = getTargets();
        Direction directionForNewMove = STOP;

        if (boardState.badPoints.contains(me)) {
            System.out.println("Плохо стою. Ищу пути отхода");
            List<Point> safePointsAround = getSafePointsAround(me);
            if (safePointsAround.size() > 0) {
                System.out.println("Отход есть. Пытаюсь уйти.");
                targets = safePointsAround;
            }
        }
        directionForNewMove = getWayToClosestTarget(targets, me);
        System.out.println("Я хочу походить: " + directionForNewMove);
        result = directionForNewMove.toString() +
                (isNeedToShoot(directionForNewMove) ? ", ACT" : "");
        return result;
    }

    int lastShoot = -100;

    private String getTestMove() {
        boolean before = false;
        Point me = board.getMe();
        Direction direction = boardState.getBasicByPoint(me).getDirection();
        String act = direction.clockwise().toString();

        if (boardState.tick - lastShoot > 8) {
            lastShoot = boardState.tick;
//            act+=",ACT," + direction.clockwise().clockwise().toString();
            act = before ? "ACT, " + act : act + ", ACT";
        }
        return act;
    }

    boolean isNeedToShoot(Direction whereIWhantToGo) {
        //new strategy
        Point me = board.getMe();
        List<Direction> directionsWhereISeeEnemies = getDirectionsWhereISeeEnemies(me);
        Basic meBasic = boardState.getBasicByPoint(me);
        Point copy = me.copy();
        copy.change(whereIWhantToGo);
        Basic newPointForMove = boardState.getBasicByPoint(copy);
        if (directionsWhereISeeEnemies.contains(whereIWhantToGo)) {
            System.out.println("В направлении моего хода вижу противника. Стреляю");
            return true;
        }
        if (newPointForMove instanceof Destroy) {
            System.out.println("Собираюсь идти в стену. Надо рушить.");
            return true;
        }
        return false;
    }

    @Override
    public String get(Board board) {
        this.board = board;
        long start = System.currentTimeMillis();
        boardState.analize(board);
        String result;
        if (board.isGameOver()) {
            System.out.println("Мертвый я :(");
            result = "";
        } else {
            result = getNextMove();
//            result = getTestMove();
        }
        long finish = System.currentTimeMillis();

        System.out.println("Тик:" + boardState.getTick());
        System.out.println("Время на анализ: " + (finish - start) + "ms");
        return result;
    }


    public static void main(String[] args) {
        WebSocketRunner.runClient(
                // paste here board page url from browser after registration
                "http://dojorena.io/codenjoy-contest/board/player/v0736i3xpu69ffx52y75?code=2352770933751269297",  //batle server
// test server                "http://codenjoy.com/codenjoy-contest/board/player/nj3p5h4t9uzgr0junj52?code=6551112659237526156",
                new YourSolver(new RandomDice()),
                new Board());
    }

    public YourSolver(Dice dice) {
        this.dice = dice;
    }
}
