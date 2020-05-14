package net.thenova.titan.spigot.module.auctionhouse.database.tables;

import net.thenova.titan.library.database.sql.table.DatabaseTable;
import net.thenova.titan.library.database.sql.table.column.TableColumn;
import net.thenova.titan.library.database.sql.table.column.data_type.*;
import net.thenova.titan.spigot.module.auctionhouse.database.DatabaseAH;

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
public final class DBTableAuctions extends DatabaseTable {

    public DBTableAuctions() {
        super("ah_auctions", new DatabaseAH());
    }

    @Override
    public void init() {
        registerColumn(
                new TableColumn("auction_id", new IntAutoIncrement()).setPrimary(),
                new TableColumn("player_uuid", new VarChar(VarChar.LENGTH_UUID)).setNullable(false),
                new TableColumn("auction_item", new Text()).setNullable(false),
                new TableColumn("auction_status", new VarChar(VarChar.LENGTH_NAME)).setNullable(false),
                new TableColumn("auction_price", new Int()).setNullable(false).setDefault(0),
                new TableColumn("auction_expire", new BigInt()).setNullable(false).setDefault(-1),
                new TableColumn("staff_id", new VarChar(VarChar.LENGTH_UUID)).setNullable(true)
        );
    }
}
