
package com.cinema.ui;

import javax.swing.*;

import com.cinema.ui.employee.AdminLoginFrame;
import com.cinema.ui.user.LoginFrame;

import java.awt.*;

/**
 * 系統進入點畫面，提供使用者選擇「會員登入」或「營運人員登入」。
 */
public class MainEntryFrame extends JFrame {
    public MainEntryFrame() {
        setTitle("電影院售票系統");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));

        // 標題
        JLabel title = new JLabel("歡迎使用電影院售票系統", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
        add(title);

        // 會員登入按鈕
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton userBtn = new JButton("會員登入");
        userBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        userBtn.setPreferredSize(new Dimension(200, 50));
        buttonPanel.add(userBtn);
        userBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        add(buttonPanel);

        // 營運人員登入按鈕
        JPanel buttonPanel2 = new JPanel(new FlowLayout());
        JButton adminBtn = new JButton("營運人員登入");
        adminBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        adminBtn.setPreferredSize(new Dimension(200, 50));
        buttonPanel.add(adminBtn);
        adminBtn.addActionListener(e -> {
            dispose();
            new AdminLoginFrame();
        });

        add(buttonPanel2);

        setLocationRelativeTo(null); //畫面固定在中間
        setVisible(true);
    }

    public static void main(String[] args) {
    	try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        new MainEntryFrame();
    }
}
