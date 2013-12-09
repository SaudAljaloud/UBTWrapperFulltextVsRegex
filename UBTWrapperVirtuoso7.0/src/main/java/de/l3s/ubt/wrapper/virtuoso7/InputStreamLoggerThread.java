package de.l3s.ubt.wrapper.virtuoso7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

public class InputStreamLoggerThread extends Thread {

	private final InputStream in;
	private final Logger log;
	private final boolean error;
	
	public InputStreamLoggerThread(InputStream in, Logger log) {
		this(in, log, false);
	}

	public InputStreamLoggerThread(InputStream in, Logger log, boolean error) {
		super("std" + (error?"err":"out") + "-input-stream-logger");
		
		this.in = in;
		this.log = log;
		this.error = error;
	}
	
	@Override
	public void run() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(this.in));
			String line;
			while((line = reader.readLine()) != null) {
				if(this.error)
					log.error(line);
				else
					log.debug(line);
			}
		} catch (IOException e) {
			log.debug("InputStreamLoggerThread (std" + (this.error?"err":"out") + ") could not read from given inputstream", e);
			return;
		}
		
		try {
			reader.close();
		} catch (IOException e) {
			log.debug("InputStreamLoggerThread (std" + (this.error?"err":"out") + ") could not close given inputstream", e);
		}

		log.debug("InputStreamLoggerThread (std" + (this.error?"err":"out") + ") shutting down");
	}

}
