package com.github.shynixn.petblocks.bukkit.addon.buffeffects.logic.listener;

import com.github.shynixn.petblocks.api.bukkit.event.PetBlockDeathEvent;
import com.github.shynixn.petblocks.api.bukkit.event.PetBlockSpawnEvent;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.addon.buffeffects.api.controller.BuffEffectController;
import com.github.shynixn.petblocks.bukkit.addon.buffeffects.api.entity.BuffEffect;
import com.github.shynixn.petblocks.bukkit.lib.SimpleListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class PotionListener extends SimpleListener {
    private final BuffEffectController controller;

    private final Map<Player, BuffEffect[]> effects = new HashMap<>();

    public PotionListener(BuffEffectController effectController, Plugin plugin) {
        super(plugin);
        this.controller = effectController;
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        this.clearPotionEffectsFromPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPetBlockRemoveEvent(PetBlockDeathEvent event) {
        this.clearPotionEffectsFromPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPetBlockSpawnEvent(PetBlockSpawnEvent event) {
        final PetMeta petMeta = event.getPetBlock().getMeta();
        final Optional<BuffEffect[]> optPotion = this.controller.getPotionEffectsFromEngine((int) petMeta.getEngine().getId());
        if (optPotion.isPresent()) {
            final BuffEffect[] data = optPotion.get();
            for (final BuffEffect s : data) {
                final Player player = event.getPlayer();
                s.apply(player);
            }
            this.effects.put(event.getPlayer(), data);
        }
    }

    private void clearPotionEffectsFromPlayer(Player player) {
        if (this.effects.containsKey(player)) {
            for (final BuffEffect potionEffectBuilder : this.effects.get(player)) {
                player.removePotionEffect(potionEffectBuilder.getType());
            }
            this.effects.remove(player);
        }
    }
}
