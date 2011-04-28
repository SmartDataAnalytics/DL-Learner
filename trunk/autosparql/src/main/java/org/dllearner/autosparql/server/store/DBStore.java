package org.dllearner.autosparql.server.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.dllearner.autosparql.client.exception.SPARQLQuerySavingFailedException;
import org.dllearner.autosparql.client.model.Example;
import org.dllearner.autosparql.client.model.StoredSPARQLQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class DBStore implements Store{
	
	private JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource){
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public void init(){
		String query = "CREATE TABLE IF NOT EXISTS SPARQL_QUERY_STORE (QUESTION(VARCHAR(5000) PRIMARY KEY, QUERY VARCHAR(20000), ENDPOINT VARCHAR(2000), HITCOUNT SHORT)";
		jdbcTemplate.execute(query);
	}

	@Override
	public void saveSPARQLQuery(String question, String query, String endpoint, List<Example> posExamples, List<Example> negExamples, Example lastSuggestedExample)
			throws SPARQLQuerySavingFailedException {
		String sqlQuery = "INSERT INTO SPARQL_QUERY_STORE(QUESTION, QUERY, ENDPOINT, HITCOUNT) VALUES(?, ?, ?, ?) ";
		jdbcTemplate.update(sqlQuery, question, query, endpoint, Integer.valueOf(0));
		
	}

	@Override
	public List<StoredSPARQLQuery> getStoredSPARQLQueries() {
		String sqlQuery = "SELECT * FROM SPARQL_QUERY_STORE";
		
		List<StoredSPARQLQuery> queries = jdbcTemplate.query(sqlQuery, new RowMapper<StoredSPARQLQuery>() {

			@Override
			public StoredSPARQLQuery mapRow(ResultSet rs, int rowNum) throws SQLException {
				StoredSPARQLQuery query = new StoredSPARQLQuery();
				
				query.setQuestion(rs.getString("QUESTION"));
				query.setQuery(rs.getString("QUERY"));
				query.setEndpoint(rs.getString("ENDPOINT"));
				
				return query;
			}
		});
		
		return queries;
	}

	@Override
	public void incrementHitCount(StoredSPARQLQuery storedQuery) {
		String query = "SELECT HITCOUNT FROM SPARQL_QUERIES_STORE WHERE QUESTION = ?";
		int count = jdbcTemplate.queryForInt(query);
		query = "UPDATE SPARQL_QUERIES_STORE SET HITCOUNT = ?";
		jdbcTemplate.update(query, Integer.valueOf(count++));
	}

}
