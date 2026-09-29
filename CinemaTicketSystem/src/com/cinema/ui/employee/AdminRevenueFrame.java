
package com.cinema.ui.employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminRevenueFrame extends JFrame {
    private JComboBox<String> movieBox = new JComboBox<>();
    private DefaultTableModel model = new DefaultTableModel(new String[]{"類型", "名稱", "收入"}, 0);
    private JTable table = new JTable(model);

    public AdminRevenueFrame() {
        setTitle("查詢帳目");
        setSize(800, 500);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel top = new JPanel();
        top.add(new JLabel("選擇電影:"));
        top.add(movieBox);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton backBtn = new JButton("返回控制台");
        backBtn.addActionListener(e -> {
            dispose();
            new AdminFrame();
        });
        JPanel bottom = new JPanel();
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);

        loadMoviesAndRevenue();
        movieBox.addActionListener(e -> loadMovieSessions());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadMoviesAndRevenue() {
        model.setRowCount(0);
        int total = 0;
        movieBox.removeAllItems();
        movieBox.addItem("全部");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            Statement stmt = conn.createStatement();
            ResultSet allMovies = stmt.executeQuery("SELECT uid, title FROM Movie");

            while (allMovies.next()) {
                String title = allMovies.getString("title");
                PreparedStatement revStmt = conn.prepareStatement(
                    "SELECT SUM(price) as revenue FROM Booking WHERE movie_uid=? AND status='active'");
                revStmt.setInt(1, allMovies.getInt("uid"));
                ResultSet revRs = revStmt.executeQuery();
                int revenue = 0;
                if (revRs.next()) {
                    revenue = revRs.getInt("revenue");
                }
                movieBox.addItem(title);
                model.addRow(new Object[]{ "電影", title, revenue });
                total += revenue;
            }

            model.addRow(new Object[]{ "總計", "", total });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMovieSessions() {
        String title = (String) movieBox.getSelectedItem();
        if (title == null) return;
        if (title.equals("全部")) {
            loadMoviesAndRevenue();
            return;
        }

        model.setRowCount(0);
        int total = 0;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            PreparedStatement sessionStmt = conn.prepareStatement(
                "SELECT s.start_time FROM Session s JOIN Movie m ON s.movie_uid = m.uid WHERE m.title = ? ORDER BY s.start_time");
            sessionStmt.setString(1, title);
            ResultSet sessionRs = sessionStmt.executeQuery();

            while (sessionRs.next()) {
                String time = sessionRs.getString("start_time");
                PreparedStatement revStmt = conn.prepareStatement(
                    "SELECT SUM(price) AS revenue FROM Booking b JOIN Movie m ON b.movie_uid = m.uid " +
                    "WHERE m.title = ? AND b.start_time = ? AND b.status='active'");
                revStmt.setString(1, title);
                revStmt.setString(2, time);
                ResultSet revRs = revStmt.executeQuery();
                int revenue = 0;
                if (revRs.next()) {
                    revenue = revRs.getInt("revenue");
                }
                model.addRow(new Object[]{ "場次", time, revenue });
                total += revenue;
            }

            model.addRow(new Object[]{ "總計", "", total });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
