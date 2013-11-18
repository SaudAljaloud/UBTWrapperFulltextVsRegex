package saud.ubt.wrapper.Owlim;

import java.io.File;
import org.openrdf.repository.sail.SailRepository;

import com.ontotext.trree.OwlimSchemaRepository;

public class OwlimAbstractRepository2 extends OwlimRepository2 {

	private final static File ROOT_DATA_DIR = new File("Owlim-repositories");

	public OwlimAbstractRepository2() {
		super.provideFulltext();
	}

	@Override
	protected SailRepository createRepository() {
		SailRepository repo = null;
		try {
			OwlimSchemaRepository schema = new OwlimSchemaRepository();
			schema.setDataDir(new File("./" + ROOT_DATA_DIR));
			schema.setParameter("storage-folder", "./");
			schema.setParameter("repository-type", "file-repository");
			schema.setParameter("ruleset", "rdfs");
			repo = new SailRepository(schema);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return repo;

	}

}
