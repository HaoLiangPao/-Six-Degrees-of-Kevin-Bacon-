package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class addMovie implements HttpHandler {

  private Driver neo4jDriver;
  private String name;
  private String ID;
  private String response;

  //constructor
  public addMovie(neo4j database) {
    neo4jDriver = database.getDriver();
  }

  public void handle(HttpExchange r) {
    try {
      if (r.getRequestMethod().equals("GET")) {
        //handleGet(r);
      } else if (r.getRequestMethod().equals("POST")) {
        handlePut(r);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void handlePut(HttpExchange r) throws IOException, JSONException {
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);

    System.out.println("addMovie handler get:");
    System.out.println(deserialized);

    if (deserialized.has("name"))
      name = deserialized.getString("name");

    if (deserialized.has("ID"))
      ID = deserialized.getString("ID");

    //interaction with database
    add(name, ID);

    r.sendResponseHeaders(200, 0);
    OutputStream os = r.getResponseBody();
    os.close();
  }

  public void add(String name, String ID){
    try (Session session = neo4jDriver.session())
    {
      response = session.writeTransaction( new TransactionWork<String>() {
        @Override
        public String execute(Transaction tx) {
          return createMovie(tx, name, ID);
        }
      });
    }
  }

  public String createMovie(Transaction tx, String  name, String ID){
    StatementResult result = tx.run("CREATE (m:Movie) " +
        "SET m.name = $name, m.ID = $ID " +
        "RETURN m.name",
        parameters("name", name , "ID", ID));
    return result.single().get(0).asString();
  }
}