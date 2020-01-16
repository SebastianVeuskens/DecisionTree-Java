import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

//Read the data from a file and store them to provide acces for further processing
public class Dataset {
  private Object[][] data;

  public Dataset(String fileName, char seperator, boolean header,
  ArrayList<String> attributesName, ArrayList<Integer> attributes) {
    FileInputStream inputStream = null;
    InputStreamReader inputReader = null;
    BufferedReader reader = null;
    try {
      inputStream = new FileInputStream(fileName);
      inputReader = new InputStreamReader(inputStream);
      reader = new BufferedReader(inputReader);
    } catch (Exception e) {
      System.out.println(e);
    }

    LinkedList<LinkedList<String>> dataList = new LinkedList();
    for (int i = 0; true; i++) {
      try {
        if (reader == null) {
          throw new IOException("No File open!");
        }
        String lineI = reader.readLine();
        if (lineI != null && !header) {
          LinkedList<String> attList = new LinkedList();
          dataList.add(i, attList);
          String[] line = lineI.split(Character.toString(seperator));
          for (int j = 0; j < line.length; j++) {
            dataList.get(i).add(j, line[j]);
          }
          if (attributes.isEmpty()) {
            for (int k = 0; k < line.length; k++) {
              attributes.add(k);
            }
          }
        } else if (!header) {
          break;
        } else {
          String[] line = lineI.split(Character.toString(seperator));
          if (attributesName != null) {
            for (int k = 0; k < line.length; k++) {
              if (attributesName.contains(line[k])) {
                attributes.add(k);
                attributesName.remove(line[k]);
              }
            }
            if (!attributesName.isEmpty()) {
              throw new RuntimeException("ATTRIBUTES NOT FOUND");
            }
          } else {
            for (int k = 0; k < line.length; k++) {
              attributes.add(k);
            }
          }
          i--;
          header = false;
        }
      } catch (IOException e) {
        System.out.println("Exception-" + e.getMessage());
        break;
      }
    }
    int i = 0;
    this.data = new String[dataList.size()][dataList.get(0).size()];
    for (LinkedList ins : dataList) {
      int j = 0;
      for (String att : dataList.get(i)) {
        this.data[i][j] = att;
        j++;
      }
      i++;
    }

    try {
      inputStream.close();
      inputReader.close();
      reader.close();
    } catch (IOException e) {
      System.out.println("Could not close");
    }
  }

  public Dataset(int numIns, int numAtt) {
    this.data = new Object[numIns][numAtt];
  }

  public int getNumAtt() {
    return this.data[0].length;
  }

  public int length() {
    return this.data.length;
  }

  public Object[][] getData() {
    return this.data;
  }
}
