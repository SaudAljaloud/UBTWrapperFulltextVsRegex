package de.l3s.ubt.wrapper.jena2_11Regex;


public abstract class PersistentRepository extends JenaRepository {

	@Override
	protected String getName() {
		return "persistent";
	}

}
