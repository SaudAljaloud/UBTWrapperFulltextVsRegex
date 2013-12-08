package de.l3s.ubt.wrapper.jena2_5;

public class MySQLRepository extends JDBCRepository {

	private static final String TYPE = "MySQL";
	private static final String DRIVER = "com.mysql.jdbc.Driver";

	@Override
	protected String getName() {
		return super.getName() + "-mysql";
	}

	@Override
	protected String type() {
		return TYPE;
	}
	
	@Override
	protected String driver() {
		return DRIVER;
	}

	@Override
	protected String jdbcURL(String ...names) {
		StringBuilder string = new StringBuilder();
		string.append("jdbc:mysql://localhost/jena2ubt_");
		string.append(names[names.length-1].replaceAll("-", "_"));
		return string.toString();
	}

}
