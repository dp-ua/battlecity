package com.codenjoy.dojo.battlecity.client.neron;

import com.codenjoy.dojo.battlecity.client.Board;
import com.codenjoy.dojo.battlecity.model.Elements;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;

import java.util.ArrayList;
import java.util.List;

public class DetectorService {
    Elements OUT_FIELD_ELEMENT = Elements.BATTLE_WALL;

    public List<Detector> getDetectors(Board board, Point point) {
        List<Detector> result = new ArrayList<>();
        result.add(getForwardDetector(board, point));
        result.add(getStarLinesDetector(board, point, 1));
        result.add(getStarLinesDetector(board, point, 2));
        result.add(getStarLinesDetector(board, point, 3));
        result.add(getSquareDetector(board, point, 1));
        result.add(getSquareDetector(board, point, 2));
        result.add(getSquareDetector(board, point, 3));
        return result;
    }

    private Detector getSquareDetector(Board board, Point point, int roundSquare) {
        Detector detector = new Detector();
        detector.setName("SQ" + roundSquare);
        detector.setCoef(0.7);
        StringBuilder field = new StringBuilder();
        field.append(getSquareAroundField(board, point, roundSquare));
        detector.setField(field.toString());
        return detector;
    }

    private Detector getStarLinesDetector(Board board, Point point, int linesSize) {
        Detector detector = new Detector();
        detector.setName("SL" + linesSize);
        detector.setCoef(0.6);
        StringBuilder field = new StringBuilder();

        Elements at = board.getAt(point);
        field.append(at);
        field.append(getLinesAroundField(board, point, linesSize));
        detector.setField(field.toString());
        return detector;
    }


    private Detector getForwardDetector(Board board, Point point) {
        int forwardScan = 5;

        Detector detector = new Detector();
        detector.setName("DF");
        detector.setCoef(0.5);
        StringBuilder field = new StringBuilder();

        Elements at = board.getAt(point);
        field.append(at);

        Direction elementDirection = getElementDirection(at);
        Point copy = point.copy();
        if (elementDirection != Direction.STOP)
            for (int i = 0; i < forwardScan; i++) {
                copy.change(elementDirection);
                field.append(getElementFromBoard(board, copy));
            }
        detector.setField(field.toString());
        return detector;
    }

    private String getSquareAroundField(Board board, Point point, int roundSquare) {
        StringBuilder field = new StringBuilder();

        List<Point> scanPoints = new ArrayList<>();
        if (roundSquare <= 0)
            scanPoints.add(point);
        else
            for (int y = point.getY() - roundSquare; y <= point.getY() + roundSquare; y++)
                for (int x = point.getX() - roundSquare; x <= point.getX() + roundSquare; x++)
                    scanPoints.add(new PointImpl(x, y));
        for (Point scanPoint : scanPoints) {
            field.append(getElementFromBoard(board, scanPoint));
        }
        return field.toString();
    }

    private String getLinesAroundField(Board board, Point point, int linesSize) {
        StringBuilder field = new StringBuilder();
        for (Direction direction : Direction.onlyDirections()) {
            Point copy = point.copy();
            for (int i = 0; i < linesSize; i++) {
                copy.change(direction);
                field.append(getElementFromBoard(board, copy));
            }
        }
        return field.toString();
    }

    private Elements getElementFromBoard(Board board, Point point) {
        if (board.isOutOfField(point.getX(), point.getY())) return OUT_FIELD_ELEMENT;
        else return board.getAt(point);
    }

    private Direction getElementDirection(Elements element) {
        String name = element.name();
        for (Direction direction : Direction.onlyDirections()) {
            if (name.indexOf(direction.toString()) != -1) return direction;
        }
        return Direction.STOP;
    }
}
