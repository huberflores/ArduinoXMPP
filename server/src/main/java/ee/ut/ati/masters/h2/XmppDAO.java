package ee.ut.ati.masters.h2;

import ee.ut.ati.masters.h2.data.Data;
import ee.ut.ati.masters.h2.data.SensorData;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

public class XmppDAO {
	
	public static void insertSensorData(DataSource dataSource, int location, int type, double value) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = dataSource.getConnection();
			preparedStatement = connection
					.prepareStatement("insert into data (sensortype_id, location_id, value, measure_time) "
							+ "values(?, ?, ?, current_timestamp)");
			preparedStatement.setInt(1, type);
			preparedStatement.setInt(2, location);
			preparedStatement.setDouble(3, value);
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(connection, preparedStatement);
		}
	}

	public static SensorData processMessage(String message) throws IOException {
		if (message.contains("SensorData")) {
			ObjectMapper mapper = new ObjectMapper();
			String content = message.substring(message.indexOf("{"));
			SensorData sensorData = mapper.readValue(content, SensorData.class);
			for (Data data : sensorData.getData()) {
				XmppDAO.insertSensorData(
						ConnectionFactory.getDataSource(),
						sensorData.getLocation(), data.getType(),
						data.getValue());
			}
			return sensorData;
		}
		return null;
	}

	public static void insertLocation(DataSource dataSource, String location) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = dataSource.getConnection();
			preparedStatement = connection
					.prepareStatement("insert into locations (name) values(?)");
			preparedStatement.setString(1, location);
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(connection, preparedStatement);
		}
	}

	private static void close(Connection connection, PreparedStatement stmt) {
		try {
			if (stmt != null)
				stmt.close();
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
