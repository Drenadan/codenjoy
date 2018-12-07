package com.codenjoy.dojo.services;

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


import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Когда пользователь зарегистрировался в игре создается новая игра в движке и джойстик игрока где-то там сохраняется во фреймворке.
 * Часто джойстик неразрывно связан с героем игрока, который бегает по полю. Так вот если этот герой помрет, и на его место появится новый
 * надо как-то вот тот изначально сохраненный в недрах фреймворка джойстик обновить. Приходится дергать постоянно game.
 * Кроме того из за ассинхронности ответов пришлось буфферизировать в этом джойстике ответ клиента, а по tick() передавать сохраненное
 * значение сервису.
 */
public class LazyJoystick implements Joystick, Tickable {

    private final Game game;

    private List<Consumer<Joystick>> commands = new LinkedList<>();

    public LazyJoystick(Game game) {
        this.game = game;
    }

    @Override
    public void down() {
        commands.add(Joystick::down);
    }

    @Override
    public void up() {
        commands.add(Joystick::up);
    }

    @Override
    public void left() {
        commands.add(Joystick::left);
    }

    @Override
    public void right() {
        commands.add(Joystick::right);
    }

    @Override
    public void act(int... parameters) {
        if (parameters == null) {
            return;
        }
        commands.add(joystick -> joystick.act(parameters));
    }

    @Override
    public void message(String message) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
        commands.add(joystick -> joystick.message(message));
    }

    @Override
    public void tick() {
        Joystick joystick = game.getJoystick();

        commands.forEach(command -> command.accept(joystick));

        commands.clear();
    }
}
