package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class addActor implements HttpHandler
{
  private Driver neo4jDriver;
  private String name;
  private String ID;
  private String response;

  //constructor
  public addActor(neo4j database){
    neo4jDriver = database.getDriver();
    System.out.println("neo4jDriver is set");
    System.out.println(neo4jDriver.toString());
  }

  public void handle(HttpExchange r) {
    try {
      if (r.getRequestMethod().equals("GET")) {
        handleGet(r);
      } else if (r.getRequestMethod().equals("POST")) {
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
      response = session.writeTransaction( new TransactionWork<String>() {
        @Override
        public String execute(Transaction tx) {
          return createActor(tx, name, ID);
        }
      });
    }
    System.out.println(neo4jDriver.session());
  }

  private static String createActor(Transaction tx, String name, String ID){
    System.out.println("private function creatActor is running");
    StatementResult result = tx.run( "CREATE (a:Actor) " +
            "SET a.name = $name, a.ID = $ID " +
            "RETURN 'a.ID is added'",
        parameters("name", name , "ID", ID));
    return result.single().get(0).asString();
  }

  public void get( final String ID )
  {
    try ( Session session = neo4jDriver.session() )
    {
      String greeting = session.writeTransaction( new TransactionWork<String>() {
        @Override
        public String execute(Transaction tx) {
          return getActor(tx, ID);
        }
      });
    }
  }
  private static String getActor(Transaction tx, String ID) {
    StatementResult result = tx.run("MATCH (a:Actor) " +
            "MATCH (a:Actor { ID: $ID })"
            + "RETURN a.name",
        parameters("ID", ID));
    return result.single().get(0).asString();
  }


  public void handleGet(HttpExchange r) throws IOException, JSONException {
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);

    // See body and deserilized
    System.out.println("addActor HandleGet :");
    System.out.println(deserialized);

    if (deserialized.has("name"))
      name = deserialized.getString("name");

    if (deserialized.has("ID"))
      ID = deserialized.getString("ID");

    //Interaction with database
    get(ID);

    r.sendResponseHeaders(200, response.length());
    OutputStream os = r.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  public void handlePut(HttpExchange r) throws IOException, JSONException {
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);

    // See body and deserilized
    System.out.println(body);
    System.out.println(deserialized);


    if (deserialized.has("name"))
      name = deserialized.getString("name");

    if (deserialized.has("ID"))
      ID = deserialized.getString("ID");

    //interaction with database
    add(name, ID);

    r.sendResponseHeaders(200, 0);
    OutputStream os = r.getResponseBody();
//    os.write(response.getBytes());
    os.close();
  }

}

