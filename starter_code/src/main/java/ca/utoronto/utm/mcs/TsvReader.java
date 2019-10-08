package ca.utoronto.utm.mcs;

import com.sun.tools.javac.util.ArrayUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.server.ExportException;
import java.util.Arrays;

public class TsvReader{

  private String[] data;

  // return the data read from specific file
  public String[] getData() {
    return data;
  }

  public void readData(String url) throws Exception {
    BufferedReader TSVFile =
        new BufferedReader(new FileReader(url));

    String dataRow = TSVFile.readLine(); // Read first line.

    while (dataRow != null){
      String[] dataArray = dataRow.split("\\t");
      //data = ArrayUtils.addAll(data, dataArray);
      for (String item:dataArray) {
        System.out.print(item + "  ");
      }
      System.out.println(); // Print the data line.
      dataRow = TSVFile.readLine(); // Read next line of data.
    }

    //System.out.println("dataArray is :");
    //System.out.println(Arrays.toString(dataArray));

// Close the file once all data has been read.
    TSVFile.close();
  }

  public TsvReader(String url) throws Exception {
    readData(url);
  }
}