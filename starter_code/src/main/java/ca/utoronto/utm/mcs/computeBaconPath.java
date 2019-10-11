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
    //get Request variables
    String body = Utils.convert(r.getRequestBody());
    JSONObject deserialized = new JSONObject(body);
    JSONObject responseJSON = new JSONObject();
    System.out.println("getRelationship handler get:");
    System.out.println(deserialized);
    if (deserialized.has("actorID"))
      actorID = deserialized.getString("actorID");
    //normal case to compute a baconNumber&baconPath
    if (!actorID.equals(baconID)){
      //interaction with database
      get(actorID, baconID);
      //add baconNumber response
      responseJSON.put("baconNumber", getResponse.get("baconNumber"));
      //add baconPath in a list<JSONObject> form
      responseJSON.put("baconPath", createBaconPath(getResponse));
      //write to a byte[] for OutputStream
      result = responseJSON.toString().getBytes();
    }
    else {
      responseJSON.put("baconNumber", "0");
      result = responseJSON.toString().getBytes();
    }
    r.sendResponseHeaders(200, 0);
    OutputStream os = r.getResponseBody();
    os.write(result);
    os.close();
  }


  private List<JSONObject> createBaconPath(Map response) throws JSONException {
    //change format of data from: Path ->Iterable<Node> ->List<JSONObject>
    InternalPath path = (InternalPath) response.get("baconPath");
    Iterable<Node> nodeIterable = path.nodes();
    List<JSONObject> baconPath = new ArrayList<JSONObject>();//for baconPath return
    //change format of data from: Nodes ->Two Lists
    List<String> actorsINPATH = new ArrayList<String>();
    List<String> moviesINPATH = new ArrayList<String>();
    for (Node node: nodeIterable
    ) {
      Map nodeMap = node.asMap();
      String aID,mID = "";
      if (nodeMap.get("actorID") != null){
        aID = nodeMap.get("actorID").toString();
        actorsINPATH.add(aID);
      }
      else {
        mID = nodeMap.get("movieID").toString();
        moviesINPATH.add(mID);
      }
    }
    //implementing baconPath
    Integer index = 0;
    while (index < actorsINPATH.size()) {
      JSONObject pathPoint = new JSONObject();
      pathPoint.put("actorID", actorsINPATH.get(index));
      //movie list always has one item less than actor node
      if (index == moviesINPATH.size()) {
        //put the last movie again with Kevin Bacon
        pathPoint.put("movieID", moviesINPATH.get(index - 1));
      }
      //normal combination as a slice in baconPath
      else { pathPoint.put("movieID", moviesINPATH.get(index)); }
      index += 1;
      //add slice of baconPath to 'complete path' list
      baconPath.add(pathPoint);
    }
    return baconPath;
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
