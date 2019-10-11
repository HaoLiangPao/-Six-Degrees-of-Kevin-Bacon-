package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.ofList;
import static org.neo4j.driver.v1.Values.parameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class addMovie implements HttpHandler {

  private Driver neo4jDriver;
  private String name;
  private String ID;
  private Map response;

  //constructor
  public addMovie(neo4j database) {
    neo4jDriver = database.getDriver();
  }

  public void handle(HttpExchange r) {
    try {
      if (r.getRequestMethod().equals("PUT")) {
        handlePut(r);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void handlePut(HttpExchange r) throws IOException, JSONException {
	  try {
	    String body = Utils.convert(r.getRequestBody());
	    JSONObject deserialized = new JSONObject(body);
	
	    System.out.println("addMovie handler get:");
	    System.out.println(deserialized);
	    if (!deserialized.has("name") || !deserialized.has("movieID")) {
	    	r.sendResponseHeaders(400, -1);
	    }
	    else {
		    name = deserialized.getString("name");
		    ID = deserialized.getString("movieID");
		
		    //interaction with database
		    add(name, ID);
		    //result for server-client interaction
		    JSONObject responseJSON = new JSONObject();
		    responseJSON.put("name", response.get("m.name"));
		    responseJSON.put("movieID", response.get("m.id"));
		    byte[] result = responseJSON.toString().getBytes();
		
		    r.sendResponseHeaders(200, result.length);
		    OutputStream os = r.getResponseBody();
		    os.write(result);
		    os.close();
	    	}
	  	}
	 catch(Exception e) {
		 r.sendResponseHeaders(500, -1);
	 }
  }

  public void add(String name, String ID){
    try (Session session = neo4jDriver.session())
    {
      response = session.writeTransaction( new TransactionWork<Map>() {
        @Override
        public Map execute(Transaction tx) {
          return createMovie(tx, name, ID);
        }
      });
    }
  }

  public Map createMovie(Transaction tx, String  name, String ID){

    //if the same movie is added twice, only one node should be created


    StatementResult result = tx.run("MERGE (m:Movie {id:$movieID}) " +
        "ON CREATE SET m.name = $name " +
        "RETURN m.name, m.id",
        parameters("name", name , "movieID", ID));

    System.out.println("\ncreateMovie is returning the result");
    //Get values from neo4j StatementResult object
    List<Record> records = result.list();
    Record record = records.get(0);
    Map recordMap = record.asMap();

    return recordMap;
  }
}