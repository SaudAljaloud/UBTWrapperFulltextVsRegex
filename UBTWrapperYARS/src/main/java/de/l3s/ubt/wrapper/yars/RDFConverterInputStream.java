package de.l3s.ubt.wrapper.yars;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This InputStream takes an RDF file, converts it from its origin format
 * into the desired RDF format and provides that as an InputStream.
 * 
 * @author Enrico Minack
 *
 */
public class RDFConverterInputStream extends InputStream {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final File in;
	private final PipedOutputStream pout;
	private final PipedInputStream pin;
	private final RDFParser parser;

	public RDFConverterInputStream(File in, RDFFormat inFormat, RDFFormat outFormat) throws IOException {
		// memorize the input file
		this.in = in;
		
		// create a pipe from output stream to input stream
		pout = new PipedOutputStream();
		pin = new PipedInputStream(pout);

		// let the writer write to the piped output stream
		RDFHandler handler = RDFWriterRegistry.getInstance().get(outFormat).getWriter(pout);
		
		// let the RDF parser write to the writer
		this.parser = RDFParserRegistry.getInstance().get(inFormat).getParser();
		this.parser.setRDFHandler(handler);
		
		// start a thread for the parsing
		new Thread(new Runnable() {
			public void run() {
				try {
					RDFConverterInputStream.this.parser.parse(
							new FileReader(RDFConverterInputStream.this.in),
							"unkown:namespace");
				} catch (Exception e) {
					log.error("exception while parsing input rdf stream", e);
				}
			};
		}).start();
	}

	@Override
	public int read() throws IOException {
		return pin.read();
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return pin.read(b, off, len);
	}
	
	@Override
	public long skip(long n) throws IOException {
		return pin.skip(n);
	}

}
