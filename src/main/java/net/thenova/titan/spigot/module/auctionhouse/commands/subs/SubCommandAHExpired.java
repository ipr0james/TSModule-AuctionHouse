package net.thenova.titan.spigot.module.auctionhouse.commands.subs;

import net.thenova.titan.library.command.data.CommandContext;
import net.thenova.titan.library.command.data.CommandPermission;
import net.thenova.titan.spigot.command.SpigotCommand;
import net.thenova.titan.spigot.users.user.User;

/**
 * Copyright 2019 ipr0james
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SubCommandAHExpired extends SpigotCommand<User> implements CommandPermission<User> {

    public SubCommandAHExpired(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public final void execute(final User user, final CommandContext context) {

    }

    @Override
    public final boolean hasPermission(final User user) {
        return user.hasPermission("titan.command.auctionhouse.user");
    }
}
