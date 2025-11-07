package org.ayosynk.storage;

import org.ayosynk.ArenaLite;
import org.ayosynk.models.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PlayerDataStorage {

    private enum StorageMode {
        YAML,
        MYSQL
    }

    private final ArenaLite plugin;
    private File dataFolder;

    private StorageMode storageMode = StorageMode.YAML;

    // SQL configuration
    private String jdbcUrl;
    private String username;
    private String password;
    private String tableName;
    private boolean driverLoaded;

    public PlayerDataStorage(ArenaLite plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        configure();
    }

    public void reload() {
        configure();
    }

    private synchronized void configure() {
        String type = plugin.getConfig().getString("storage.type", "YAML");
        StorageMode targetMode;
        try {
            targetMode = StorageMode.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Unknown storage type '" + type + "'. Falling back to YAML.");
            targetMode = StorageMode.YAML;
        }

        if (targetMode == StorageMode.MYSQL) {
            if (!setupMySql()) {
                plugin.getLogger().warning("Falling back to YAML storage due to MySQL configuration issues.");
                setupYaml();
            }
        } else {
            setupYaml();
        }
    }

    private void setupYaml() {
        storageMode = StorageMode.YAML;
        dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create playerdata folder at " + dataFolder.getAbsolutePath());
        }
        driverLoaded = false;
        jdbcUrl = null;
        username = null;
        password = null;
        tableName = null;
        plugin.getLogger().info("Using YAML-based player data storage!");
    }

    private boolean setupMySql() {
        FileConfiguration config = plugin.getConfig();

        String host = config.getString("storage.mysql.host", "localhost");
        int port = config.getInt("storage.mysql.port", 3306);
        String database = config.getString("storage.mysql.database", "arenalite");
        username = config.getString("storage.mysql.username", "root");
        password = config.getString("storage.mysql.password", "password");
        boolean useSsl = config.getBoolean("storage.mysql.use-ssl", false);
        String prefix = config.getString("storage.mysql.table-prefix", "arenalite_");

        tableName = sanitizeTableName(prefix + "player_data");

        jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=" + useSsl + "&autoReconnect=true&characterEncoding=utf8";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL driver not found. Add mysql-connector-java to your server.", e);
            driverLoaded = false;
            return false;
        }

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                    "`uuid` VARCHAR(36) NOT NULL PRIMARY KEY, " +
                    "`kills` INT NOT NULL DEFAULT 0, " +
                    "`deaths` INT NOT NULL DEFAULT 0, " +
                    "`streak` INT NOT NULL DEFAULT 0" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize MySQL storage.", e);
            return false;
        }

        storageMode = StorageMode.MYSQL;
        dataFolder = null;
        plugin.getLogger().info("Using MySQL-based player data storage!");
        return true;
    }

    private String sanitizeTableName(String name) {
        String sanitized = name.replaceAll("[^a-zA-Z0-9_]+", "");
        if (sanitized.isEmpty()) {
            sanitized = "arenalite_player_data";
        }
        return sanitized;
    }

    private Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("MySQL driver not loaded");
        }
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    public CompletableFuture<Void> loadPlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            if (storageMode == StorageMode.MYSQL) {
                loadFromMySql(data);
            } else {
                loadFromYaml(data);
            }
        });
    }

    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            if (storageMode == StorageMode.MYSQL) {
                saveToMySql(data);
            } else {
                saveToYaml(data);
            }
        });
    }

    private void loadFromYaml(PlayerData data) {
        File file = getPlayerFile(data.getUuid());
        if (!file.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        data.setKills(config.getInt("kills", 0));
        data.setDeaths(config.getInt("deaths", 0));
        data.setStreak(config.getInt("streak", 0));
    }

    private void saveToYaml(PlayerData data) {
        File file = getPlayerFile(data.getUuid());
        FileConfiguration config = new YamlConfiguration();

        config.set("uuid", data.getUuid().toString());
        config.set("kills", data.getKills());
        config.set("deaths", data.getDeaths());
        config.set("streak", data.getStreak());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getUuid(), e);
        }
    }

    private void loadFromMySql(PlayerData data) {
        String query = "SELECT kills, deaths, streak FROM `" + tableName + "` WHERE uuid = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, data.getUuid().toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    data.setKills(resultSet.getInt("kills"));
                    data.setDeaths(resultSet.getInt("deaths"));
                    data.setStreak(resultSet.getInt("streak"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data from MySQL for " + data.getUuid(), e);
        }
    }

    private void saveToMySql(PlayerData data) {
        String query = "INSERT INTO `" + tableName + "` (uuid, kills, deaths, streak) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE kills = VALUES(kills), deaths = VALUES(deaths), streak = VALUES(streak)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, data.getUuid().toString());
            statement.setInt(2, data.getKills());
            statement.setInt(3, data.getDeaths());
            statement.setInt(4, data.getStreak());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data to MySQL for " + data.getUuid(), e);
        }
    }

    private File getPlayerFile(UUID uuid) {
        if (dataFolder == null) {
            dataFolder = new File(plugin.getDataFolder(), "playerdata");
        }
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    public void close() {
        // No persistent resources to close; connections are handled per operation.
    }
}

