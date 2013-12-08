package de.l3s.ubt.wrapper.jena2_5;

public class DerbyRepository extends JDBCRepository {

	private static final String DEFAULT_OPTIONS = "create=true;upgrade=true";

	private static final String TYPE = "Derby";
	private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

	@Override
	protected String getName() {
		return super.getName() + "-derby";
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
		string.append("jdbc:derby:");
		for(String name : names) {
			string.append(name);
			string.append('/');
		}
		string.append(';');
		string.append(DEFAULT_OPTIONS);
		return string.toString();
	}

}
