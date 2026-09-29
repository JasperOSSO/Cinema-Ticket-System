package com.cinema.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * 初始化資料庫結構用程式：
 * 執行 schema.sql 來建立所有資料表。
 * Booking 表含 status 欄位（active / cancelled）
 */
public class DBInit {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:cinema.db";             // 資料庫檔案位置
        String schemaFile = "schema.sql";                 // SQL 腳本檔

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(schemaFile))) {

            StringBuilder sql = new StringBuilder();
            String line;

            // 一行一行讀入 schema.sql，遇到分號就執行
            while ((line = reader.readLine()) != null) {
                sql.append(line);
                if (line.trim().endsWith(";")) {
                    stmt.execute(sql.toString());
                    sql.setLength(0); // 清空建構器
                } else {
                    sql.append("\n");
                }
            }

            System.out.println("✅ 資料庫初始化完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
