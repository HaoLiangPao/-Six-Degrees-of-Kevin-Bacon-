package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
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
  private JSONObject getResponse;

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
      getResponse = session.writeTransaction( new TransactionWork<JSONObject>() {
        @Override
        public JSONObject execute(Transaction tx) {
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
  private static JSONObject getActorData(Transaction tx, String ID) throws JSONException {
    System.out.println(ID);

    StatementResult result = tx.run("MATCH (a:Actor{actorID:$actorID})-" +
            "[ACTED_IN]->(m:Movie) RETURN a.actorID, a.name, m.movieID",
        parameters("actorID", ID));
    //Get values from neo4j StatementResult object

    System.out.println("getActorData is running:");
    JSONObject jsonReturn = new JSONObject();
    JSONArray movies = new JSONArray();

    while (result.hasNext() == true){
      Record record = result.next();
      if (record.get("a.actorID") != null){
        System.out.println(record.get("a.actorID").toString());
        System.out.println(record.get("a.actorID"));

        jsonReturn.put("actorID", record.get("a.actorID").toString());
      }
      if (record.get("a.name") != null){
        jsonReturn.put("name", record.get("a.name").toString());
      }
      movies.put(record.get("m.movieID").toString());
    }
    jsonReturn.put("movies", movies);

    JSONObject abc = new JSONObject();
    abc.put("123", "123");
    abc.put("234", "234");
    abc.put("345", "234");
    System.out.println(abc);

    System.out.println(jsonReturn.toString());
    System.out.println(movies.getString(0));
    System.out.println(movies.get(0).toString());
    System.out.println(movies.get(0).toString().getClass());
    System.out.println(movies.get(0).getClass());

    return jsonReturn;
  }


  public void handleGet(HttpExchange r) throws IOException, JSONException {
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);

    // See body and deserilized
    System.out.println("addActor HandleGet :");
    System.out.println(deserialized);

    if (deserialized.has("actorID"))
      ID = deserialized.getString("actorID");

    //Interaction with database + assign values to JSONObjects already
    get(ID);
    byte[] result = getResponse.toString().getBytes();

    r.sendResponseHeaders(200, result.length);
    OutputStream os = r.getResponseBody();
    os.write(result);
    os.close();
  }
}
