
package com.cinema.ui.employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 營運人員管理電影（上/下架）用的畫面。
 */
public class AdminMovieManagerFrame extends JFrame {
    private DefaultTableModel model;

    public AdminMovieManagerFrame() {
        setTitle("電影上/下架管理");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 表格顯示所有電影
        model = new DefaultTableModel(new String[]{"ID", "名稱", "級別", "片長"}, 0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 下方新增欄位
        JPanel inputPanel = new JPanel(new GridLayout(2, 4));
        JTextField titleField = new JTextField();
        JTextField levelField = new JTextField();
        JTextField lengthField = new JTextField();
        JTextField summaryField = new JTextField();
        JButton addBtn = new JButton("新增電影");
        JButton deleteBtn = new JButton("下架選取電影");
        JButton backBtn = new JButton("回管理首頁");

        inputPanel.add(new JLabel("片名"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("分級"));
        inputPanel.add(levelField);
        inputPanel.add(new JLabel("片長"));
        inputPanel.add(lengthField);
        inputPanel.add(new JLabel("簡介"));
        inputPanel.add(summaryField);

        JPanel bottom = new JPanel();
        bottom.add(addBtn);
        bottom.add(deleteBtn);
        bottom.add(backBtn);

        add(inputPanel, BorderLayout.NORTH);
        add(bottom, BorderLayout.SOUTH);

        // 載入資料
        loadMovies();

        // 新增電影
        addBtn.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO Movie (title, classification, duration, summary) VALUES (?, ?, ?, ?)");
                stmt.setString(1, titleField.getText());
                stmt.setString(2, levelField.getText());
                stmt.setInt(3, Integer.parseInt(lengthField.getText()));
                stmt.setString(4, summaryField.getText());
                stmt.executeUpdate();
                loadMovies();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 刪除選取的電影
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) model.getValueAt(row, 0);
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM Movie WHERE uid=?");
                    stmt.setInt(1, id);

                 // 檢查是否仍有場次或訂票紀錄
                 PreparedStatement chk1 = conn.prepareStatement("SELECT COUNT(*) FROM Session WHERE movie_uid=?");
                 chk1.setInt(1, id);
                 ResultSet r1 = chk1.executeQuery();
                 int sessionCount = r1.next() ? r1.getInt(1) : 0;

                 PreparedStatement chk2 = conn.prepareStatement("SELECT COUNT(*) FROM Booking WHERE movie_uid=?");
                 chk2.setInt(1, id);
                 ResultSet r2 = chk2.executeQuery();
                 int bookingCount = r2.next() ? r2.getInt(1) : 0;

                 if (sessionCount > 0 || bookingCount > 0) {
                     JOptionPane.showMessageDialog(this, "無法下架：該電影仍有場次或訂票紀錄");
                     return;
                 }

                 stmt.executeUpdate(); // 真正下架

                    loadMovies();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        backBtn.addActionListener(e -> {
            dispose();
            new AdminFrame();
        });
        
        setLocationRelativeTo(null); //畫面固定在中間
        setVisible(true);
    }

    private void loadMovies() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT uid, title, classification, duration FROM Movie");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("uid"), rs.getString("title"), rs.getString("classification"), rs.getInt("duration")});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
