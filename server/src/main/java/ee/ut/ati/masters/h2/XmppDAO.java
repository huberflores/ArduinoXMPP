package ee.ut.ati.masters.h2;

import ee.ut.ati.masters.h2.data.Data;
import ee.ut.ati.masters.h2.data.SensorData;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class XmppDAO {

	public static void insertSensorData(DataSource dataSource, int location, Data data) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = dataSource.getConnection();
			preparedStatement = connection.prepareStatement("insert into data (sensortype_id, location_id, value, measure_time, measured) values(?, ?, ?, ?, ?)");
			preparedStatement.setInt(1, data.getType());
			preparedStatement.setInt(2, location);
			preparedStatement.setDouble(3, data.getValue());
			preparedStatement.setTimestamp(4, data.getMeasureTime());
			preparedStatement.setBoolean(5, data.isMeasured());
			preparedStatement.execute();
			Logger log = Logger.getLogger("InsertDataSource");
			log.debug(String.format("insert into data (sensortype_id, location_id, value, measure_time, measured) values(%d, %d, %f, %s, %s)",
					data.getType(), location, data.getValue(), data.getMeasureTime(), data.isMeasured()));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(connection, preparedStatement);
		}
	}

	public static SensorData getPreviousSensorData(DataSource dataSource, int location) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<Data> dataList = new ArrayList<Data>();
		try {
			connection = dataSource.getConnection();
			int[] sensorTypes = new int[] {Data.TYPE_TEMPERATURE, Data.TYPE_LIGHT, Data.TYPE_HALL};
			for (int type : sensorTypes) {
				preparedStatement = connection.prepareStatement("select * from data where sensortype_id = ? and location_id = ? and measure_time < current_timestamp and measured = true order by measure_time desc limit 1");
				preparedStatement.setInt(1, type);
				preparedStatement.setInt(2, location);
				preparedStatement.execute();
				ResultSet resultSet = preparedStatement.getResultSet();
				if (resultSet.next()) {
					Data data = new Data(type, resultSet.getDouble("value"));
					data.setMeasured(resultSet.getBoolean("measured"));
					data.setMeasureTime(resultSet.getTimestamp("measure_time"));
					dataList.add(data);
				}
			}
			return new SensorData(location, dataList);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(connection, preparedStatement);
		}
		return null;
	}

	public static List<Data> getDifferenceComparisonDataList(DataSource dataSource, int sensorType, int location) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<Data> result = new ArrayList<Data>();
		try {
			connection = dataSource.getConnection();
			preparedStatement = connection.prepareStatement("select * from data where sensortype_id = ? and location_id = ? and measure_time < current_timestamp and measure_time >= dateadd('minute', -4, current_timestamp) and measured = true order by measure_time asc");
			preparedStatement.setInt(1, sensorType);
			preparedStatement.setInt(2, location);
			preparedStatement.execute();

			ResultSet resultSet = preparedStatement.getResultSet();
			while (resultSet.next()) {
				Data data = new Data(resultSet.getInt("sensortype_id"), resultSet.getDouble("value"));
				data.setMeasured(resultSet.getBoolean("measured"));
				data.setMeasureTime(resultSet.getTimestamp("measure_time"));
				result.add(data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(connection, preparedStatement);
		}
		return result;
	}

	public static SensorData processMessage(String message) throws IOException {
		SensorData sensorData = null;
		if (message.contains("SensorData")) {
			ObjectMapper mapper = new ObjectMapper();
			String content = message.substring(message.indexOf("{"));
			sensorData = mapper.readValue(content, SensorData.class);
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			for (Data data : sensorData.getData()) {
				data.setMeasured(true);
				data.setMeasureTime(ts);
			}
		}
		return sensorData;
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
