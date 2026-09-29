
package com.cinema.ui.employee;

import javax.swing.*;
import java.awt.*;

/**
 * 營運人員主畫面，可選擇執行訂單查詢、電影上架、建立場次等功能。
 */
public class AdminFrame extends JFrame {
    public AdminFrame() {
        setTitle("營運管理後台");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(6, 1));

        // 建立功能按鈕
        JButton viewBookingBtn = new JButton("查詢訂票紀錄");
        JButton manageMoviesBtn = new JButton("電影上/下架");
        JButton createSessionBtn = new JButton("管理電影場次");
        JButton Revenue = new JButton("帳目");
        JButton logoutBtn = new JButton("登出");

        // 綁定功能按鈕
        viewBookingBtn.addActionListener(e -> {
            dispose();
            new AdminBookingViewFrame();
        });
        manageMoviesBtn.addActionListener(e -> {
            dispose();
            new AdminMovieManagerFrame();
        });
        createSessionBtn.addActionListener(e -> {
            dispose();
            new AdminSessionManagerFrame();
        });;
        Revenue.addActionListener(e -> {
            dispose();
            new AdminRevenueFrame();
        });

        // 加入按鈕到畫面
        add(new JLabel("請選擇管理功能：", SwingConstants.CENTER));
        add(viewBookingBtn);
        add(manageMoviesBtn);
        add(createSessionBtn);
        add(Revenue);
        add(logoutBtn);

        // 登出返回登入畫面
        logoutBtn.addActionListener(e -> {
            dispose();
            new AdminLoginFrame();
            
        });
        
        setLocationRelativeTo(null); //畫面固定在中間
        setVisible(true);
    }
}
