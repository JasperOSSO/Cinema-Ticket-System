package com.cinema.ui.employee;

import javax.swing.*;
import com.cinema.ui.user.LoginFrame;
import java.awt.*;

/**
 * 營運人員登入畫面，密碼固定為 Jordan，登入成功後可進入管理畫面。
 */
public class AdminLoginFrame extends JFrame {
    public AdminLoginFrame() {
        setTitle("營運人員登入");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 主畫面
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 密碼輸入行（Label + 輸入框）
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel passLabel = new JLabel("請輸入營運人員密碼：");
        passLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 40));

        inputPanel.add(passLabel);
        inputPanel.add(passwordField);

        // 登入按鈕
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginBtn = new JButton("登入");
        loginBtn.setPreferredSize(new Dimension(200, 50));
        buttonPanel.add(loginBtn);

        // 狀態提示區
        JLabel status = new JLabel("");
        status.setHorizontalAlignment(SwingConstants.CENTER);

        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(inputPanel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(buttonPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(status);

        add(centerPanel, BorderLayout.CENTER);

        // 登入按鈕
        loginBtn.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            if ("passwords".equals(password)) {
                dispose();
                new AdminFrame(); // 登入成功 → 進入營運畫面
            } else {
                status.setText("密碼錯誤");
            }
        });

        setLocationRelativeTo(null); //畫面固定在中間
        setVisible(true);
    }
}
