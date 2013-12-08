package de.l3s.ubt.wrapper.jena2_5;

public class HSQLDBRepository extends JDBCRepository {

	private static final String TYPE = "HSQL";
	private static final String DRIVER = "org.hsqldb.jdbcDriver";

	@Override
	protected String getName() {
		return super.getName() + "-hsqldb";
	}

	@Override
	public String type() {
		return TYPE;
	}
	
	@Override
	public String driver() {
		return DRIVER;
	}

	@Override
	protected String jdbcURL(String ...names) {
		StringBuilder string = new StringBuilder();
		string.append("jdbc:hsqldb:file:");
		for(String name : names) {
			string.append(name);
			string.append('/');
		}
		string.append("hsqldb");
		return string.toString();
	}

}
