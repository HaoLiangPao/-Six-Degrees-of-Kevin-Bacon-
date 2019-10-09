package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;

import java.util.List;
import java.util.Map;
import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class addActor implements HttpHandler {
  private Driver neo4jDriver;
  private String name;
  private String ID;
  private Map addResponse;

  //constructor
  public addActor(neo4j database){
    neo4jDriver = database.getDriver();
    System.out.println("neo4jDriver is set");
    System.out.println(neo4jDriver.toString());
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


  public void add(String name, String ID )
  {
    System.out.println("private add function is running");
    try ( Session session = neo4jDriver.session() )
    {
      System.out.println("session is created");
      addResponse = session.writeTransaction( new TransactionWork<Map>() {
        @Override
        public Map execute(Transaction tx) {
          return createActor(tx, name, ID);
        }
      });
    }
    System.out.println(neo4jDriver.session());
  }

  private static Map createActor(Transaction tx, String name, String ID){
    System.out.println("private function creatActor is running");
    StatementResult result = tx.run( "MERGE (a:Actor{actorID:$actorID}) " +
            "ON CREATE SET a.name = $name " +
            "RETURN a.name, a.actorID",
        parameters("name", name , "actorID", ID));
    //Get values from neo4j StatementResult object
    List<Record> records = result.list();
    Record record = records.get(0);
    Map recordMap = record.asMap();
    return recordMap;
  }

  public void handlePut(HttpExchange r) throws IOException, JSONException {
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);

    // See body and deserilized
    System.out.println(body);
    System.out.println(deserialized);


    if (deserialized.has("name"))
      name = deserialized.getString("name");

    if (deserialized.has("actorID"))
      ID = deserialized.getString("actorID");

    //interaction with database
    // what if name or ID is none??
    add(name, ID);

    //result for server-client interaction
    JSONObject responseJSON = new JSONObject();
    responseJSON.put("name", addResponse.get("a.name"));
    responseJSON.put("actorID", addResponse.get("a.actorID"));
    byte[] result = responseJSON.toString().getBytes();

    r.sendResponseHeaders(200, result.length);
    OutputStream os = r.getResponseBody();
    os.write(result);
    os.close();
  }

}

