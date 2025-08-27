package com.sfr.tokyo.sfr_backend.util;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaInspectorMain {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(com.sfr.tokyo.sfr_backend.SfrBackendApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("default")
                .run();
        try (ctx) {
            DataSource ds = ctx.getBean(DataSource.class);
            try (Connection con = ds.getConnection(); Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("SHOW COLUMNS FROM council_peer_evaluations")) {
                    System.out.println("=== council_peer_evaluations columns ===");
                    while (rs.next()) {
                        System.out.println(rs.getString("Field") + "\t" + rs.getString("Type"));
                    }
                }
            }
        }
    }
}