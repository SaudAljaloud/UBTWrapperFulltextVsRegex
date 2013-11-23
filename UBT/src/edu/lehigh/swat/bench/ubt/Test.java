/**
 * by Yuanbo Guo
 * Semantic Web and Agent Technology Lab, CSE Department, Lehigh University, USA
 * Copyright (C) 2004
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package edu.lehigh.swat.bench.ubt;

import java.util.*;
import java.io.*;
import java.text.*;

import edu.lehigh.swat.bench.ubt.api.*;

public class Test extends RepositoryCreator {
	/** execution times of each query */
	public static final int QUERY_TIME = 6;
	/** name of file to hold the query test result */
	/** command to flush system fs caches */
	private static final String FLUSH_FS_CACHE_COMMAND = "sudo purge";
	/**
	 * maximum allowed duration for a query, queries exceeding this limit are
	 * not executed anymore
	 */
	private static final Integer MAX_DURATION = 100000;

	/** list of target systems */
	@SuppressWarnings("rawtypes")
	private Vector kbList_; // KbSpec
	/** list of test queries */
	@SuppressWarnings("rawtypes")
	private Vector queryList_; // QuerySpec
	/** list of test query ids that shall not be executed anymore */
	private List<Integer> prunedQueries_ = new ArrayList<Integer>();
	/** result holder of query test */
	private QueryTestResult[][] queryTestResults_;
	/**
	 * Output stream for query test results. The file is created simply to ease
	 * the manipulation of the results lateron, e.g., copying it to a Excel
	 * sheet.
	 */
	private PrintStream queryTestResultFile_;
	/** flag indicating whether the target system is memory-based or not */
	private boolean isMemory_ = false;
	/** the current repository object */
	private Repository repository_ = null;

	/**
	 * main method
	 */
	public static void main(String[] args) {
		String arg = "";

		try {
			if (args.length < 1)
				throw new Exception();
			arg = args[0];
			if (arg.equals("query")) {
				if (args.length < 3)
					throw new Exception();
			} else if (arg.equals("load")) {
				if (args.length < 2)
					throw new Exception();
			} else if (arg.equals("memory")) {
				if (args.length < 3)
					throw new Exception();
			} else
				throw new Exception();
		} catch (Exception e) {
			System.err.println("Usage: Test load <kb config file>");
			System.err
					.println("    or Test query <kb config file> <query config file>");
			System.err
					.println("    or Test memory <kb config file> <query config file>");
			System.exit(-1);
		}

		Test test = new Test();
		if (arg.equals("query")) {
			test.testQuery(args[1], args[2]);
		} else if (arg.equals("load")) {
			test.testLoad(args[1]);
		} else if (arg.equals("memory")) {
			test.testMemory(args[1], args[2]);
		}

		System.exit(0);
	}

	/**
	 * constructor.
	 */
	public Test() {
	}

	/**
	 * Starts the loading test defined in the specified config file.
	 * 
	 * @param kbConfigFile
	 *            The knowledge base config file describing the target systems
	 *            and test data.
	 */
	public void testLoad(String kbConfigFile) {
		createKbList(kbConfigFile);
		doTestLoading();
	}

	/**
	 * Starts the query test defined in the specified config files.
	 * 
	 * @param kbConfigFile
	 *            The knowledge base config file describing the target systems.
	 * @param queryConfigFile
	 *            The query config file describing the test queries.
	 */
	public void testQuery(String kbConfigFile, String queryConfigFile) {
		Date date = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd.mm.yyyy.hh.mm.ss");
		String dataFormate = ft.format(date).toString();
		String QUERY_TEST_RESULT_FILE = "Result" + dataFormate + ".txt";
		try {
			queryTestResultFile_ = new PrintStream(new FileOutputStream(
					QUERY_TEST_RESULT_FILE.toString()));

			createKbList(kbConfigFile);
			createQueryList(queryConfigFile);
			doTestQuery();
			queryTestResultFile_.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Starts the test on a memory-based system. Query test is conducted
	 * immediately after loading. NOTE: The current implementation is only
	 * workable in the case where there is just one single system in the test,
	 * i.e., only one system is defined in the config file.
	 * 
	 * @param kbConfigFile
	 *            The knowledge base config file describing the target systems.
	 * @param queryFile
	 *            The query config file describing the test queries.
	 */
	public void testMemory(String kbConfigFile, String queryFile) {
		isMemory_ = true;
		testLoad(kbConfigFile);
		testQuery(kbConfigFile, queryFile);
	}

	/**
	 * Creates the target system list from the config file.
	 * 
	 * @param kbConfigFile
	 *            The knowledge base config file.
	 */
	private void createKbList(String kbConfigFile) {
		try {
			kbList_ = new KbConfigParser()
					.createKbList(kbConfigFile, isMemory_);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Creates the query list from the config file
	 * 
	 * @param queryConfigFile
	 *            The query config file.
	 */
	private void createQueryList(String queryConfigFile) {
		try {
			queryList_ = new QueryConfigParser()
					.createQueryList(queryConfigFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Conducts the loading test.
	 */
	@SuppressWarnings("unchecked")
	private void doTestLoading() {
		KbSpecification kb;
		Date startTime, endTime;

		for (int i = 0; i < kbList_.size(); i++) {
			kb = (KbSpecification) kbList_.get(i);

			repository_ = createRepository(kb.kbClass_);
			if (repository_ == null) {
				System.err.println(kb.kbClass_ + ": class not found!");
				System.exit(-1);
			}

			repository_.setOntology(kb.ontology_);

			System.out.println();
			System.out.println("Started loading " + kb.id_);

			startTime = new Date();
			if (isMemory_)
				repository_.open(null);
			else
				repository_.open(kb.dbFile_);
			if (!repository_.load(kb.dataDir_)) {
				repository_.close();
				return;
			}
			if (!isMemory_)
				repository_.close();

			endTime = new Date();
			kb.duration = (endTime.getTime() - startTime.getTime()) / 1000;
			kbList_.set(i, kb);
			System.out.println();
			System.out.println();
			System.out.println("Finished loading " + kb.id_ + "\t"
					+ kb.duration + " seconds");
		}

		showTestLoadingResults();
	}

	/** Conducts query test */
	private void doTestQuery() {
		queryTestResults_ = new QueryTestResult[kbList_.size()][queryList_
				.size()];
		for (int i = 0; i < kbList_.size(); i++) {
			testQueryOneKb(i);
		}
		showTestQueryResultsToFile();
		showTestQueryResults();
	}

	/**
	 * Conducts query test on the specified system.
	 * 
	 * @param index
	 *            Index of the system in the target system list.
	 */
	private void testQueryOneKb(int index) {
		QuerySpecification query;
		Date startTime, endTime;
		KbSpecification kb;
		long resultNum = 0;
		QueryResult result;

		kb = (KbSpecification) kbList_.get(index);

		if (!isMemory_)
			repository_ = createRepository(kb.kbClass_);
		if (repository_ == null) {
			System.err.println(kb.kbClass_ + ": class not found!");
			System.exit(-1);
		}

		// set ontology
		repository_.setOntology(kb.ontology_);

		// wait some
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}

		System.out.println();
		System.out.println("### Started testing " + kb.id_ + " ###");
		queryTestResultFile_.println(kb.id_);

		// test each query on this repository
		for (int i = 0; i < queryList_.size(); i++) {
			// flush system caches first
			flushFSCache();

			// open repository
			if (!isMemory_)
				repository_.open(kb.dbFile_);
			query = (QuerySpecification) queryList_.get(i);
			System.out.println();
			System.out.println("~~~" + query.id_ + "~~~");

			// issue the query for QUERY_TIME times
			queryTestResults_[index][i] = new QueryTestResult();
			for (int j = 0; j < QUERY_TIME; j++) {
				long duration = 0l;
				if (query.query_.getString().length() == 0) {
					// this query is empty, so it is not supported by the store
					duration = 0;
					resultNum = -3; // marks it as not supported
				} else if (prunedQueries_.contains(i)) {
					// this query took too much time previously, so don't
					// evaluate it again
					duration = 0;
					resultNum = -2; // marks it as pruned
				} else {
					startTime = new Date();
					result = repository_.issueQuery(query.query_);
					if (result == null) {
						// don't stop querying but note the error in the result
						// repository_.close();
						System.err.println("Query error!");
						resultNum = -1;
						duration = 0;
						j = QUERY_TIME;
						// System.exit(-1);
					} else {
						// traverse the result set.
						resultNum = 0;
						while (result.next() != false)
							resultNum++;
						endTime = new Date();
						duration = endTime.getTime() - startTime.getTime();

						// if this query took too long,
						// or if the j-times we evaluated this query already
						// exceeds the MAX_DURATION,
						// we do not want to evaluate it again
						if (duration > MAX_DURATION / 2) {
							prunedQueries_.add(i);
						}
					}
				}
				System.out.println("\tDuration: " + duration + "\tResult#: "
						+ resultNum);
				queryTestResults_[index][i].duration_.add(duration);
				queryTestResults_[index][i].resultNum_.add(resultNum);
			} // end of for j
				// close the repository
			if (!isMemory_)
				repository_.close();
			result = null;
			for (Long duration : queryTestResults_[index][i].duration_)
				queryTestResultFile_.println(duration);
			queryTestResultFile_.println(resultNum);
			queryTestResultFile_.println();
		} // end of for i
		System.out.println("### Finished testing " + kb.id_ + " ###");
	}

	/**
	 * Flushes system caches to avoid influences of previously executed queries
	 * on this system.
	 */
	private void flushFSCache() {
		try {
			Process flush = Runtime.getRuntime().exec(FLUSH_FS_CACHE_COMMAND);
			if (flush.waitFor() != 0) {
				System.err
						.println("Could not flush system filesystem caches, ignoring!");
				System.err
						.println("Subsequent queries now may influence each others performance!");
			} else {
				// wait some
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		} catch (IOException e) {
			System.err.println("could not run filesystem cache flush command '"
					+ FLUSH_FS_CACHE_COMMAND + "':");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.err
					.println("interruption while waiting for filesystem cache flush command '"
							+ FLUSH_FS_CACHE_COMMAND + "' to terminate:");
			e.printStackTrace();
		}
	}

	/**
	 * Displays the loading test results.
	 */
	private void showTestLoadingResults() {
		KbSpecification kb;

		System.out.println();
		for (int i = 0; i < kbList_.size(); i++) {
			kb = (KbSpecification) kbList_.get(i);
			System.out.println("\t" + kb.id_ + "\t" + kb.duration + " seconds");
		}
	}

	/**
	 * Displays the query test results.
	 */
	private void showTestQueryResults() {
		KbSpecification kb;
		QuerySpecification query;
		QueryTestResult result;

		System.out.println();
		for (int i = 0; i < kbList_.size(); i++) {
			kb = (KbSpecification) kbList_.get(i);
			System.out.print("\t\t" + kb.id_);
		}
		System.out.println();
		System.out.print("\t\tDuration");
		for (int i = 0; i < QUERY_TIME; i++) {
			System.out.print("\t");
		}
		System.out.println("Result#");
		for (int j = 0; j < queryList_.size(); j++) {
			query = (QuerySpecification) queryList_.get(j);
			System.out.print(query.id_);
			for (int i = 0; i < kbList_.size(); i++) {
				kb = (KbSpecification) kbList_.get(i);
				result = queryTestResults_[i][j];
				System.out.print("\t\t");
				for (Long duration : result.duration_)
					System.out.print(duration + "\t");
				for (Long results : result.resultNum_)
					System.out.print(results + "\t");
			}
			System.out.println();
		}
	}

	private void showTestQueryResultsToFile() {

		KbSpecification kb;
		QuerySpecification query;
		QueryTestResult result;

		queryTestResultFile_.println();
		for (int i = 0; i < kbList_.size(); i++) {
			kb = (KbSpecification) kbList_.get(i);
			queryTestResultFile_.print("\t\t" + kb.id_);
		}
		queryTestResultFile_.println();
		queryTestResultFile_.print("\t\tDuration");
		for (int i = 0; i < QUERY_TIME; i++) {
			queryTestResultFile_.print("\t");
		}
		queryTestResultFile_.println("Result#");
		for (int j = 0; j < queryList_.size(); j++) {
			query = (QuerySpecification) queryList_.get(j);
			queryTestResultFile_.print(query.id_);
			for (int i = 0; i < kbList_.size(); i++) {
				kb = (KbSpecification) kbList_.get(i);
				result = queryTestResults_[i][j];
				queryTestResultFile_.print("\t\t");
				for (Long duration : result.duration_)
					queryTestResultFile_.print(duration + "\t");
				for (Long results : result.resultNum_)
					queryTestResultFile_.print(results + "\t");
			}
			queryTestResultFile_.println();
		}
	}
}
