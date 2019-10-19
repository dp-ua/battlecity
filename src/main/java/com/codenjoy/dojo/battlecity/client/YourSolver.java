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
import com.codenjoy.dojo.battlecity.client.statistic.StatisticComponent;
import com.codenjoy.dojo.battlecity.client.statistic.StatisticHolder;
import com.codenjoy.dojo.battlecity.model.Elements;
import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.RandomDice;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static com.codenjoy.dojo.services.Direction.STOP;

/**
 * User: your name
 */
@Getter
@Setter
public class YourSolver implements Solver<Board> {

    public int FREE_MOVES_BEHIND = 6;
    public int SCAN_RANGE_ATTACK = 8;
    public int MAX_MOVES_ANALIZE = 30;
    public boolean NO_TARGET_TO_AI = true;
    public boolean IS_SIMPLE_MOD = false;

    public GameType gameType = GameType.MOVEANDSHOOT;

    public int DURATION_EXCESS_STATISTIC = 500;

    protected StatisticHolder statisticHolder = StatisticHolder.getInstance();
    protected Dice dice;
    protected Board board;

    BoardState boardState = new BoardState(MAX_MOVES_ANALIZE);


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

    protected Direction getWayToClosestTarget(List<Point> targets, Point point) {
        long start = System.currentTimeMillis();
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
        long finish = System.currentTimeMillis();
        statisticHolder.addOther(new StatisticComponent(start, finish, "getWayToClosestTarget:" + point));
        return direction;
    }

    protected boolean isNotBarried(Point point) {
        if (board.isBarrierAt(point)) return false;
        if (board.isBulletAt(point.getX(), point.getY())) return false;
        return true;
    }

    protected Map<Direction, Integer> getDirectionsWhereISeeEnemies(Point point) {
        long start = System.currentTimeMillis();
        Map<Direction, Integer> result = new HashMap<>();
        Set<Point> enemies = new HashSet<>(board.getEnemies());
        Set<Point> barriers = new HashSet<>(board.getBarriers());
        barriers.addAll(board.getBullets());

        for (Direction direction : Direction.onlyDirections()) {
            Point copy = point.copy();
            copy.change(direction);
            int count = 0;
            while (count < SCAN_RANGE_ATTACK && !barriers.contains(copy)) {
                if (enemies.contains(copy)) {
                    result.put(direction, count);
                    break;
                }
                count++;
                copy.change(direction);
            }
        }
        long finish = System.currentTimeMillis();
        statisticHolder.addOther(new StatisticComponent(start, finish, "getDirectionsWhereISeeEnemies from:" + point));
        return result;
    }


    protected List<Point> getTargets() {
        long start = System.currentTimeMillis();
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
        long finish = System.currentTimeMillis();
        statisticHolder.addOther(new StatisticComponent(start, finish, "getTargets"));
        return result;
    }

    protected List<Point> getSafePointsAround(Point point) {
        List<Point> result = new ArrayList<>();
        List<Point> enemies = board.getEnemies();
        for (Point p : boardState.getPointsAround(point))
            if (!boardState.badPoints.contains(p))
                if (!enemies.contains(p))
                    if (!board.isBarrierAt(p))
                        result.add(p);
        return result;
    }


    protected String getMoveAndShoot() {

        String result = "";
        Point me = board.getMe();
        Basic meBasic = boardState.getBasicByPoint(me);
        Direction myActiveDirection = meBasic.getDirection();
        Map<Direction, Integer> directionsWhereISeeEnemiesWithRange = getDirectionsWhereISeeEnemies(me);

        Set<Direction> directionsWhereISeeEnemies = directionsWhereISeeEnemiesWithRange.keySet();
        System.out.println("Enemies on:" + Arrays.toString(directionsWhereISeeEnemies.toArray()));
        System.out.println("I look:[" + myActiveDirection.toString() + "]");

        List<Point> targets = getTargets();
        Direction directionForNewMove = STOP;

        if (boardState.badPoints.contains(me)) {
            System.out.println("I Have trouble. Need run");
            Direction runDirection = getRunDirection(me);
            if (runDirection != STOP) directionForNewMove = runDirection;
            else {
                List<Point> safePointsAround = getSafePointsAround(me);
                if (safePointsAround.size() > 0) {
                    System.out.println("Another try find way to run");
                    targets = safePointsAround;
                }
            }
        }
        if (directionForNewMove == STOP) directionForNewMove = getWayToClosestTarget(targets, me);

        if (directionsWhereISeeEnemies.size() > 0 && !boardState.badPoints.contains(me)) {
            Direction needShootDirection = getDirectionWhereTargetIsCloset(directionsWhereISeeEnemiesWithRange);
            if (meBasic.getDirection() == needShootDirection) result = "ACT";
            else directionForNewMove = needShootDirection;
        }

        System.out.println("Want to move: " + directionForNewMove);
        if ("".equals(result)) result = directionForNewMove.toString() +
                (isNeedToShoot(directionForNewMove) ? ", ACT" : "");
        return result;
    }

    Direction getRunDirection(Point me) {
        Direction moveDirection = STOP;
        if (isIStayOnBadPoint(me) && isIHaveFreePointForMove(me)) {
            System.out.println("I am in dangerous. Need to run");
            Set<Point> freeSafePointsAround = getFreeSafePointsAround(me);
            Point bestFreePointForMove = getBestFreePointForMove(freeSafePointsAround);
            List<Direction> directionsFromPointToPoint = boardState.getDirectionsFromPointToPoint(me, bestFreePointForMove);
            if (directionsFromPointToPoint.size() == 1) moveDirection = directionsFromPointToPoint.get(0);
        }
        return moveDirection;
    }

    protected String getSimpleMove() {
        Point me = board.getMe();
        Direction move = STOP;
        boolean shoot = false;
        Map<Direction, Integer> directionsWhereISeeEnemies = getDirectionsWhereISeeEnemies(me);
        if (isIStayOnBadPoint(me)) {
            Set<Point> freeSafePointsAround = getFreeSafePointsAround(me);
            Direction wayToClosestTarget = getWayToClosestTarget(new ArrayList<>(freeSafePointsAround), me);
            move = wayToClosestTarget;
        }
        if (move != STOP) {
            if (directionsWhereISeeEnemies.containsKey(move)) shoot = true;
        } else {
            for (Map.Entry<Direction, Integer> entry : directionsWhereISeeEnemies.entrySet()) {
                move = entry.getKey();
                shoot = true;
                break;
            }
        }
        return move.toString() + (shoot ? ",ACT" : "");
    }

    protected boolean isNeedToShoot(Direction whereIWantToGo) {
        //new strategy
        Point me = board.getMe();
        Set<Direction> directionsWhereISeeEnemies = getDirectionsWhereISeeEnemies(me).keySet();
        Point copy = me.copy();
        copy.change(whereIWantToGo);
        Basic newPointForMove = boardState.getBasicByPoint(copy);
        if (directionsWhereISeeEnemies.contains(whereIWantToGo)) {
            System.out.println("See enemy. Shoot");
            return true;
        }
        if (newPointForMove instanceof Destroy) {
            System.out.println("Go to wall. Try to destroy");
            return true;
        }
        if (isNearEnemie(me) && whereIWantToGo != STOP) {
            System.out.println("SomeOne scout me. Shoot");
            return true;
        }
        return false;
    }

    @Override
    public String get(Board board) {
        statisticHolder.clear();
        this.board = board;
        long start = System.currentTimeMillis();
        boardState.analize(board);
        String result;
        if (board.isGameOver()) {
            System.out.println("Мертвый я :(");
            result = "";
        } else {
            switch (gameType) {
                case SIMPLE:
                    result = getSimpleMove();
                    break;
                case MOVEANDSHOOT:
                    result = getMoveAndShoot();
                    break;
                case DEFEND:
                    result = getDefendMove();
                    break;
                case USUALLY:
                default:
                    result = getMoveAndShoot();
                    break;

            }
//            result = IS_SIMPLE_MOD ? getSimpleMove() : getMoveAndShoot();
        }
        long finish = System.currentTimeMillis();
        System.out.println("Тик:" + boardState.getTick());
        statisticHolder.setMain(new StatisticComponent(start, finish, "main analyzer"));
        System.out.println(statisticHolder.getStringToShow(DURATION_EXCESS_STATISTIC));
        return result;
    }

    public String getDefendMove() {
        boolean iNeedRun = false;
        boolean hasISafePointForMove = false;
        boolean canSomeOneGoToMyPoint = false;
        boolean iSeeEnemyFromMyPoint = false;

        Direction moveDirection = STOP;
        boolean shoot = false;

        Point me = board.getMe();
        Basic meBasic = boardState.getBasicByPoint(me);
        Map<Direction, Integer> directionsWhereISeeEnemies = getDirectionsWhereISeeEnemies(me);
        List<Point> targets = getTargets();

        if (isNearEnemie(me)) canSomeOneGoToMyPoint = true;

        if (isIStayOnBadPoint(me)) {
            iNeedRun = true;
        }
        if (isIHaveFreePointForMove(me)) {
            hasISafePointForMove = true;
        }

        if (directionsWhereISeeEnemies.size() > 0) iSeeEnemyFromMyPoint = true;

        if (!iSeeEnemyFromMyPoint && !iNeedRun) {
            System.out.println("No enemies. Go to Closest target");
            moveDirection = getWayToClosestTarget(targets, me);
        } else if (iNeedRun && hasISafePointForMove) {
            System.out.println("I am in dangerous. Need to run");
            if (canSomeOneGoToMyPoint) shoot = true;
            Set<Point> freeSafePointsAround = getFreeSafePointsAround(me);
            Point bestFreePointForMove = getBestFreePointForMove(freeSafePointsAround);
            List<Direction> directionsFromPointToPoint = boardState.getDirectionsFromPointToPoint(me, bestFreePointForMove);
            if (directionsFromPointToPoint.size() == 1) moveDirection = directionsFromPointToPoint.get(0);
        } else if (!iNeedRun && iSeeEnemyFromMyPoint) {
            System.out.println("Good position. Need to Shoot");
            shoot = true;
            if (!directionsWhereISeeEnemies.containsKey(meBasic.getDirection())) {
                moveDirection = getDirectionWhereTargetIsCloset(directionsWhereISeeEnemies);
            }
        }
        if (isBarrierOnMyWay(moveDirection, me)) shoot = true;

        return (moveDirection == STOP ? "" : moveDirection.toString()) +
                (shoot ? ",ACT" : "");

    }

    boolean isBarrierOnMyWay(Direction direction, Point point) {
        Point copy = point.copy();
        copy.change(direction);
        Basic basicByPoint = boardState.getBasicByPoint(copy);
        return (basicByPoint instanceof Destroy);
    }

    boolean isNearEnemie(Point point) {
        List<Point> pointsAround = boardState.getPointsAround(point);
        for (Point p : pointsAround) {
            Basic basicByPoint = boardState.getBasicByPoint(p);
            if (basicByPoint instanceof Enemy) return true;

        }
        return false;
    }

    Point getBestFreePointForMove(Set<Point> points) {
        for (Point point : points) {
            Map<Direction, Integer> directionsWhereISeeEnemies = getDirectionsWhereISeeEnemies(point);
            if (directionsWhereISeeEnemies.size() > 0) return point;
        }
        for (Point point : points) {
            return point;
        }
        return null;
    }

    boolean isINeedTurnForShoot(Direction directionForShoot, Point pointFrom) {
        Basic me = boardState.getBasicByPoint(pointFrom);
        if (me.getDirection() == STOP) return true;
        return directionForShoot == me.getDirection();
    }

    private Direction getDirectionWhereTargetIsCloset(Map<Direction, Integer> directionsWhereISeeEnemies) {
        Optional<Integer> minRange = directionsWhereISeeEnemies.values().stream().reduce((v1, v2) -> Math.min(v1, v2));
        if (minRange.isPresent()) {
            int value = minRange.get();
            for (Map.Entry<Direction, Integer> entry : directionsWhereISeeEnemies.entrySet()) {
                if (entry.getValue() == value) return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Cant find min range");
    }


    Set<Point> getFreeSafePointsAround(Point point) {
        Set<Point> result = new HashSet<>();
        for (Point p : boardState.getPointsAround(point)) {
            if (boardState.freePoints.contains(p)) result.add(p);
        }
        return result;
    }

    private boolean isIHaveFreePointForMove(Point point) {
        Set<Point> freeSafePointsAround = getFreeSafePointsAround(point);
        return freeSafePointsAround.size() > 0;
    }

    private boolean isIStayOnBadPoint(Point point) {
        Set<Point> badPoints = boardState.badPoints;
        return badPoints.contains(point);
    }

    public static void main(String[] args) {
        YourSolver yourSolver = new YourSolver(new RandomDice());
        GameType gameType = GameType.USUALLY;
        String mainUrl = "http://dojorena.io/codenjoy-contest/board/player/v0736i3xpu69ffx52y75?code=2352770933751269297";
        if (args.length >= 1) {
            String type = args[0];

            try {
                int v = Integer.parseInt(type);
                GameType byInt = GameType.getByInt(v);
                gameType = byInt;
            } catch (NumberFormatException eN) {
                try {
                    GameType byValue = GameType.valueOf(type);
                    gameType = byValue;
                } catch (IllegalArgumentException eI) {
                }
            }
        }


        if (gameType == GameType.HELP) {
            System.out.print("Commands: ");
            for (GameType value : GameType.values()) {
                System.out.print(value + " ");
            }
            return;
        }

        if (args.length == 2) mainUrl = args[1];
        yourSolver.gameType = gameType;
        if (gameType == GameType.SIMPLE) yourSolver.SCAN_RANGE_ATTACK = 4;
        System.out.println("URL: " + mainUrl);
        System.out.println("!!!RUN " + gameType.name() + " MOD!!!");

// test server                "http://codenjoy.com/codenjoy-contest/board/player/nj3p5h4t9uzgr0junj52?code=6551112659237526156",

        WebSocketRunner.runClient(
                // paste here board page url from browser after registration
                mainUrl,  //batle server
                yourSolver,
                new Board());
    }

    public YourSolver(Dice dice) {
        this.dice = dice;

    }
}
