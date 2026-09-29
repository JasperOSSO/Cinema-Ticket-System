
package com.cinema.ui.user;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileReader;
import java.sql.*;
import java.util.*;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class BookingFrame extends JFrame {
    private JComboBox<String> movieBox = new JComboBox<>();
    private JComboBox<String> timeBox = new JComboBox<>();
    private JComboBox<String> roomBox = new JComboBox<>();
    private JComboBox<Integer> fullBox = new JComboBox<>(new Integer[]{0,1,2,3,4,5,6});
    private JComboBox<Integer> studentBox = new JComboBox<>(new Integer[]{0,1,2,3,4,5,6});
    private JComboBox<Integer> seniorBox = new JComboBox<>(new Integer[]{0,1,2,3,4,5,6});
    private JPanel seatPanel = new JPanel();
    private JButton confirmButton = new JButton("確認訂票");
    private JButton backButton = new JButton("回主頁");

    private List<String[]> availableSessions = new ArrayList<>();
    private Map<String, Integer> movieMap = new HashMap<>();
    private Map<String, Integer> roomMap = new HashMap<>();
    private Set<String> selectedSeats = new HashSet<>();
    private String currentRoomType = "small";
    private String userEmail;
    private int userUid;

    public BookingFrame(String email, int uid) {
        this.userEmail = email;
        this.userUid = uid;
        initUI();
    }

    private void initUI() {
        setTitle("訂票選位");
        setSize(800, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(3, 3));
        top.add(new JLabel("電影:")); top.add(movieBox);
        top.add(new JLabel("時間:")); top.add(timeBox);
        top.add(new JLabel("影廳:")); top.add(roomBox);
        top.add(new JLabel("全票(320元):")); top.add(fullBox);
        top.add(new JLabel("學生票(270元):")); top.add(studentBox);
        top.add(new JLabel("敬老票(250元):")); top.add(seniorBox);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(seatPanel), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.add(confirmButton);
        bottom.add(backButton);
        add(bottom, BorderLayout.SOUTH);

        loadMovies();
        movieBox.addActionListener(e -> loadTimes());
        timeBox.addActionListener(e -> loadRoom());
        fullBox.addActionListener(this::renderSeats);
        studentBox.addActionListener(this::renderSeats);
        seniorBox.addActionListener(this::renderSeats);
        confirmButton.addActionListener(e -> {
        	if (selectedSeats.size() == 0) {
                JOptionPane.showMessageDialog(this, "⚠️ 請選擇票及位子再逕行訂購");
                return;
            }
        	
            if (selectedSeats.size() != getTotalTickets()) {
                JOptionPane.showMessageDialog(this, "⚠️ 請選擇正確數量的座位（共 " + getTotalTickets() + " 張）");
                return;
            }

            if (checkAge()) {
                showCheckoutSummary(); // 符合才跳結帳
            }
        });
        backButton.addActionListener(e -> {
            new UserHomeFrame(userEmail, userUid).setVisible(true);
            dispose();
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private boolean checkAge() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            PreparedStatement userStmt = conn.prepareStatement("SELECT birth FROM Member WHERE uid=?");
            userStmt.setInt(1, userUid);
            ResultSet userRs = userStmt.executeQuery();
            if (!userRs.next()) {
                JOptionPane.showMessageDialog(this, "查無會員資訊");
                return false;
            }

            String birth = userRs.getString("birth");
            LocalDate birthday = LocalDate.parse(birth);
            String time = (String) timeBox.getSelectedItem();
            LocalDate showDate = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
            int age = Period.between(birthday, showDate).getYears();

            int movieId = movieMap.get((String) movieBox.getSelectedItem());
            PreparedStatement classStmt = conn.prepareStatement("SELECT classification FROM Movie WHERE uid=?");
            classStmt.setInt(1, movieId);
            ResultSet crs = classStmt.executeQuery();
            if (!crs.next()) {
                JOptionPane.showMessageDialog(this, "⚠️ 查無電影資訊");
                return false;
            }

            int ageLimit = Integer.parseInt(crs.getString("classification").replace("+", ""));
            if (age < ageLimit) {
                JOptionPane.showMessageDialog(this, "⚠️ 您的年齡不符合電影分級，無法訂票");
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "⚠️ 查詢年齡與分級時發生錯誤：" + ex.getMessage());
            return false;
        }

        return true;
    }


    private int getTotalTickets() {
        return (int)fullBox.getSelectedItem() + (int)studentBox.getSelectedItem() + (int)seniorBox.getSelectedItem();
    }

    private void loadMovies() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT uid, title FROM Movie");
            while (rs.next()) {
                String title = rs.getString("title");
                movieBox.addItem(title);
                movieMap.put(title, rs.getInt("uid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTimes() {
        timeBox.removeAllItems();
        availableSessions.clear();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT s.movie_uid, s.room_uid, s.start_time, r.type FROM Session s JOIN Room r ON s.room_uid = r.uid WHERE movie_uid=?");
            stmt.setInt(1, movieMap.get((String) movieBox.getSelectedItem()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String time = rs.getString("start_time");
                String room = "Room " + rs.getInt("room_uid");
                String type = rs.getString("type");
                availableSessions.add(new String[]{time, room, type});
                timeBox.addItem(time);
                roomMap.put(room, rs.getInt("room_uid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void loadRoom() {
        roomBox.removeAllItems();
        String selectedTime = (String) timeBox.getSelectedItem();
        for (String[] s : availableSessions) {
            if (s[0].equals(selectedTime)) {
                roomBox.addItem(s[1]);
                currentRoomType = s[2];
            }
        }
    }

    private void renderSeats(ActionEvent e) {
        seatPanel.removeAll();
        selectedSeats.clear();
        int total = getTotalTickets();
        if (total == 0) {
            JOptionPane.showMessageDialog(this, "請至少選擇一張票！");
            return;
        }

        try (FileReader reader = new FileReader("data/" + currentRoomType + "_room.json")) {
            JSONArray array = new JSONArray(new JSONTokener(reader));
            seatPanel.setLayout(new BoxLayout(seatPanel, BoxLayout.Y_AXIS));
            Map<String, JPanel> rowPanels = new LinkedHashMap<>();
            JPanel screenRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            screenRow.add(new JLabel("")); // 對齊 row 標籤
            JButton screenBtn = new JButton("銀幕");
            screenBtn.setEnabled(false);
            screenBtn.setBackground(Color.DARK_GRAY);

            // 判斷廳別設定螢幕寬度
            if (currentRoomType.equals("small")) {
                screenBtn.setPreferredSize(new Dimension(1000, 30));
            } else {
                screenBtn.setPreferredSize(new Dimension(2500, 30)); // 大影廳用 800，可自行調整
            }

            screenRow.add(screenBtn);
            seatPanel.add(screenRow);

            for (int i = 0; i < array.length(); i++) {
                JSONObject seat = array.getJSONObject(i);
                String row = seat.getString("row");
                int num = seat.getInt("seatNum");
                String type = seat.optString("type", "seat");
                String seatId = row + num;

                rowPanels.putIfAbsent(row, new JPanel(new FlowLayout(FlowLayout.LEFT)));
                JPanel rowPanel = rowPanels.get(row);

                if (rowPanel.getComponentCount() == 0) {
                    JLabel rowLabel = new JLabel(row + " ");
                    rowLabel.setPreferredSize(new Dimension(48, 32));
                    rowPanel.add(rowLabel);
                }

                if (type.equals("space")) {
                    rowPanel.add(Box.createRigidArea(new Dimension(48, 32)));
                    continue;
                }

                JButton btn = new JButton(String.valueOf(num));
                btn.setPreferredSize(new Dimension(48, 32));
                btn.addActionListener(ae -> {
                    if (selectedSeats.contains(seatId)) {
                        selectedSeats.remove(seatId);
                        btn.setBackground(null);
                    } else {
                        if (selectedSeats.size() >= total) {
                            JOptionPane.showMessageDialog(this, "已達選票上限！");
                            return;
                        }
                        selectedSeats.add(seatId);
                        btn.setBackground(Color.GREEN);
                    }
                });
                rowPanel.add(btn);
            }
            
            for (JPanel row : rowPanels.values()) {
                seatPanel.add(row);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private void confirmBooking(ActionEvent e) {

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            int movieId = movieMap.get((String) movieBox.getSelectedItem());
            int roomId = roomMap.get((String) roomBox.getSelectedItem());
            String time = (String) timeBox.getSelectedItem();
            List<String> seats = new ArrayList<>(selectedSeats);
            int i = 0;

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Booking (member_uid, movie_uid, room_uid, start_time, row, seat_number, ticket_type, price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'active')");
            for (int j = 0; j < (int)fullBox.getSelectedItem(); j++, i++) {
                insertOne(stmt, movieId, roomId, time, seats.get(i), "全票", 320);
            }
            for (int j = 0; j < (int)studentBox.getSelectedItem(); j++, i++) {
                insertOne(stmt, movieId, roomId, time, seats.get(i), "學生票", 270);
            }
            for (int j = 0; j < (int)seniorBox.getSelectedItem(); j++, i++) {
                insertOne(stmt, movieId, roomId, time, seats.get(i), "敬老票", 250);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "訂票失敗：" + ex.getMessage());
        }
    }

    private void insertOne(PreparedStatement stmt, int movieId, int roomId, String time, String seat, String type, int price) throws SQLException {
        stmt.setInt(1, userUid);
        stmt.setInt(2, movieId);
        stmt.setInt(3, roomId);
        stmt.setString(4, time);
        stmt.setString(5, seat.substring(0,1));
        stmt.setInt(6, Integer.parseInt(seat.substring(1)));
        stmt.setString(7, type);
        stmt.setInt(8, price);
        stmt.executeUpdate();
    }

    

private void showCheckoutSummary() {
    JDialog dialog = new JDialog(this, "結帳", true);
    dialog.setSize(500, 400);
    dialog.setLayout(new BorderLayout());

    JTextArea area = new JTextArea();
    area.setEditable(false);
    area.setFont(new Font("Monospaced", Font.PLAIN, 14));

    List<String> seats = new ArrayList<>(selectedSeats);
    Collections.sort(seats);
    int full = (int) fullBox.getSelectedItem();
    int student = (int) studentBox.getSelectedItem();
    int senior = (int) seniorBox.getSelectedItem();
    int total = full + student + senior;
    int sum = 0;
    int i = 0;

    if (full > 0) {
        for (int j = 0; j < full; j++, i++) {
            String seat = seats.get(i);
            area.append("全票 1 張 x 320元  座位 " + seat + "\n");
            sum += 320;
        }
    }
    if (student > 0) {
        for (int j = 0; j < student; j++, i++) {
            String seat = seats.get(i);
            area.append("學生票 1 張 x 270元  座位 " + seat + "\n");
            sum += 270;
        }
    }
    if (senior > 0) {
        for (int j = 0; j < senior; j++, i++) {
            String seat = seats.get(i);
            area.append("敬老票 1 張 x 250元  座位 " + seat + "\n");
            sum += 250;
        }
    }

    area.append("-----------------------------");
    area.append("總金額： " + sum + " 元");

    dialog.add(new JScrollPane(area), BorderLayout.CENTER);

    JButton ok = new JButton("確認結帳");
    ok.addActionListener(e -> {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            int movieId = movieMap.get((String) movieBox.getSelectedItem());
            int roomId = roomMap.get((String) roomBox.getSelectedItem());
            String time = (String) timeBox.getSelectedItem();
            List<String> seatList = new ArrayList<>(selectedSeats);
            Collections.sort(seatList);
            int idx = 0;

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Booking (member_uid, movie_uid, room_uid, start_time, row, seat_number, ticket_type, price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'active')");

            for (int j = 0; j < full; j++, idx++) {
                insertOne(stmt, movieId, roomId, time, seatList.get(idx), "全票", 320);
            }
            for (int j = 0; j < student; j++, idx++) {
                insertOne(stmt, movieId, roomId, time, seatList.get(idx), "學生票", 270);
            }
            for (int j = 0; j < senior; j++, idx++) {
                insertOne(stmt, movieId, roomId, time, seatList.get(idx), "敬老票", 250);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "寫入資料庫失敗：" + ex.getMessage());
        }

        JOptionPane.showMessageDialog(this, "結帳完成！");
        dialog.dispose();
        new UserHomeFrame(userEmail, userUid).setVisible(true);
        dispose();
    });

    JPanel bottom = new JPanel();
    bottom.add(ok);
    dialog.add(bottom, BorderLayout.SOUTH);

    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}
}
