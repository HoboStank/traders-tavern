package com.traderstavern.db;

import com.traderstavern.model.PriceData;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class DatabaseManager {
    private static final String DB_NAME = "traderstavern.db";
    private static final String DB_DIR = System.getProperty("user.home") + "/.runelite/traderstavern";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_NAME;
    private Connection connection;
    
    @Inject
    public DatabaseManager() {
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            // Create database directory if it doesn't exist
            File dbDir = new File(DB_DIR);
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            
            // Initialize database connection
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(false); // Enable transactions for better performance
            
            createTables();
            connection.commit();
            log.info("Database initialized successfully");
        } catch (SQLException e) {
            log.error("Failed to initialize database", e);
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException re) {
                log.error("Failed to rollback database initialization", re);
            }
        }
    }
    
    private void createTables() throws SQLException {
        // Price history table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS price_history (
                    item_id INTEGER NOT NULL,
                    high INTEGER NOT NULL,
                    low INTEGER NOT NULL,
                    high_timestamp BIGINT NOT NULL,
                    low_timestamp BIGINT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Create indices
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_price_history_item_id ON price_history(item_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_price_history_timestamps ON price_history(high_timestamp, low_timestamp)");
        }
    }
    
    public void savePriceData(int itemId, PriceData price) {
        String sql = """
            INSERT INTO price_history (item_id, high, low, high_timestamp, low_timestamp)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, price.getHigh());
            pstmt.setInt(3, price.getLow());
            pstmt.setLong(4, price.getHighTimestamp());
            pstmt.setLong(5, price.getLowTimestamp());
            pstmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            log.error("Failed to save price data for item {}", itemId, e);
            try {
                connection.rollback();
            } catch (SQLException re) {
                log.error("Failed to rollback price data save", re);
            }
        }
    }
    
    public List<PriceData> getPriceHistory(int itemId) {
        List<PriceData> history = new ArrayList<>();
        String sql = """
            SELECT high, low, high_timestamp, low_timestamp
            FROM price_history
            WHERE item_id = ?
            ORDER BY high_timestamp DESC
            LIMIT 100
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                PriceData price = PriceData.builder()
                    .itemId(itemId)
                    .high(rs.getInt("high"))
                    .low(rs.getInt("low"))
                    .highTimestamp(rs.getLong("high_timestamp"))
                    .lowTimestamp(rs.getLong("low_timestamp"))
                    .build();
                history.add(price);
            }
        } catch (SQLException e) {
            log.error("Failed to get price history for item {}", itemId, e);
        }
        
        return history;
    }
    
    public void cleanupOldData() {
        String sql = """
            DELETE FROM price_history
            WHERE high_timestamp < ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            long cutoff = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000); // 30 days
            pstmt.setLong(1, cutoff);
            int deleted = pstmt.executeUpdate();
            log.debug("Cleaned up {} old price records", deleted);
        } catch (SQLException e) {
            log.error("Failed to cleanup old data", e);
        }
    }
    
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Failed to close database connection", e);
        }
    }
}