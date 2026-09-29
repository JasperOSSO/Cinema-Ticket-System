
package com.cinema.ui.employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * AdminSessionManagerFrame：整合建立與管理電影場次，含時間衝突與30分鐘間隔檢查
 */
public class AdminSessionManagerFrame extends JFrame {
	private DefaultTableModel model = new DefaultTableModel(new String[] { "場次ID", "電影", "影廳", "時間" }, 0);
    private JComboBox<String> filterMovieBox = new JComboBox<>();
	private JTable table = new JTable(model);
	private JComboBox<String> movieBox = new JComboBox<>();
	private JComboBox<String> roomBox = new JComboBox<>(new String[] { "1", "2" });
	private JTextField timeField = new JTextField("2025-06-01T18:30");
	private java.util.Map<String, Integer> movieMap = new java.util.HashMap<>();

	public AdminSessionManagerFrame() {
		setTitle("建立與管理電影場次");
		setSize(850, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		// 建立區塊
		JPanel topPanel = new JPanel(new GridLayout(2, 4));
		topPanel.setBorder(BorderFactory.createTitledBorder("建立新場次"));
		topPanel.add(new JLabel("電影："));
		topPanel.add(movieBox);
		topPanel.add(new JLabel("影廳："));
		topPanel.add(roomBox);
		topPanel.add(new JLabel("時間（格式 yyyy-MM-ddTHH:mm）："));
		topPanel.add(timeField);
		JButton createBtn = new JButton("建立");
		topPanel.add(createBtn);
		add(topPanel, BorderLayout.NORTH);

		// 表格顯示
        JPanel tablePanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("篩選電影："));
        filterPanel.add(filterMovieBox);
        tablePanel.add(filterPanel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createTitledBorder("已排定場次"));
		tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		JButton updateBtn = new JButton("修改時間");
		JButton deleteBtn = new JButton("刪除場次");
		JButton backBtn = new JButton("返回首頁");
		bottomPanel.add(updateBtn);
		bottomPanel.add(deleteBtn);
		bottomPanel.add(backBtn);
		add(bottomPanel, BorderLayout.SOUTH);

		loadMovies();
        filterMovieBox.addActionListener(e -> loadSessions((String) filterMovieBox.getSelectedItem()));
		if (filterMovieBox.getItemCount() > 0) loadSessions((String) filterMovieBox.getItemAt(0));

		createBtn.addActionListener(e -> createSession());
		updateBtn.addActionListener(e -> updateSession());
		deleteBtn.addActionListener(e -> deleteSession());
		backBtn.addActionListener(e -> {
			dispose();
			new AdminFrame();
		});

		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void loadMovies() {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db");
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT uid, title FROM Movie")) {
			movieBox.removeAllItems();
        filterMovieBox.removeAllItems();
			movieMap.clear();
			while (rs.next()) {
				String title = rs.getString("title");
				movieBox.addItem(title);
                filterMovieBox.addItem(title);
				movieMap.put(title, rs.getInt("uid"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadSessions() {
		model.setRowCount(0);
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db");
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(
						"SELECT s.uid, m.title, s.room_uid, s.start_time FROM Session s JOIN Movie m ON s.movie_uid = m.uid")) {
			while (rs.next()) {
				model.addRow(new Object[] { rs.getInt("uid"), rs.getString("title"), rs.getInt("room_uid"),
						rs.getString("start_time") });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 建立場次
	private void createSession() {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
			int movieId = movieMap.get((String) movieBox.getSelectedItem());
			int roomId = Integer.parseInt((String) roomBox.getSelectedItem());
			String timeStr = timeField.getText();
			LocalDateTime newStart = LocalDateTime.parse(timeStr);

			PreparedStatement check = conn.prepareStatement(
					"SELECT s.start_time, m.duration, m.title FROM Session s JOIN Movie m ON s.movie_uid = m.uid WHERE s.room_uid = ?");
			check.setInt(1, roomId);
			ResultSet rs = check.executeQuery();

			while (rs.next()) {
				LocalDateTime existingStart = LocalDateTime.parse(rs.getString("start_time"));
				int duration = rs.getInt("duration");
				LocalDateTime existingEnd = existingStart.plusMinutes(duration).plusMinutes(30); // 緩衝 30 分
				LocalDateTime newEnd = newStart.plusMinutes(duration);

				if (!(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd))) {
					JOptionPane.showMessageDialog(this, "❌ 時間衝突：該廳已有場次，需間隔 30 分鐘");
					return;
				}
			}

			PreparedStatement stmt = conn
					.prepareStatement("INSERT INTO Session (movie_uid, room_uid, start_time) VALUES (?, ?, ?)");
			stmt.setInt(1, movieId);
			stmt.setInt(2, roomId);
			stmt.setString(3, timeStr);
			stmt.executeUpdate();
			JOptionPane.showMessageDialog(this, "✅ 場次建立成功！");
			if (filterMovieBox.getItemCount() > 0) loadSessions((String) filterMovieBox.getItemAt(0));
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "建立失敗：" + e.getMessage());
		}
	}

	// 更新場次
	private void updateSession() {
	    int row = table.getSelectedRow();
	    if (row == -1) return;

	    int sessionId = (int) model.getValueAt(row, 0);
	    int roomId = (int) model.getValueAt(row, 2);
	    String oldTime = (String) model.getValueAt(row, 3);
	    String newTime = JOptionPane.showInputDialog(this, "輸入新時間（yyyy-MM-ddTHH:mm）：", oldTime);

	    if (newTime != null && !newTime.isEmpty()) {
	        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
	            LocalDateTime newStart = LocalDateTime.parse(newTime);

	            // 檢查是否與其他場次衝突
	            PreparedStatement check = conn.prepareStatement(
	                "SELECT s.start_time, m.duration FROM Session s " +
	                "JOIN Movie m ON s.movie_uid = m.uid WHERE s.room_uid=? AND s.uid != ?"
	            );
	            check.setInt(1, roomId);
	            check.setInt(2, sessionId);
	            ResultSet rs = check.executeQuery();

	            while (rs.next()) {
	                LocalDateTime existStart = LocalDateTime.parse(rs.getString("start_time"));
	                int duration = rs.getInt("duration");
	                LocalDateTime existEnd = existStart.plusMinutes(duration).plusMinutes(30);
	                LocalDateTime newEnd = newStart.plusMinutes(duration);

	                if (!(newEnd.isBefore(existStart) || newStart.isAfter(existEnd))) {
	                    JOptionPane.showMessageDialog(this, "❌ 時間衝突，請與其他場次至少間隔 30 分鐘");
	                    return;
	                }
	            }

	            // 修改資料
	            PreparedStatement stmt = conn.prepareStatement("UPDATE Session SET start_time=? WHERE uid=?");
	            stmt.setString(1, newTime);
	            stmt.setInt(2, sessionId);
	            stmt.executeUpdate();
	            if (filterMovieBox.getItemCount() > 0) loadSessions((String) filterMovieBox.getItemAt(0));
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

	// 刪除場次
	private void deleteSession() { 
        int row = table.getSelectedRow();
        if (row == -1) return;
        int sessionId = (int) model.getValueAt(row, 0);
        int roomId = (int) model.getValueAt(row, 2);
        String time = (String) model.getValueAt(row, 3);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db")) {
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM Booking WHERE room_uid=? AND start_time=?");
            check.setInt(1, roomId);
            check.setString(2, time);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "❌ 該場次已有訂票紀錄，無法刪除");
                return;
            }

            PreparedStatement stmt = conn.prepareStatement("DELETE FROM Session WHERE uid=?");
            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
            if (filterMovieBox.getItemCount() > 0) loadSessions((String) filterMovieBox.getItemAt(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void loadSessions(String movieFilter) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:cinema.db");
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT s.uid, m.title, s.room_uid, s.start_time FROM Session s JOIN Movie m ON s.movie_uid = m.uid WHERE m.title=?")) {
            stmt.setString(1, movieFilter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("uid"),
                        rs.getString("title"),
                        rs.getInt("room_uid"),
                        rs.getString("start_time")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
