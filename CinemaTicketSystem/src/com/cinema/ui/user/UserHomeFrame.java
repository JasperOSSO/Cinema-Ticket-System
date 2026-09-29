
package com.cinema.ui.user;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * 會員主頁畫面：顯示會員資訊與「尚未觀看」的未來場次訂單，
 * 並提供功能按鈕（訂票、退票、查電影、歷史訂單、註銷帳號、登出）
 */
public class UserHomeFrame extends JFrame {
    private int uid;
    private String email;

    public UserHomeFrame(String email, int uid) {
        this.uid = uid;
        this.email = email;

        setTitle("會員主頁");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== 上方顯示會員基本資料 =====
        JPanel top = new JPanel(new GridLayout(3, 1));
        JLabel uidLabel = new JLabel("UID: " + uid);
        JLabel emailLabel = new JLabel("Email: " + email);
        JLabel birthLabel = new JLabel("生日: ");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT birth FROM Member WHERE uid=?");
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                birthLabel.setText("生日: " + rs.getString("birth"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        top.add(uidLabel);
        top.add(emailLabel);
        top.add(birthLabel);
        add(top, BorderLayout.NORTH);

        // ===== 中間顯示尚未觀看的訂票紀錄（未來場次） =====
        JTable orderTable = new JTable();
        DefaultTableModel model = new DefaultTableModel(new String[]{"訂單ID", "電影", "時間", "票種", "座位", "金額"}, 0);
        orderTable.setModel(model);
        add(new JScrollPane(orderTable), BorderLayout.CENTER);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
        	PreparedStatement stmt = conn.prepareStatement(
        		    "SELECT b.booking_id, m.title, b.start_time, b.row, b.seat_number, b.ticket_type, b.price " +
        		    "FROM Booking b JOIN Movie m ON b.movie_uid = m.uid " +
        		    "WHERE b.member_uid=? AND b.status='active' ORDER BY b.start_time ASC"
        		);
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            LocalDateTime now = LocalDateTime.now();
            while (rs.next()) {
                String time = rs.getString("start_time");
                if (LocalDateTime.parse(time).isAfter(now)) {
                	int bookingId = rs.getInt("booking_id");
                    String seat = rs.getString("row") + rs.getInt("seat_number");
                    String ticket_type = rs.getString("ticket_type");
                    String price = rs.getString("price");
                    model.addRow(new Object[]{bookingId, rs.getString("title"), time, ticket_type, seat, price});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ===== 下方操作按鈕區 =====
        JPanel buttonPanel = new JPanel();
        JButton bookBtn = new JButton("我要訂票");
        JButton refundBtn = new JButton("我要退票");
        JButton movieListBtn = new JButton("上映中電影");
        JButton historyBtn = new JButton("歷史訂單");
        JButton deleteBtn = new JButton("註銷帳號");
        JButton logoutBtn = new JButton("登出");

        // === 各功能按鈕事件 ===
        bookBtn.addActionListener(e -> {
            dispose();
            new BookingFrame(email,uid).setVisible(true);
        });

        refundBtn.addActionListener(e -> {
            dispose();
            new BookingHistoryFrame(email);
        });

        movieListBtn.addActionListener(e -> {
            new MovieListFrame();
        });

        historyBtn.addActionListener(e -> {
            dispose();
            new UserHistoryFrame(email);
        });

        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "確定要註銷帳號？此動作無法復原", "確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
                    PreparedStatement deleteBooking = conn.prepareStatement("DELETE FROM Booking WHERE member_uid=?");
                    deleteBooking.setInt(1, uid);
                    deleteBooking.executeUpdate();

                    PreparedStatement deleteUser = conn.prepareStatement("DELETE FROM Member WHERE uid=?");
                    deleteUser.setInt(1, uid);
                    deleteUser.executeUpdate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "帳號已註銷");
                dispose();
                new LoginFrame();
            }
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        // 加入所有按鈕
        buttonPanel.add(bookBtn);
        buttonPanel.add(refundBtn);
        buttonPanel.add(movieListBtn);
        buttonPanel.add(historyBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(logoutBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // 視窗置中
        setVisible(true);
    }
}
