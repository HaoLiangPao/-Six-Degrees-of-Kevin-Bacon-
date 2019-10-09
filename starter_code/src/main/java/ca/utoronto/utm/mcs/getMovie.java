package ca.utoronto.utm.mcs;

    import static org.neo4j.driver.v1.Values.ofList;
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

public class getMovie implements HttpHandler {

  private Driver neo4jDriver;
  private String name;
  private String ID;
  private JSONObject response;

  //constructor
  public getMovie(neo4j database) {
    neo4jDriver = database.getDriver();
  }

  public void handle(HttpExchange r) {
    try {
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

    System.out.println("addMovie handler get:");
    System.out.println(deserialized);

    if (deserialized.has("movieID"))
      ID = deserialized.getString("movieID");

    //interaction with database
    get(ID);
    byte[] result = response.toString().getBytes();

    r.sendResponseHeaders(200, result.length);
    OutputStream os = r.getResponseBody();
    os.write(result);
    os.close();
  }

  public void get(String ID){
    try (Session session = neo4jDriver.session())
    {
      response = session.writeTransaction( new TransactionWork<JSONObject>() {
        @Override
        public JSONObject execute(Transaction tx) {
          try {
            return getMovieData(tx, ID);
          } catch (JSONException e) {
            e.printStackTrace();
          }
          return null;
        }
      });
    }
  }

  public JSONObject getMovieData(Transaction tx, String ID) throws JSONException {

    //if the same movie is added twice, only one node should be created


    StatementResult result = tx.run("MATCH (m:Movie{movieID:$movieID})<-" +
            "[ACTED_IN]-(a:Actor) RETURN m.movieID, m.name, a.actorID",
        parameters("movieID", ID));

    //Get values from neo4j StatementResult object

    System.out.println("getMovie is running:");
    JSONObject jsonReturn = new JSONObject();
    JSONArray actors = new JSONArray();

    while (result.hasNext() == true){
      Record record = result.next();
      actors.put(record.get("a.actorID"));
      if (record.get("m.movieID") != null){
        jsonReturn.put("movieID", record.get("m.movieID"));
      }
      if (record.get("m.name") != null){
        jsonReturn.put("name", record.get("m.name"));
      }
    }
    jsonReturn.put("actors", actors);

    System.out.println(jsonReturn.toString());

    return jsonReturn;
  }
}