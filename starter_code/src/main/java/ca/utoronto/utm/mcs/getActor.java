package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class getActor implements HttpHandler {
  private Driver neo4jDriver;
  private String ID;
  private Map getResponse;

  //constructor
  public getActor(neo4j database) {
    neo4jDriver = database.getDriver();
    System.out.println("getActor: neo4jDriver is set");
    System.out.println(neo4jDriver.toString());
  }

  @Override
  public void handle(HttpExchange r) throws IOException {
    try {
      if (r.getRequestMethod().equals("GET")) {
        handleGet(r);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void get( final String ID )
  {
    try ( Session session = neo4jDriver.session() )
    {
      getResponse = session.writeTransaction( new TransactionWork<Map>() {
        @Override
        public Map execute(Transaction tx) {
          try {
            return getActorData(tx, ID);
          } catch (JSONException e) {
            e.printStackTrace();
          }
          return null;
        }
      });
    }
  }
  private static Map getActorData(Transaction tx, String ID) throws JSONException {
    System.out.println(ID);

    StatementResult result = tx.run("MATCH (a:Actor{id:$actorID})-" +
            "[ACTED_IN]->(m:Movie) " +
            "RETURN a.id as actorID, a.name as name, collect(m.id) as movies",
        parameters("actorID", ID));
    //Get values from neo4j StatementResult object

    System.out.println("getActorData is running:");
    //Get values from neo4j StatementResult object
    List<Record> records = result.list();
    Record record = records.get(0);
    Map recordMap = record.asMap();

    return recordMap;
  }


  public void handleGet(HttpExchange r) throws IOException, JSONException {
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);

    //See body and deserilized
    System.out.println("addActor-HandelGet get input:");
    System.out.println(deserialized);

    if (deserialized.has("actorID"))
      ID = deserialized.getString("actorID");

    //Interaction with database + assign values to JSONObjects already
    get(ID);

    JSONObject response = new JSONObject(getResponse);
    byte[] result = response.toString().getBytes();

    if (result != null){
      r.sendResponseHeaders(200, result.length);
    }
    else{
      r.sendResponseHeaders(400, result.length);
    }

    OutputStream os = r.getResponseBody();
    os.write(result);
    os.close();
  }
}
