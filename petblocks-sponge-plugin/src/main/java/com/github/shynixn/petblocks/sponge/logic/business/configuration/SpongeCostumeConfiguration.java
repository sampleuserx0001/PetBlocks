package com.github.shynixn.petblocks.sponge.logic.business.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.core.logic.persistence.configuration.CostumeConfiguration;
import com.github.shynixn.petblocks.sponge.logic.business.entity.ItemContainer;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Shynixn 2017.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
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
public class SpongeCostumeConfiguration extends CostumeConfiguration {

    @Inject
    private PluginContainer plugin;

    @Inject
    private Config config;

    /**
     * Initializes a new engine repository
     */
    public SpongeCostumeConfiguration() {
        super("ordinary");
    }

    /**
     * Reloads the content from the fileSystem
     */
    @Override
    public void reload() {
        System.out.println("CALLED 1");
        this.items.clear();
        this.config.reload();
        final ConfigurationNode node = this.config.getData("wardrobe.ordinary");
        System.out.println("CALLED 2");
        final Map<Object, ? extends ConfigurationNode> data = node.getChildrenMap();
        for (final Object key : data.keySet()) {
            try {
                System.out.println("KEY  3 " + key);
                final GUIItemContainer container = new ItemContainer(Integer.parseInt((String) key), data.get(key));
                this.items.add(container);
                System.out.println("ADDED");
            } catch (final Exception e) {
                this.plugin.getLogger().info("Failed to load guiItem " + this.costumeCategory + '.' + key + '.');
            }
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        super.close();
        this.plugin = null;
    }
}