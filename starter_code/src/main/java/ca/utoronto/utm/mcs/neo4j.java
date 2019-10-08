package ca.utoronto.utm.mcs;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class neo4j implements AutoCloseable{
  private Driver driver;

  //Database neo4j constructor
  public neo4j(String url, String user, String password){
    driver = GraphDatabase.driver( url, AuthTokens.basic( user, password ) );
    System.out.println(driver.toString());
  }

  @Override
  public void close() throws Exception
  {
    driver.close();
  }

  public Driver getDriver(){
    return driver;
  }
}
