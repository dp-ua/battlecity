package clientlib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static clientlib.Elements.AI_TANK_DOWN;
import static clientlib.Elements.AI_TANK_LEFT;
import static clientlib.Elements.AI_TANK_RIGHT;
import static clientlib.Elements.AI_TANK_UP;
import static clientlib.Elements.BATTLE_WALL;
import static clientlib.Elements.BULLET;
import static clientlib.Elements.CONSTRUCTION;
import static clientlib.Elements.CONSTRUCTION_DESTROYED;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_DOWN;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_DOWN_LEFT;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_DOWN_RIGHT;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_DOWN_TWICE;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_LEFT;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_LEFT_RIGHT;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_LEFT_TWICE;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_RIGHT;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_RIGHT_TWICE;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_RIGHT_UP;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_UP;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_UP_DOWN;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_UP_LEFT;
import static clientlib.Elements.CONSTRUCTION_DESTROYED_UP_TWICE;
import static clientlib.Elements.OTHER_TANK_DOWN;
import static clientlib.Elements.OTHER_TANK_LEFT;
import static clientlib.Elements.OTHER_TANK_RIGHT;
import static clientlib.Elements.OTHER_TANK_UP;
import static clientlib.Elements.TANK_DOWN;
import static clientlib.Elements.TANK_LEFT;
import static clientlib.Elements.TANK_RIGHT;
import static clientlib.Elements.TANK_UP;


public class SampleSolver extends Solver {

    private Random rnd = new Random();

    public List<Point> getPlayerTankCoordinates() {
        List<Point> playerTank = getCoordinates(TANK_DOWN, TANK_UP, TANK_LEFT, TANK_RIGHT);
        if (playerTank.size() == 0) {
            playerTank.add(new Point(0, 0));
        }
        return playerTank;
    }


    public List<Point> getOtherPlayersTanks() {
        List<Point> otherPlayers = getCoordinates(OTHER_TANK_DOWN, OTHER_TANK_UP, OTHER_TANK_LEFT, OTHER_TANK_RIGHT);
        return otherPlayers;
    }

    public List<Point> getBotsTanks() {
        List<Point> bots = getCoordinates(AI_TANK_DOWN, AI_TANK_UP, AI_TANK_LEFT, AI_TANK_RIGHT);
        return bots;
    }

    public List<Point> getBullets() {
        List<Point> bullets = getCoordinates(BULLET);
        return bullets;
    }


    public List<Point> getConstructions() {
        List<Point> constructions = getCoordinates(CONSTRUCTION);
        return constructions;
    }

    public List<Point> getDestroyedConstructions() {
        List<Point> constructions = getCoordinates(CONSTRUCTION_DESTROYED_DOWN,
                CONSTRUCTION_DESTROYED_UP,
                CONSTRUCTION_DESTROYED_LEFT,
                CONSTRUCTION_DESTROYED_RIGHT,
                CONSTRUCTION_DESTROYED,

                CONSTRUCTION_DESTROYED_DOWN_TWICE,
                CONSTRUCTION_DESTROYED_UP_TWICE,
                CONSTRUCTION_DESTROYED_LEFT_TWICE,
                CONSTRUCTION_DESTROYED_RIGHT_TWICE,

                CONSTRUCTION_DESTROYED_LEFT_RIGHT,
                CONSTRUCTION_DESTROYED_UP_DOWN,

                CONSTRUCTION_DESTROYED_UP_LEFT,
                CONSTRUCTION_DESTROYED_RIGHT_UP,
                CONSTRUCTION_DESTROYED_DOWN_LEFT,
                CONSTRUCTION_DESTROYED_DOWN_RIGHT);
        return constructions;
    }

    public List<Point> getWalls() {
        List<Point> walls = getCoordinates(BATTLE_WALL);
        return walls;
    }

    public List<Point> getBarriers() {
        List<Point> barriers = new ArrayList<>();
        barriers.addAll(getWalls());
        barriers.addAll(getConstructions());
        barriers.addAll(getDestroyedConstructions());
        barriers.addAll(getOtherPlayersTanks());
        barriers.addAll(getBotsTanks());
        return barriers;
    }

    public boolean isNear(int x, int y, Elements el) {
        return isAt(x + 1, y, el) ||
                isAt(x - 1, y, el) ||
                isAt(x, y - 1, el) ||
                isAt(x, y + 1, el);
    }

    public boolean isBarrierAt(int x, int y) {
        return getBarriers().contains(new Point(x, y));
    }

    public boolean isAnyOfAt(int x, int y, Elements... elements) {
        boolean result = false;
        for (Elements el : elements) {
            result = isAt(x, y, el);
            if (result) break;
        }
        return result;
    }

    public boolean isAt(int x, int y, Elements element) {
        if (isOutOfBounds(x, y)) {
            return false;
        } else {
            return field[x][y] == element;
        }
    }

    public int countNear(int x, int y, Elements element) {
        int counter = 0;
        if (isAt(x + 1, y, element)) counter++;
        if (isAt(x - 1, y, element)) counter++;
        if (isAt(x, y + 1, element)) counter++;
        if (isAt(x, y - 1, element)) counter++;
        return counter;
    }

    public boolean isOutOfBounds(int x, int y) {
        return x >= field.length || y >= field.length || x < 0 || y < 0;
    }


    public List<Point> getCoordinates(Elements... searchElements) {
        Set<Elements> searchSetElements = new HashSet<>(Arrays.asList(searchElements));
        List<Point> elementsCoordinates = new ArrayList<>();

        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field.length; x++) {
                if (searchSetElements.contains(field[x][y])) {
                    elementsCoordinates.add(new Point(x, y));
                }
            }
        }
        return elementsCoordinates;
    }

    @Override
    public String move() {
        switch (rnd.nextInt(4)) {
            case 0:
                return left(Action.AFTER_TURN);
            case 1:
                return right(Action.AFTER_TURN);
            case 2:
                return up(Action.AFTER_TURN);
            case 3:
                return down(Action.AFTER_TURN);
        }
        return left();
    }

}
