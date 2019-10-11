package ca.utoronto.utm.mcs;
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

public class getMovie implements HttpHandler {

  private Driver neo4jDriver;
  private String name;
  private String ID;
  private Map getResponse;

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

    JSONObject response = new JSONObject(getResponse);
    byte[] result = response.toString().getBytes();

    r.sendResponseHeaders(200, result.length);
    OutputStream os = r.getResponseBody();
    os.write(result);
    os.close();
  }

  public void get(String ID){
    try (Session session = neo4jDriver.session())
    {
      getResponse = session.writeTransaction( new TransactionWork<Map>() {
        @Override
        public Map execute(Transaction tx) {
          return getMovieData(tx, ID);
        }
      });
    }
  }

  public Map getMovieData(Transaction tx, String ID){

    //if the same movie is added twice, only one node should be created


    StatementResult result = tx.run("MATCH (m:Movie{id:$movieID})<-" +
            "[ACTED_IN]-(a:Actor) " +
            "RETURN m.id as movieID, m.name as name, collect(a.id) as actors",
        parameters("movieID", ID));

    //Get values from neo4j StatementResult object
    List<Record> records = result.list();
    Record record = records.get(0);
    Map recordMap = record.asMap();

    return recordMap;
  }
}