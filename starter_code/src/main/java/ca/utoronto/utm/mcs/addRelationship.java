package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.json.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class addRelationship implements HttpHandler {

  private Driver neo4jDriver;
  private String actorID;
  private String movieID;
  private String addResponse;

  //constructor
  public addRelationship(neo4j database){
    neo4jDriver = database.getDriver();
  }

  public void handle(HttpExchange r) {
    try{
      if (r.getRequestMethod().equals("PUT")) {
        handlePut(r);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void handlePut(HttpExchange r) throws IOException, JSONException {
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);

    System.out.println("addRelationship handler get:");
    System.out.println(deserialized);

    if (deserialized.has("actorID"))
      actorID = deserialized.getString("actorID");

    if (deserialized.has("movieID"))
      movieID = deserialized.getString("movieID");

    //interaction with database
    add(actorID, movieID);

    r.sendResponseHeaders(200, 0);
    OutputStream os = r.getResponseBody();
    os.close();
  }

  public void add(String actorID, String movieID){
    try (Session session = neo4jDriver.session())
    {
      addResponse = session.writeTransaction( new TransactionWork<String>() {
        @Override
        public String execute(Transaction tx) {
          return createRelationship(tx, actorID, movieID);
        }
      });
    }
  }

  public String createRelationship(Transaction tx, String  actorID, String movieID){
    StatementResult result = tx.run("MATCH (a:Actor), (b:Movie) " +
            "WHERE a.actorID = $actorID AND b.movieID = $movieID " +
            "MERGE (a)-[r:ACTED_IN]->(b)" +
            "RETURN type(r)",
        parameters("actorID", actorID , "movieID", movieID));
    return result.single().get(0).asString();
  }
}
