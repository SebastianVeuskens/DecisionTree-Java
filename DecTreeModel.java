import java.util.ArrayList;
//Main class to evaluate the specified data using 5-fold cross validation 
public class DecTreeModel {
  public static void main(String[] args) {
    String fileName;
    String functionName;
    char seperator = ',';
    boolean header = false;
    Integer label = 0;
    Object model = null;
    ArrayList<String> attributesName = null;
    ArrayList<Integer> attributes = new ArrayList();

    if (args.length < 2) {
      throw new RuntimeException("EXCEPTION: DATAFILE AND FUNCTION NAME MUST BE ENTERED!");
    }
    fileName = args[0];
    functionName = args[1];
    int numInput = 2;
    if (args.length > 2) {
      //Instead of entering an value, default can be entered at the optional inputs
      if (!args[2].equals("default")) {
        seperator = args[2].charAt(0);
      }
      numInput++;
    }
    if (args.length > 3) {
      header = args[3].equals("true");
      numInput++;
    }
    if (args.length > 4) {
      if (!args[4].equals("default")) {
        label = Integer.parseInt(args[4]);
      }
      numInput++;
    }
    if (args.length > 5) {
      attributesName = new ArrayList();
      for (int i  = numInput; i < args.length; i++) {
        attributesName.add(args[i]);
      }
    }
    Dataset data = null;
    try {
      data = new Dataset(fileName, seperator, header, attributesName, attributes);
    } catch (Exception e) { System.out.println(e); }

    Integer[] attributesArray = new Integer[attributes.size()];
    for (int t = 0; t < attributesArray.length; t++) {
      attributesArray[t] = attributes.get(t);
    }

    if (functionName.equals("GainRatio")) {
      //A model can be build for further uses
      //model = DecisionTreeGain.decTreeGain(data, label, (Integer[]) attributes.toArray());
      EvalTable[] table = DecisionTreeGain.crossValidation(5, data, label, attributesArray);
      Double accuracy = 0.0;
      for (int i = 0; i < table.length; i++) {
        accuracy += table[i].getAccuracy() / Double.valueOf(table.length);
      }
      System.out.println(accuracy);
    } else if (functionName.equals("Extended")) {
      //A model can be build for further uses
      //model = DecisionTreeExtended.decTreeExt(data, label, attributesArray);
      EvalTable[] table = DecisionTreeExtended.crossValidation(5, data, label, attributesArray);
      Double accuracy = 0.0;
      for (int i = 0; i < table.length; i++) {
        accuracy += table[i].getAccuracy() / Double.valueOf(table.length);
      }
      System.out.println(accuracy);
    } else {
      throw new RuntimeException("FUNCTION UNKOWN!");
    }
  }
}
