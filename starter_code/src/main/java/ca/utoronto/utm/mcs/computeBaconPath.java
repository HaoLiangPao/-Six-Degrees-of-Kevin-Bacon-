package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.internal.InternalPath;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.types.Node;

public class computeBaconPath implements HttpHandler {

  private Driver neo4jDriver;
  private String actorID;
  private String baconID = "1";
  private Map getResponse;
  private byte[] result;

  //constructor
  public computeBaconPath(neo4j database){
    neo4jDriver = database.getDriver();
  }

  public void handle(HttpExchange r) {
    try{
      if (r.getRequestMethod().equals("GET")) {
        handleGet(r);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void handleGet(HttpExchange r) throws IOException, JSONException {
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);

    System.out.println("getRelationship handler get:");
    System.out.println(deserialized);

    if (deserialized.has("actorID"))
      actorID = deserialized.getString("actorID");

    if (!actorID.equals(baconID)){
      //interaction with database
      get(actorID, baconID);
      //result for server-client interaction
      JSONObject responseJSON = new JSONObject(getResponse);
      //path object obtains baconPath return from database
      InternalPath path = (InternalPath) responseJSON.get("baconPath");
      Iterable<Node> nodeIterable = path.nodes();

      ArrayList<JSONObject> baconPath = new ArrayList<JSONObject>();
      //two lists for value storing
      ArrayList<String> actors = new ArrayList<String>();
      ArrayList<String> movies = new ArrayList<String>();

      //iterate nodes in baconPath
      for (Node node: nodeIterable
      ) {
        Map nodeMap = node.asMap();
        String aID = "";
        String mID = "";
        if (nodeMap.get("actorID") != null){
          aID = nodeMap.get("actorID").toString();
          actors.add(aID);
        }
        else {
          mID = nodeMap.get("movieID").toString();
          movies.add(mID);
        }
        System.out.println(actors);
        System.out.println(movies);
      }

      result = responseJSON.toString().getBytes();
    }
    else {
      JSONObject responseJSON = new JSONObject();
      responseJSON.put("baconNumber", "0");
      result = responseJSON.toString().getBytes();
    }

    r.sendResponseHeaders(200, 0);
    OutputStream os = r.getResponseBody();
    os.write(result);
    os.close();
  }

  public void get( final String actorID, final String movieID)
  {
    try ( Session session = neo4jDriver.session() )
    {
      getResponse = session.writeTransaction( new TransactionWork<Map>() {
        @Override
        public Map execute(Transaction tx) {
          return getRelationshipData(tx, actorID, movieID);
        }
      });
    }
  }

  private static Map getRelationshipData(Transaction tx, String actorID, String baconID) {
    StatementResult result = tx.run("MATCH p=shortestPath((a:Actor{actorID:$actorID})-[*]-" +
            "(b:Actor{actorID:$baconID})) " +
            "RETURN length(p)/2 as baconNumber, p as baconPath",
        parameters("actorID", actorID, "baconID", baconID));
    //Get values from neo4j StatementResult object
    List<Record> records = result.list();
    Record record = records.get(0);
    Map recordMap = record.asMap();

    return recordMap;
  }
}
