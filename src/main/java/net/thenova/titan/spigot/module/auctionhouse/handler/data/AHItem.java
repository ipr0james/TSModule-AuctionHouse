package net.thenova.titan.spigot.module.auctionhouse.handler.data;

import net.thenova.titan.library.Titan;
import net.thenova.titan.library.database.sql.SQLQuery;
import net.thenova.titan.spigot.data.message.MessageHandler;
import net.thenova.titan.spigot.data.message.placeholders.Placeholder;
import net.thenova.titan.spigot.data.message.placeholders.PlayerPlaceholder;
import net.thenova.titan.spigot.module.auctionhouse.database.DatabaseAH;
import net.thenova.titan.spigot.module.auctionhouse.handler.AHHandler;
import net.thenova.titan.spigot.users.UUIDCache;
import net.thenova.titan.spigot.users.user.User;
import net.thenova.titan.spigot.util.UValidate;
import net.thenova.titan.spigot.util.UVault;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
public class AHItem {

    private int id;

    /* Seller Information */
    private UUID uuid;
    private String name;

    /* Item Information */
    private ItemStack item;
    private long price;

    /* Auction Information */
    private AHState state;
    private long expireTime;

    /* Confiscated Information */
    private UUID staffId;

    public AHItem(final int id) {
        this.id = id;

        new SQLQuery(new DatabaseAH(), "SELECT * FROM `ah_auctions` WHERE `auction_id` = ?", id)
                .execute(res -> {
                    try {
                        if(res.next()) {
                            final UUID uuid = UUID.fromString(res.getString("player_uuid"));
                            this.uuid = uuid;
                            this.name = UUIDCache.INSTANCE.getName(uuid);

                            this.item = null; // TODO ITEM DE-SERIALIZATION
                            this.price = res.getLong("auction_price");

                            this.state = AHState.valueOf(res.getString("auction_status"));
                            this.expireTime = res.getLong("auction_expire");

                            final String staffId = res.getString("staff_id");
                            if(staffId != null) {
                                this.staffId = UUID.fromString(staffId);
                            }
                        }
                    } catch (final SQLException ex) {
                        Titan.INSTANCE.getLogger().info("[Module] [AuctionHouse] [AHItem] - Failed to select load in data.", ex);
                    }
                });
    }

    private AHItem(final User user, final ItemStack item, final long price) {
        this.uuid = user.getUUID();
        this.name = user.getName();

        this.item = item;
        this.price = price;

        this.state = AHState.AUCTION;
        this.expireTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(AHHandler.INSTANCE.getConfig().get("sell-duration", 48, Long.class));

        new SQLQuery(new DatabaseAH(), "INSERT INTO `ah_auctions` (`player_uuid`, `auction_item`, `auction_status`, `auction_price`, `auction_expire`) VALUES (?, ?, ?, ?, ?)",
                this.uuid, null, this.state.toString(), this.price, this.expireTime)
                .execute(() -> {
                    new SQLQuery(new DatabaseAH(), "SELECT last_insert_id() AS `last_id` FROM `ah_auctions`")
                            .execute(res -> {
                                try {
                                    if(res.next()) {
                                        this.id = res.getInt("last_id");
                                    }
                                } catch (final SQLException ex) {
                                    Titan.INSTANCE.getLogger().info("[Module] [AuctionHouse] [AHItem] - Failed to select `last_insert_id()`.", ex);
                                }
                            });
                });
        //TODO ITEM SERIALIZATION
    }

    public synchronized static AHItem create(final User user, final ItemStack item, final long price) {
        return new AHItem(user, item, price);
    }

    public void purchase(final User user) {
        final Player player = user.getPlayer();
        if(user.getUUID().equals(this.uuid)) {
            MessageHandler.INSTANCE.build("auctionhouse.buy.own-item").send(user);
            return;
        }

        if(!UVault.has(player, this.price)) {
            MessageHandler.INSTANCE.build("auctionhouse.buy.insufficient-funds")
                    .placeholder(new Placeholder("cost", this.price))
                    .send(user);
            return;
        }

        if(!AHHandler.INSTANCE.isListed(this)) {
            MessageHandler.INSTANCE.build("auctionhouse.buy.not-available").send(user);
            return;
        }

        if(!UValidate.hasInventorySpace(player)) {
            MessageHandler.INSTANCE.build("auctionhouse.buy.inventory-space").send(user);
            return;
        }

        player.getInventory().addItem(this.item);
        player.closeInventory();

        UVault.give(Bukkit.getOfflinePlayer(this.uuid), this.price);
        UVault.take(player, this.price);

        MessageHandler.INSTANCE.build("auctionhouse.buy.success").send(player);

        final Player owner = Bukkit.getPlayer(this.uuid);
        if(owner != null) {
            AHHandler.INSTANCE.delete(this);
            MessageHandler.INSTANCE.build("auctionhouse.sold.online")
                    .placeholder(new PlayerPlaceholder(player))
                    .send(owner);
        } else {
            this.setState(AHState.SOLD);
        }
    }

    public void cancel() {
        final Player player = Bukkit.getPlayer(uuid);
        if(!AHHandler.INSTANCE.isListed(this)) {
            MessageHandler.INSTANCE.build("auctionhouse.cancel.failed").send(player);
            return;
        }

        this.setState(AHState.EXPIRED);
        MessageHandler.INSTANCE.build("auctionhouse.cancel.success").send(player);

    }

    public void expire() {
    }

    public void delete() {
        new SQLQuery(new DatabaseAH(), "DELETE FROM `ah_auctions` WHERE `auction_id` = ?", this.id).execute();
    }

    public void confiscate(final User staff) {
        if(!AHHandler.INSTANCE.isListed(this)) {
            MessageHandler.INSTANCE.build("auctionhouse.cancel.failed").send(staff);
            return;
        }

        final Player player = Bukkit.getPlayer(this.uuid);
        if(player != null) {
            MessageHandler.INSTANCE.build("auctionhouse.confiscated.user").send(player);
        }

        this.setState(AHState.CONFISCATED);
        MessageHandler.INSTANCE.build("auctionhouse.confiscated.success").send(staff);
    }

    /* Setters */
    public final void setState(final AHState state) {
        this.state = state;
        new SQLQuery(new DatabaseAH(), "UPDATE `ah_auctions` SET (`auction_status`, `auction_expire`) = (?,?) WHERE `auction_id` = ?",
                state.toString(), -1, this.id)
                .execute();
    }

    /* Getters */
    public final UUID getUUID() {
        return this.uuid;
    }
    public final String getName() {
        return this.name;
    }

    public final ItemStack getItem() {
        return this.item;
    }
    public final long getPrice() {
        return this.price;
    }

    public final AHState getState() {
        return this.state;
    }
    public final long getExpireTime() {
        return this.expireTime;
    }
}
