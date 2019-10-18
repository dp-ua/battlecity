package com.codenjoy.dojo.battlecity;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the ElementFree Software Foundation, either version 3 of the
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


import com.codenjoy.dojo.battlecity.client.YourSolver;

/**
 * Created by Oleksandr_Baglai on 2016-10-15.
 */
public class SolverRunner {
    final long SHORT_TIMEOUT_RECONNECT = 5000;
    final long LONG_TIMEOUT_RECONNECT = SHORT_TIMEOUT_RECONNECT * 12;
    final int SHOT_CONNECT_LIMIT = 5;

    public static void main(String[] args) {
        System.out.println("Running Java client");
        YourSolver.main(args);
    }
}
