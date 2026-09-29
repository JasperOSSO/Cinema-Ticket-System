
package com.cinema.ui.user;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * 會員註冊畫面，輸入 Gmail、密碼與生日，建立新帳號。
 */
public class RegisterFrame extends JFrame {
    public RegisterFrame() {
        setTitle("會員註冊");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 中央欄位區塊
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 20, 60));

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        JTextField emailField = new JTextField();

        JLabel passwordLabel = new JLabel("密碼:");
        passwordLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        JPasswordField passwordField = new JPasswordField();

        JLabel birthLabel = new JLabel("生日 (yyyy-mm-dd):");
        birthLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        JTextField birthField = new JTextField();

        formPanel.add(emailLabel); formPanel.add(emailField);
        formPanel.add(passwordLabel); formPanel.add(passwordField);
        formPanel.add(birthLabel); formPanel.add(birthField);
        add(formPanel, BorderLayout.CENTER);

        // 按鈕區塊（右下角）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        JButton registerBtn = new JButton("註冊");
        JButton backBtn = new JButton("返回登入頁面");
        JLabel statusLabel = new JLabel("");

        buttonPanel.add(backBtn);
        buttonPanel.add(registerBtn);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.EAST);
        southPanel.add(statusLabel, BorderLayout.WEST);

        add(southPanel, BorderLayout.SOUTH);

        // 註冊事件
        registerBtn.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String birth = birthField.getText();

            if (!email.endsWith("@gmail.com")) {
                statusLabel.setText("Email 必須為 @gmail.com 結尾");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db");
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Member (email, password, birth) VALUES (?, ?, ?)")) {

                stmt.setString(1, email);
                stmt.setString(2, password);
                stmt.setString(3, birth);
                stmt.executeUpdate();

                statusLabel.setText("註冊成功！");
                dispose();
                new LoginFrame(); // 回登入頁面
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("註冊失敗：" + ex.getMessage());
            }
        });

        // 返回按鈕事件
        backBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
