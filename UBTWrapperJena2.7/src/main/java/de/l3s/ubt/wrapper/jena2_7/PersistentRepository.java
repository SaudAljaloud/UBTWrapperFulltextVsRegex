package de.l3s.ubt.wrapper.jena2_7;


public abstract class PersistentRepository extends JenaRepository {

	@Override
	protected String getName() {
		return "persistent";
	}

}
