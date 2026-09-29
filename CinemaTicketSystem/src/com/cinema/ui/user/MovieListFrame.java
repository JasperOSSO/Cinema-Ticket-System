
package com.cinema.ui.user;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

/**
 * 顯示電影清單，並提供每部電影查詢場次按鈕
 */
public class MovieListFrame extends JFrame {
    public MovieListFrame() {
        setTitle("電影列表查詢");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Movie");

            while (rs.next()) {
                int uid = rs.getInt("uid");
                String title = rs.getString("title");
                String level = rs.getString("classification");
                int length = rs.getInt("duration");
                String summary = rs.getString("summary");

                JTextArea area = new JTextArea();
                area.setEditable(false);
                area.setText("🎬 電影名稱: " + title +
                             "\n分　　級: " + level +
                             "\n片　　長: " + length + " 分鐘" +
                             "\n簡　　介: " + summary);
                area.setBorder(new EmptyBorder(10, 10, 10, 10));

                JButton btn = new JButton("查詢場次");
                btn.addActionListener(e -> new SessionListFrame(uid, title));

                JPanel entry = new JPanel();
                entry.setLayout(new BorderLayout());
                entry.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                entry.add(area, BorderLayout.CENTER);
                entry.add(btn, BorderLayout.SOUTH);

                contentPanel.add(entry);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLocationRelativeTo(null); // 視窗置中
        setVisible(true);
    }
}
