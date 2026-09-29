
package com.cinema.ui.user;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * 顯示會員的歷史訂單（包含：
 * - 已觀看（時間 < 現在）
 * - 已退票（status = 'cancelled'）
 */
public class UserHistoryFrame extends JFrame {
    public UserHistoryFrame(String email) {
        setTitle("歷史訂單紀錄");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("歷史訂票紀錄", SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"訂單ID", "電影", "時間", "票種", "座位", "金額", "狀態"}, 0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton backBtn = new JButton("回主頁");
        add(backBtn, BorderLayout.SOUTH);
        backBtn.addActionListener(e -> {
            dispose();
            new UserHomeFrame(email, getUidByEmail(email));
        });

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            int uid = getUidByEmail(email);

            // 查詢會員的歷史紀錄（退票 or 已過場次）
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT b.booking_id, m.title, b.start_time, b.row, b.seat_number, b.ticket_type, b.price, b.status " +
                "FROM Booking b JOIN Movie m ON b.movie_uid = m.uid " +
                "WHERE b.member_uid = ?"
            );
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            LocalDateTime now = LocalDateTime.now();

            while (rs.next()) {
            	int bookingId = rs.getInt("booking_id");
                String startTime = rs.getString("start_time");
                String status = rs.getString("status");
                String ticket_type = rs.getString("ticket_type");
                String price = rs.getString("price");
                LocalDateTime showTime = LocalDateTime.parse(startTime);
                boolean isPast = showTime.isBefore(now);
                boolean isCancelled = "cancelled".equalsIgnoreCase(status);

                if (isPast || isCancelled) {
                    String seat = rs.getString("row") + rs.getInt("seat_number");
                    model.addRow(new Object[]{
                    	bookingId, 
                        rs.getString("title"),
                        startTime,
                        ticket_type,
                        seat,
                        price,
                        isCancelled ? "已退票" : "已看過"
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private int getUidByEmail(String email) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM Member WHERE email=?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("uid");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
