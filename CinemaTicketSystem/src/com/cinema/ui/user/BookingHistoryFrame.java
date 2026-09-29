
package com.cinema.ui.user;

import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 訂票紀錄與退票畫面（使用 status 取消票，不刪除資料）
 */
public class BookingHistoryFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private String email;

    public BookingHistoryFrame(String email) {
        this.email = email;
        setTitle("訂票紀錄與退票");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"訂單ID", "電影", "時間", "票種", "座位", "金額", "操作"}, 0);
        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        // 表格按鈕設定
        table.getColumn("操作").setCellRenderer(new ButtonRenderer());
        table.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton backBtn = new JButton("回主頁");
        backBtn.addActionListener(e -> {
            dispose();
            new UserHomeFrame(email, getUidByEmail(email));
        });
        add(backBtn, BorderLayout.SOUTH);

        loadBookings();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private int getUidByEmail(String email) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT uid FROM Member WHERE email=?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("uid");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    private void loadBookings() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            int uid = getUidByEmail(email);
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT b.booking_id, m.title, b.start_time, b.row, b.seat_number, b.ticket_type, b.price " +
                "FROM Booking b JOIN Movie m ON b.movie_uid = m.uid " +
                "WHERE b.member_uid=? AND b.status = 'active'");
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                String title = rs.getString("title");
                String start = rs.getString("start_time");
                String seat = rs.getString("row") + rs.getInt("seat_number");
                String ticket_type = rs.getString("ticket_type");
                String price = rs.getString("price");
                model.addRow(new Object[]{bookingId, title, start, ticket_type, seat, price, "退票"});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "讀取訂票紀錄失敗：" + e.getMessage());
        }
    }

    public void tryCancel(int bookingId, String startTimeStr) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            if (LocalDateTime.now().isAfter(startTime.minusMinutes(30))) {
                JOptionPane.showMessageDialog(this, "已超過退票時限（需在開演前30分鐘）");
                return;
            }

            // 使用 UPDATE 將 status 改為 cancelled
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE Booking SET status='cancelled' WHERE booking_id=?");
                stmt.setInt(1, bookingId);
                stmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "退票成功！");
            dispose();
            new BookingHistoryFrame(email);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "退票失敗：" + e.getMessage());
        }
    }

    // 表格按鈕渲染器
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "退票" : value.toString());
            return this;
        }
    }

    // 表格按鈕編輯器（按下觸發退票）
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int bookingId;
        private String startTime;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("退票");
            button.addActionListener(e -> {
                tryCancel(bookingId, startTime);
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
            bookingId = (int) table.getValueAt(row, 0);      // 訂單ID
            startTime = (String) table.getValueAt(row, 2);   // 時間
            return button;
        }
    }
}
