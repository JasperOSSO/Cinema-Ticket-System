
package com.cinema.ui.employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;

/**
 * 營運人員查詢訂票紀錄畫面。
 * 可選擇電影與場次，顯示訂票資料，並標記退票狀態。
 */
public class AdminBookingViewFrame extends JFrame {
    private JComboBox<String> movieBox = new JComboBox<>();
    private JComboBox<String> sessionBox = new JComboBox<>();
    private Map<String, Integer> movieMap = new HashMap<>();
    private Map<String, String> sessionMap = new HashMap<>();
    private DefaultTableModel model;

    public AdminBookingViewFrame() {
        setTitle("查詢訂票紀錄");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.add(new JLabel("電影:"));
        top.add(movieBox);
        top.add(new JLabel("場次:"));
        top.add(sessionBox);
        JButton searchBtn = new JButton("查詢");
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"會員", "時間", "座位", "狀態"}, 0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton backBtn = new JButton("回管理首頁");
        backBtn.addActionListener(e -> {
            dispose();
            new AdminFrame();
        });
        add(backBtn, BorderLayout.SOUTH);

        loadMovies();
        movieBox.addActionListener(e -> loadSessions());
        searchBtn.addActionListener(e -> searchBookings());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadMovies() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT uid, title FROM Movie");
            movieBox.removeAllItems();
            movieMap.clear();
            while (rs.next()) {
                String title = rs.getString("title");
                movieBox.addItem(title);
                movieMap.put(title, rs.getInt("uid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSessions() {
        sessionBox.removeAllItems();
        sessionMap.clear();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            int movieId = movieMap.get((String) movieBox.getSelectedItem());
            PreparedStatement stmt = conn.prepareStatement("SELECT room_uid, start_time FROM Session WHERE movie_uid=?");
            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int room = rs.getInt("room_uid");
                String time = rs.getString("start_time");
                String label = time + " / Room " + room;
                sessionBox.addItem(label);
                sessionMap.put(label, time + "," + room);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchBookings() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            int movieId = movieMap.get((String) movieBox.getSelectedItem());
            String[] session = sessionMap.get((String) sessionBox.getSelectedItem()).split(",");
            String time = session[0];
            int room = Integer.parseInt(session[1]);

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT m.email, b.start_time, b.row, b.seat_number, b.status " +
                "FROM Booking b JOIN Member m ON b.member_uid = m.uid " +
                "WHERE b.movie_uid=? AND b.room_uid=? AND b.start_time=?");
            stmt.setInt(1, movieId);
            stmt.setInt(2, room);
            stmt.setString(3, time);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String email = rs.getString("email");
                String seat = rs.getString("row") + rs.getInt("seat_number");
                String status = rs.getString("status").equals("cancelled") ? "退票" : "有效";
                model.addRow(new Object[]{email, time, seat, status});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
