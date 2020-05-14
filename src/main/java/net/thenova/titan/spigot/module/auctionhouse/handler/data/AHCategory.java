package net.thenova.titan.spigot.module.auctionhouse.handler.data;

import de.arraying.kotys.JSONArray;
import de.arraying.kotys.JSONField;
import net.thenova.titan.library.Titan;
import net.thenova.titan.spigot.data.compatability.model.CompMaterial;
import net.thenova.titan.spigot.module.auctionhouse.handler.AHHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class AHCategory {
    @JSONField(key = "name") private String name;
    @JSONField(key = "slot") private Integer slot;
    @JSONField(key = "item")  private String materialField;

    @JSONField(key = "lore") private JSONArray lore;
    @JSONField(key = "types") private JSONArray types;

    private CompMaterial _material;
    private final List<String> _lore = new ArrayList<>();
    private final List<CompMaterial> _types = new ArrayList<>();

    public void load() {
        try {
            this._material = CompMaterial.valueOf(this.materialField);
        } catch (IllegalArgumentException ex) {
            Titan.INSTANCE.getLogger().info("[Module] [AuctionHouse] [Category] - Failed to load '"
                    + this.name + "', '" + this._material + "' is not a valid material.");
            this._material = CompMaterial.AIR;
        }

        for(int i = 0; i < this.lore.length(); i++) {
            this._lore.add(this.lore.string(i));
        }

        label:
        for(int i = 0; i < this.types.length(); i++) {
            final String mat = this.types.string(i);

            switch(mat.toLowerCase()) {
                case "%edible%":
                    this._types.addAll(Arrays.stream(CompMaterial.values())
                            .filter(material -> material.getMaterial().isEdible())
                            .collect(Collectors.toList()));
                    continue label;
                case "%blocks%":
                    this._types.addAll(Arrays.stream(CompMaterial.values())
                            .filter(material -> material.getMaterial().isBlock())
                            .collect(Collectors.toList()));
                    continue label;
                case "%others%":
                    final List<AHCategory> categories = AHHandler.INSTANCE.getCategories();

                    this._types.addAll(Arrays.stream(CompMaterial.values())
                            .filter(material -> categories.stream().noneMatch(cat -> cat.containsType(material)))
                            .collect(Collectors.toList()));
                    continue label;
                case "%all%":
                    this._types.addAll(Arrays.asList(CompMaterial.values()));
                    continue label;
            }
            try {
                this._types.add(CompMaterial.valueOf(mat));
            } catch (IllegalArgumentException ex) {
                Titan.INSTANCE.getLogger().info("[Module] [AuctionHouse] [Category] - Failed to add '{}' to types as it is not a valid material.", mat.toString());
            }
        }

    }

    public final String getName() {
        return this.name;
    }
    public final Integer getSlot() {
        return this.slot;
    }
    public CompMaterial getMaterial() {
        return this._material;
    }

    public List<String> getLore() {
        return this._lore;
    }
    public List<CompMaterial> getTypes() {
        return this._types;
    }

    public Boolean containsType(CompMaterial material) {
        return this._types.contains(material);
    }
}
