package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class App
{
    static int PORT = 8080;

    public static void main(String[] args) throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        String url = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "123456";
        neo4j database = new neo4j(url, user, password);
        System.out.println(database.getDriver());

        //Read TSV file
        TsvReader actors = new TsvReader("/Users/lianghao/Desktop/CSCC01/CSCC01-A1/starter_code/src/main/java/ca/utoronto/utm/mcs/actors-testing.tsv");

        server.createContext("/api/v1/addActor", new addActor(database));
        server.createContext("/api/v1/addMovie", new addMovie(database));
        server.createContext("/api/v1/addRelationship", new addRelationship(database));
        server.createContext("/api/v1/getActor", new getActor(database));
        server.createContext("/api/v1/getMovie", new getMovie(database));
        server.createContext("/api/v1/hasRelationship", new hasRelationship(database));
        server.createContext("/api/v1/computeBaconPath", new computBaconPath(database));
        server.createContext("/api/v1/computeBaconNumber", new computeBaconNumber(database));

        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
