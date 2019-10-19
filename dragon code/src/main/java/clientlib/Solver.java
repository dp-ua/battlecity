package clientlib;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static clientlib.Action.NONE;
import static clientlib.WebSocketRunner.formatData;
import static clientlib.WebSocketRunner.print;
import static java.lang.String.format;

public abstract class Solver {

    protected int size;
    protected Elements[][] field;
    protected Map<Elements, List<Point>> mapElements;

    private long prevTime;

    private ConcurrentHashMap<Integer, Object> map = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<Integer, String> action = new ConcurrentHashMap<>();

    /**
     * Метод парсинга игрового поля. Вызывается после ответа сервера
     *
     * @param boardString игровое поле
     */
    public String makeAMove(String boardString) {
        Integer val = boardString.hashCode();
        Object lockObj = new Object();
        Object currObj = map.putIfAbsent(val, lockObj);
        if (currObj == null) {
            currObj = lockObj;
        }
        synchronized (currObj) {
            return syncAction(boardString, val);
        }
    }

    private String syncAction(String boardString, Integer val) {
        String s = action.get(val);
        if (s != null) {
            while (queue.size() > 10) {
                Integer poll = queue.poll();
                map.remove(poll);
                action.remove(poll);
            }
            return s;
        }
        print("-------------------------------------------------------------");
        long currTime = System.currentTimeMillis();
        print("Data from server:\n" + formatData(boardString));
        print("Time diff " + (currTime - prevTime));
        prevTime = currTime;

        String board = boardString.replaceAll("\n", "");
        size = (int) Math.sqrt(board.length());
        field = new Elements[size][size];
        mapElements = new HashMap<>();

        board = boardString.replaceAll("\n", "");

        char[] temp = board.toCharArray();
        for (int y = 0; y < size; y++) {
            int dy = y * size;
            for (int x = 0; x < size; x++) {
                field[x][y] = Elements.valueOf(temp[dy + x]);
            }
        }

        String move = move();
        action.put(val, move);
        return move;
    }

    protected final String up(Action action) {
        return act("UP", action);
    }

    protected final String down(Action action) {
        return act("DOWN", action);
    }

    protected final String left(Action action) {
        return act("LEFT", action);
    }

    protected final String right(Action action) {
        return act("RIGHT", action);
    }

    protected final String act() {
        return act("ACT", NONE);
    }

    protected final String up() {
        return act("UP", NONE);
    }

    protected final String down() {
        return act("DOWN", NONE);
    }

    protected final String left() {
        return act("LEFT", NONE);
    }

    protected final String right() {
        return act("RIGHT", NONE);
    }


    public abstract String move();

    private String act(String direction, Action action) {
        action = action == null ? NONE : action;
        return format("%s%s%s", action.getPreTurn(), direction, action.getPostTurn());
    }
}
