package ee.ut.ati.masters.h2;

import javax.sql.DataSource;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;

public class ConnectionFactory {

	private static DataSource ds;

	static {
		DriverAdapterCPDS cpds = new DriverAdapterCPDS();
		try {
			cpds.setDriver("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		cpds.setUrl("jdbc:h2:DB/arduinoDB;AUTO_SERVER=TRUE");
		cpds.setUser("sa");
		cpds.setPassword("");

		SharedPoolDataSource tds = new SharedPoolDataSource();
		tds.setConnectionPoolDataSource(cpds);
		tds.setMaxActive(10);
		tds.setMaxWait(50);

		ds = tds;
	}

	public static DataSource getDataSource() {
		return ds;
	}

}
