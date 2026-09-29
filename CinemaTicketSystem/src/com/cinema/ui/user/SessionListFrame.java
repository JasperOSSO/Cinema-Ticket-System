
package com.cinema.ui.user;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 顯示選定電影的所有場次（時間、影廳、剩餘座位）
 */
public class SessionListFrame extends JFrame {
    public SessionListFrame(int movieUid, String movieTitle) {
        setTitle("場次查詢 - " + movieTitle);
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("電影：" + movieTitle, SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"時間", "影廳", "剩餘座位"}, 0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 查詢所有場次
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT s.start_time, s.room_uid, r.type " +
                "FROM Session s JOIN Room r ON s.room_uid = r.uid WHERE s.movie_uid=?");
            stmt.setInt(1, movieUid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String time = rs.getString("start_time");
                int roomUid = rs.getInt("room_uid");
                String type = rs.getString("type");

                // 查該場次已訂座位數
                PreparedStatement booked = conn.prepareStatement(
                    "SELECT COUNT(*) AS used FROM Booking WHERE movie_uid=? AND room_uid=? AND start_time=?");
                booked.setInt(1, movieUid);
                booked.setInt(2, roomUid);
                booked.setString(3, time);
                ResultSet brs = booked.executeQuery();
                int used = brs.next() ? brs.getInt("used") : 0;

                // 查該影廳總座位數
                PreparedStatement total = conn.prepareStatement("SELECT COUNT(*) FROM Seat WHERE room_uid=?");
                total.setInt(1, roomUid);
                ResultSet trs = total.executeQuery();
                int totalSeats = trs.next() ? trs.getInt(1) : 0;

                int remaining = totalSeats - used;
                model.addRow(new Object[]{time, "Room " + roomUid, remaining});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLocationRelativeTo(null); // 視窗置中
        setVisible(true);
    }
}
