package net.thenova.titan.spigot.module.auctionhouse.commands.subs;

import net.thenova.titan.library.command.data.CommandContext;
import net.thenova.titan.library.command.data.CommandPermission;
import net.thenova.titan.library.command.data.CommandUsage;
import net.thenova.titan.library.util.UNumber;
import net.thenova.titan.spigot.command.SpigotCommand;
import net.thenova.titan.spigot.data.message.MessageHandler;
import net.thenova.titan.spigot.data.message.placeholders.Placeholder;
import net.thenova.titan.spigot.module.auctionhouse.handler.AHHandler;
import net.thenova.titan.spigot.module.auctionhouse.handler.data.AHItem;
import net.thenova.titan.spigot.module.auctionhouse.handler.data.AHState;
import net.thenova.titan.spigot.users.user.User;
import net.thenova.titan.spigot.util.UValidate;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

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
@CommandUsage(
        min = 1,
        usage = "ah sell <price>",
        description = "Add an item to auction"
)
public final class SubCommandAHSell extends SpigotCommand<User> implements CommandPermission<User> {

    public SubCommandAHSell() {
        super("sell");
    }

    @SuppressWarnings("deprecation")
    @Override
    public final void execute(final User user, final CommandContext context) {
        if(!UValidate.stringIsNumerical(context.getArgument(0))) {
            MessageHandler.INSTANCE.build("module.auctionhouse.sell.numerical").send(user);
            return;
        }

        if(!UNumber.isLong(context.getArgument(0))) {
            MessageHandler.INSTANCE.build("module.auctionhouse.sell.max-price").send(user);
            return;
        }

        final ItemStack item;
        if(!UValidate.notNull((item = user.getPlayer().getItemInHand()))) {
            MessageHandler.INSTANCE.build("module.auctionhouse.sell.no-item").send(user);
            return;
        }

        final long price = Long.parseLong(context.getArgument(0));
        if(price < 0) {
            MessageHandler.INSTANCE.build("module.auctionhouse.sell.price-zero").send(user);
            return;
        }

        final Set<AHItem> auctions = AHHandler.INSTANCE.fetch(user.getUUID(), AHState.AUCTION);
        if(!auctions.isEmpty() && auctions.size() >= AHHandler.INSTANCE.getMaxAuctions(user.getPlayer())) {
            MessageHandler.INSTANCE.build("module.auctionhouse.sell.max-auctions").send(user);
            return;
        }

        AHHandler.INSTANCE.add(AHItem.create(user, item, price));
        user.getPlayer().setItemInHand(null);
        MessageHandler.INSTANCE.build("module.auctionhouse.sell.success")
                .placeholder(new Placeholder("duration", AHHandler.INSTANCE.getConfig().get("sell-duration")))
                .send(user);

    }

    @Override
    public final boolean hasPermission(final User user) {
        return user.hasPermission("titan.command.auctionhouse.user");
    }
}
