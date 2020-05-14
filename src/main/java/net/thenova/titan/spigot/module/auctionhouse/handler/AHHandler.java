package net.thenova.titan.spigot.module.auctionhouse.handler;

import de.arraying.kotys.JSON;
import net.thenova.titan.library.Titan;
import net.thenova.titan.library.database.sql.SQLQuery;
import net.thenova.titan.library.file.FileHandler;
import net.thenova.titan.library.file.json.JSONFile;
import net.thenova.titan.spigot.TitanSpigot;
import net.thenova.titan.spigot.module.auctionhouse.database.DatabaseAH;
import net.thenova.titan.spigot.module.auctionhouse.handler.data.AHCategory;
import net.thenova.titan.spigot.module.auctionhouse.handler.data.AHDataFile;
import net.thenova.titan.spigot.module.auctionhouse.handler.data.AHItem;
import net.thenova.titan.spigot.module.auctionhouse.handler.data.AHState;
import net.thenova.titan.spigot.util.task.TaskHandler;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
public enum AHHandler {
    INSTANCE;

    private JSONFile config;

    private final List<AHCategory> categories = new ArrayList<>();
    private final Set<AHItem> items = new HashSet<>();

    public final void load() {
        this.config = FileHandler.INSTANCE.loadJSONFile(AHDataFile.class);

        new SQLQuery(new DatabaseAH(), "SELECT `auction_id` FROM `ah_auctions`").execute(res -> {
            try {
                while (res.next()) {
                    this.items.add(new AHItem(res.getInt("auction_id")));
                }
            } catch (final SQLException ex) {
                Titan.INSTANCE.getLogger().info("[Module] [AuctionHouse] [AHHandler] - Failed to load data from `ah_auctions`", ex);
            }
        });

        if (this.config.get("categories.enabled", Boolean.class)) {
            final JSON categories = this.config.get("categories.types", JSON.class);

            categories.raw().keySet()
                    .forEach(cat -> {
                        final AHCategory category = categories.json(cat).marshal(AHCategory.class);
                        category.load();
                        this.categories.add(category);
                    });
        }

        TaskHandler.INSTANCE.addTask(new BukkitRunnable() {
            @Override
            public void run() {
                AHHandler.this.items.stream()
                        .filter(item -> item.getState() == AHState.AUCTION
                                && item.getExpireTime() <= System.currentTimeMillis())
                        .forEach(AHItem::expire);
            }
        }.runTaskTimerAsynchronously(TitanSpigot.INSTANCE.getPlugin(), 0, 20 * 5)
                .getTaskId());
    }

    /**
     * Check whether an item is currently classed as Listed
     *
     * @param item - Item to be checked
     * @return - Return true of AHState is Auction meaning in auction currently
     */
    public boolean isListed(final AHItem item) {
        return item.getState().equals(AHState.AUCTION);
    }

    /**
     * Add an AuctionItem to current stored
     *
     * @param item - Item being added
     */
    public void add(final AHItem item) {
        this.items.add(item);
    }

    /**
     * Delete an AuctionItem.
     * - Remove from current items
     * - Delete the item object, deleting from SQL
     *
     * @param item - Item being deleted
     */
    public void delete(final AHItem item) {
        this.items.remove(item);
        item.delete();
    }

    /**
     * Check how many auctions listings a player has permission for.
     *
     * @param player - Player being checked
     * @return - Return value based on permission
     */
    public int getMaxAuctions(Player player) {
        if(player.hasPermission("auctionhouse.admin")) {
            return Integer.MAX_VALUE;
        }

        for(int i = 20; i >= 1; i--) {
            if (player.hasPermission("auctionhouse.limit." + i)) {
                return i;
            }
        }
        return 0;
    }

    public Set<AHItem> fetch(final UUID uuid, final AHState state) {
        if(uuid == null) {
            if(state == null) {
                return this.items;
            } else {
                return this.items
                        .stream()
                        .filter(auction -> auction.getState() == state)
                        .collect(Collectors.toSet());
            }
        } else {
            if(state == null) {
                return this.items
                        .stream()
                        .filter(auction -> auction.getUUID().equals(uuid))
                        .collect(Collectors.toSet());
            } else {
                return this.items
                        .stream()
                        .filter(auction -> auction.getUUID().equals(uuid)
                                && auction.getState() == state)
                        .collect(Collectors.toSet());
            }
        }
    }

    public final JSONFile getConfig() {
        return this.config;
    }
    public final List<AHCategory> getCategories() {
        return this.categories;
    }
}
