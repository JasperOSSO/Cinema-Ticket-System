
package com.cinema.ui.user;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * 會員登入畫面，輸入 email 與密碼登入，
 * 並可切換到註冊畫面。
 */
public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("會員登入");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // === 中間輸入欄位 ===
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50)); // 增加內邊距

        // Email 行
        JPanel emailRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 20));
        JTextField emailField = new JTextField(20);
        emailRow.add(emailLabel);
        emailRow.add(emailField);

        // 密碼 行
        JPanel passwordRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel passwordLabel = new JLabel("密碼:  ");
        passwordLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 20));
        JPasswordField passwordField = new JPasswordField(20);
        passwordRow.add(passwordLabel);
        passwordRow.add(passwordField);

        // 組裝
        inputPanel.add(emailRow);
        inputPanel.add(passwordRow);

        // === 下方操作按鈕列 ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton loginBtn = new JButton("登入");
        JButton registerBtn = new JButton("點此註冊");
        registerBtn.setBorderPainted(false);
        registerBtn.setForeground(Color.RED);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        // === 狀態列 ===
        JLabel statusLabel = new JLabel("", SwingConstants.CENTER);

        // 加入到主畫面
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        // === 登入邏輯 ===
        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Member WHERE email=? AND password=?");
                stmt.setString(1, email);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int uid = rs.getInt("uid");
                    dispose();
                    new UserHomeFrame(email, uid);
                } else {
                    statusLabel.setText("帳號或密碼錯誤");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("登入失敗：" + ex.getMessage());
            }
        });

        // === 註冊切換按鈕 ===
        registerBtn.addActionListener(e -> {
            dispose();
            new RegisterFrame();
        });

        setLocationRelativeTo(null); //畫面固定在中間
        setVisible(true);
    }
}
