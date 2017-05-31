package com.github.shynixn.petblocks.business.logic.persistence.controller;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import com.github.shynixn.petblocks.api.entities.MoveType;
import com.github.shynixn.petblocks.api.entities.Movement;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.business.logic.persistence.Factory;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PetMetaMySQLControllerTest {

    private static Plugin mockPlugin() {
        final YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("sql.local", true);
        configuration.set("sql.host", "localhost");
        configuration.set("sql.port", 3306);
        configuration.set("sql.database", "db");
        configuration.set("sql.username", "root");
        configuration.set("sql.password", "");
        final Plugin plugin = mock(Plugin.class);
        new File("PetBlocks.db").delete();
        when(plugin.getDataFolder()).thenReturn(new File("PetBlocks"));
        when(plugin.getConfig()).thenReturn(configuration);
        when(plugin.getResource(any(String.class))).thenAnswer(invocationOnMock -> {
            final String file = invocationOnMock.getArgument(0);
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        });
        return plugin;
    }


    private static DB database;

    @AfterClass
    public static void stopMariaDB()
    {
        try {
            database.stop();
        } catch (final ManagedProcessException e) {
            Logger.getLogger(PetMetaMySQLControllerTest.class.getSimpleName()).log(Level.WARNING, "Failed stop maria db.", e);
        }
    }

    @BeforeClass
    public static void startMariaDB() {
        try {
            Factory.disable();
            database = DB.newEmbeddedDB(3306);
            database.start();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=")) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate("CREATE DATABASE db");
                }
            }
        } catch (SQLException | ManagedProcessException e) {
            Logger.getLogger(PetMetaMySQLControllerTest.class.getSimpleName()).log(Level.WARNING, "Failed start maria db.", e);
        }
    }

    @Test
    public void insertSelectPlayerMetaTest() throws ClassNotFoundException {
        final Plugin plugin = mockPlugin();
        plugin.getConfig().set("sql.local", false);
        Factory.initialize(mockPlugin());
        final UUID uuid = UUID.randomUUID();
        final Player player = mock(Player.class);
        when(player.getName()).thenReturn("Shynixn");
        when(player.getUniqueId()).thenReturn(uuid);
        try (PetMetaController controller = Factory.createPetDataController()) {
            try (ParticleEffectMetaController particleController = Factory.createParticleEffectController()) {
                try (PlayerMetaController playerController = Factory.createPlayerDataController()) {
                    for (final PetMeta item : controller.getAll()) {
                        controller.remove(item);
                    }
                    final PetMeta meta = new PetData();
                    meta.setDisplayName("Notch");
                    assertThrows(IllegalArgumentException.class, () -> controller.store(meta));
                    assertEquals(0, controller.size());

                    final ParticleEffectMeta particleEffectMeta = particleController.create();
                    particleEffectMeta.setEffectType(ParticleEffectMeta.ParticleEffectType.END_ROD);
                    particleController.store(particleEffectMeta);
                    meta.setParticleEffectMeta(particleEffectMeta);

                    assertThrows(IllegalArgumentException.class, () -> controller.store(meta));
                    assertEquals(0, controller.size());

                    final PlayerMeta playerMeta = playerController.create(player);
                    playerController.store(playerMeta);
                    meta.setPlayerMeta(playerMeta);
                    assertThrows(IllegalArgumentException.class, () -> controller.store(meta));

                    meta.setPetType(PetType.BAT);
                    assertThrows(IllegalArgumentException.class, () -> controller.store(meta));

                    meta.setSkin(Material.STONE, (short) 5, null);
                    controller.store(meta);

                    assertEquals(1, controller.size());
                    assertEquals("Notch", controller.getByPlayer(player).getDisplayName());
                }
            }
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }


    @Test
    public void storeLoadPlayerMetaTest() throws ClassNotFoundException {
        final Plugin plugin = mockPlugin();
        plugin.getConfig().set("sql.local", false);
        Factory.initialize(mockPlugin());
        final UUID uuid = UUID.randomUUID();
        final Player player = mock(Player.class);
        when(player.getName()).thenReturn("Shynixn");
        when(player.getUniqueId()).thenReturn(uuid);
        try (PetMetaController controller = Factory.createPetDataController()) {
            try (ParticleEffectMetaController particleController = Factory.createParticleEffectController()) {
                try (PlayerMetaController playerController = Factory.createPlayerDataController()) {
                    for (final PetMeta item : controller.getAll()) {
                        controller.remove(item);
                    }
                    PetMeta meta = new PetData();
                    meta.setDisplayName("Me");
                    meta.setSkin(Material.BIRCH_DOOR_ITEM,(short)5 , "This is my long skin.");
                    meta.setPetType(PetType.SHEEP);
                    meta.setEnabled(true);
                    meta.setAgeInTicks(500);
                    meta.setUnbreakable(true);
                    meta.setSoundsEnabled(true);
                    meta.setMoveType(MoveType.FLYING);
                    meta.setMovementType(Movement.CRAWLING);

                    final ParticleEffectMeta particleEffectMeta = particleController.create();
                    particleEffectMeta.setEffectType(ParticleEffectMeta.ParticleEffectType.END_ROD);
                    particleController.store(particleEffectMeta);
                    meta.setParticleEffectMeta(particleEffectMeta);

                    final PlayerMeta playerMeta = playerController.create(player);
                    playerController.store(playerMeta);
                    meta.setPlayerMeta(playerMeta);
                    controller.store(meta);

                    assertEquals(1, controller.size());
                    meta = controller.getById(meta.getId());
                    assertEquals("Me", meta.getDisplayName());
                    assertEquals(Material.BIRCH_DOOR_ITEM, meta.getSkinMaterial());
                    assertEquals((short)5, meta.getSkinDurability());
                    assertEquals("This is my long skin.", meta.getSkin());
                    assertEquals(PetType.SHEEP, meta.getType());
                    assertEquals(true, meta.isEnabled());
                    assertEquals(500, meta.getAgeInTicks());
                    assertEquals(true, meta.isUnbreakable());
                    assertEquals(true, meta.isSoundsEnabled());
                    assertEquals(MoveType.FLYING, meta.getMoveType());
                    assertEquals(Movement.CRAWLING, meta.getMovementType());

                    meta.setDisplayName("PikaPet");
                    meta.setSkin(Material.ARROW,(short)7 , "http://Skin.com");
                    meta.setPetType(PetType.DRAGON);
                    meta.setEnabled(false);
                    meta.setAgeInTicks(250);
                    meta.setUnbreakable(false);
                    meta.setSoundsEnabled(false);
                    meta.setMoveType(MoveType.WALKING);
                    meta.setMovementType(Movement.HOPPING);
                    controller.store(meta);

                    assertEquals(1, controller.size());
                    meta = controller.getById(meta.getId());
                    assertEquals("PikaPet", meta.getDisplayName());
                    assertEquals(Material.ARROW, meta.getSkinMaterial());
                    assertEquals((short)7, meta.getSkinDurability());
                    assertEquals("http://Skin.com", meta.getSkin());
                    assertEquals(PetType.DRAGON, meta.getType());
                    assertEquals(false, meta.isEnabled());
                    assertEquals(250, meta.getAgeInTicks());
                    assertEquals(false, meta.isUnbreakable());
                    assertEquals(false, meta.isSoundsEnabled());
                    assertEquals(MoveType.WALKING, meta.getMoveType());
                    assertEquals(Movement.HOPPING, meta.getMovementType());
                }
            }
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }
}
