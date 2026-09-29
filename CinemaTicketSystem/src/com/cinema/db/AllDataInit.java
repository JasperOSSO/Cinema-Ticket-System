
package com.cinema.db;

import java.io.FileReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class AllDataInit {
	public static void main(String[] args) {
		String url = "jdbc:sqlite:cinema.db";

// 建立與 SQLite 資料庫的連線
		try (Connection conn = DriverManager.getConnection(url)) {
			Statement stmt = conn.createStatement();

			// 1. 初始化 Room
			stmt.execute("INSERT OR IGNORE INTO Room (uid, type) VALUES (1, 'small'), (2, 'big')");

			// 2. 匯入 movie_info.json -> Movie
			FileReader movieReader = new FileReader("data/movie_info.json");
			JSONArray movies = new JSONArray(new JSONTokener(movieReader));

			PreparedStatement insertMovie = conn.prepareStatement(
					"INSERT OR IGNORE INTO Movie (uid, title, duration, summary, classification) VALUES (?, ?, ?, ?, ?)");

			for (int i = 0; i < movies.length(); i++) {
				JSONObject m = movies.getJSONObject(i);
				insertMovie.setInt(1, i + 1);
				insertMovie.setString(2, m.getString("title_zh"));
				insertMovie.setInt(3, m.getInt("length"));
				insertMovie.setString(4, m.getString("summary"));
				insertMovie.setString(5, m.getString("classification"));
// 執行更新操作
				insertMovie.executeUpdate();
			}

			// 3. 匯入 small_room.json 與 big_room.json -> Seat
			insertSeatsFromJson(conn, "data/small_room.json", 1);
			insertSeatsFromJson(conn, "data/big_room.json", 2);

			// 4. 建立 Session（每部電影安排 2 場，Room 1 和 Room 2）
			PreparedStatement insertSession = conn.prepareStatement(
					"INSERT OR IGNORE INTO Session (movie_uid, room_uid, start_time) VALUES (?, ?, ?)");

			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
			for (int i = 0; i < movies.length(); i++) {
				int movieId = i + 1;
				String time1 = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).format(fmt);
				String time2 = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0).format(fmt);

				insertSession.setInt(1, movieId);
				insertSession.setInt(2, 1); // small room
				insertSession.setString(3, time1);
// 執行更新操作
				insertSession.executeUpdate();

				insertSession.setInt(1, movieId);
				insertSession.setInt(2, 2); // big room
				insertSession.setString(3, time2);
// 執行更新操作
				insertSession.executeUpdate();
			}

			System.out.println("✅ All data initialized.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

// 建立與 SQLite 資料庫的連線
	private static void insertSeatsFromJson(Connection conn, String filePath, int roomId) throws Exception {
		FileReader seatReader = new FileReader(filePath);
		JSONArray seats = new JSONArray(new JSONTokener(seatReader));

		PreparedStatement insertSeat = conn
				.prepareStatement("INSERT OR IGNORE INTO Seat (room_uid, row, seat_number) VALUES (?, ?, ?)");

		for (int i = 0; i < seats.length(); i++) {
			JSONObject s = seats.getJSONObject(i);
			insertSeat.setInt(1, roomId);
			insertSeat.setString(2, s.getString("row"));
			insertSeat.setInt(3, s.getInt("seatNum"));
// 執行更新操作
			insertSeat.executeUpdate();
		}
	}
}