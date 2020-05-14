package net.thenova.titan.spigot.module.auctionhouse;

import de.arraying.kotys.JSONArray;
import net.thenova.titan.library.command.data.Command;
import net.thenova.titan.library.database.connection.IDatabase;
import net.thenova.titan.library.database.sql.table.DatabaseTable;
import net.thenova.titan.spigot.data.message.MessageHandler;
import net.thenova.titan.spigot.module.auctionhouse.database.DatabaseAH;
import net.thenova.titan.spigot.module.auctionhouse.database.tables.DBTableAuctions;
import net.thenova.titan.spigot.module.auctionhouse.handler.AHHandler;
import net.thenova.titan.spigot.plugin.IPlugin;
import net.thenova.titan.spigot.users.user.module.UserModule;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.List;

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
public class AuctionHouse implements IPlugin {
    @Override
    public String name() {
        return "AuctionHouse";
    }

    @Override
    public void load() {
        AHHandler.INSTANCE.load();
    }

    @Override
    public void messages(final MessageHandler handler) {
        // Prefix
        handler.add("prefix.auctionhouse", "&8[&6AuctionHouse&8]&7");
        // Help Command
        handler.add("module.auctionhouse.help", new JSONArray().append("",
                "&d/auction sell <price> &7- Sell the item you're holding",
                "&d/auction listed &7- View the items you're currently auctioning",
                "&d/auction expired &7- View your expired items",
                "",
                "&7You can list a total of &e%amount% &7items in auction."));

        // Sell Sub Command
        handler.add("module.auctionhouse.sell.numerical", "%prefix.error% The price must be numerical.");
        handler.add("module.auctionhouse.sell.max-price", "%prefix.error% You have exceeded the maximum sell price.");
        handler.add("module.auctionhouse.sell.no-item", "%prefix.error% You must be holding an item to auction it.");
        handler.add("module.auctionhouse.sell.price-zero", "%prefix.error% The price cannot be less than &c0&7.");
        handler.add("module.auctionhouse.sell.max-auctions", "%prefix.error% You've already reached the maximum auction listings.");
        handler.add("module.auctionhouse.sell.success", "%prefix.auctionhouse% Your item has been added to the auction listings. " +
                "It will be listed for the next %duration% hours.");

        // Buy Item
        handler.add("module.auctionhouse.buy.own-item", "%prefix.error% You cannot buy your own item.");
        handler.add("module.auctionhouse.buy.insufficient-funds", "%prefix.error% You do not have enough money to buy this.");
        handler.add("module.auctionhouse.buy.not-available", "%prefix.error% This item is no longer available for purchase.");
        handler.add("module.auctionhouse.buy.inventory-space", "%error.prefix% You need to clear space in your inventory before you can purchase this.");
        handler.add("module.auctionhouse.buy.success", "%prefix.auctionhouse% The items you purchased have been added to your inventory");
        handler.add("module.auctionhouse.sold.online", "%prefix.auctionhouse% One of your auctioned items has been purchased by &e%player%&7.");

        // Expired Item
        handler.add("module.auctionhouse.expired.has-expired", "%prefix.auctionhouse% One of your auctions items has expired. Use &e/auc expired &7to reclaim it.");
        handler.add("module.auctionhouse.expired.no-expired", "%prefix.error% You do not currently have any expired items to reclaim");
        handler.add("module.auctionhouse.expired.returned", "%prefix.auctionhouse% Your items have been returned to your inventory.");
        handler.add("module.auctionhouse.expired.inventory-space", "%prefix.error% You need to clear space in your inventory before your items can be returned");

        // Cancel Item
        handler.add("module.auctionhouse.cancel.failed", "%prefix.error% This item has since been purchased and can no longer be cancelled");
        handler.add("module.auctionhouse.cancel.success", "%prefix.auctionhouse% You have cancelled one of your auctioned items. Use &e/auc expired &7to recliam it.");

        // Sold Items
        handler.add("module.auctionhouse.sold.offline", "%prefix.auctionhouse% &e%amount% &7of your items were sold whilst you were offline.");

        // Confiscate Item
        handler.add("module.auctionhouse.confiscated.user", "%prefix.auctionhouse% One of your auctions has been confiscated by a member of staff and is now pending review.");
        handler.add("module.auctionhouse.confiscated.success", "%prefix.auctionhouse% You have confiscated an item from auctionhouse and is now pending review.");
        handler.add("module.auctionhouse.confiscated.inform", "%prefix.auctionhouse% There is &e%amount% &7confiscated items awaiting review.");
        handler.add("module.auctionhouse.confiscated.manage.delete", "%prefix.auctionhouse% The item has been deleted");
        handler.add("module.auctionhouse.confiscated.manage.return", "%prefix.auctionhouse% The item has been returned added to the players expired auctions");

    }

    @Override
    public void reload() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public IDatabase database() {
        return new DatabaseAH();
    }

    @Override
    public List<DatabaseTable> tables() {
        return Collections.singletonList(new DBTableAuctions());
    }

    @Override
    public List<Listener> listeners() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Command> commands() {
        return null;
    }

    @Override
    public List<Class<? extends UserModule>> user() {
        return null;
    }
}
