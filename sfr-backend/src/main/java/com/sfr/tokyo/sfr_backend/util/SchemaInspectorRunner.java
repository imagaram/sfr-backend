package com.sfr.tokyo.sfr_backend.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@Profile("schema-inspect")
public class SchemaInspectorRunner implements CommandLineRunner {

    private final DataSource dataSource;

    public SchemaInspectorRunner(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public void run(String... args) throws Exception {
        try (Connection con = dataSource.getConnection(); Statement st = con.createStatement()) {
            try (ResultSet rs = st.executeQuery("SHOW COLUMNS FROM council_peer_evaluations")) {
                System.out.println("=== council_peer_evaluations columns ===");
                while (rs.next()) {
                    System.out.println(rs.getString("Field") + "\t" + rs.getString("Type"));
                }
            } catch (Exception ex) {
                System.out.println("Failed to read columns: " + ex.getMessage());
            }
        }
    }
}