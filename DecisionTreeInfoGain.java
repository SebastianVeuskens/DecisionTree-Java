import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

//package decision_tree;

public class DecisionTreeInfoGain {
  //public final static char EOF = (char)-1;

  public Node root;
  public Object[][] data;

  //Nochmal genauer über die Baumstruktur nachdenken
  public DecisionTreeInfoGain() {
    root = new Node();
    //root.children = new ArrayList<Node>(); -> Kann wahrscheinlich raus
  }

  /**
   *Apply the DecisionTreeInfoGain algorithm on the dataset with default seperator ','
   *Attribute werden nach Split noch weiter als Kriterium in Betracht bezogen
   *
   *@param reader the training set
   *@return the trained decision tree model
   */
  public static DecisionTreeInfoGain decTree(Dataset data, Integer label, Integer[] attributes) {
    DecisionTreeInfoGain model = new DecisionTreeInfoGain();
    model.setData(data, attributes);
    Integer[] instances = new Integer[data.length()];
    for (int i = 1; i <= data.length(); i++) {
      instances[i] = Integer.valueOf(i);
    }
    //Muss noch geändert werden (-1)
    model.apply(label, instances, model.getNode(), 0);
    return model;
  }

  //zweite crossValidation Methode schreiben, die auch ohne vorher bereits er-
  //stellten DecisionTreeInfoGain auskommt (also eine statische Methode, die auch die
  //Daten selber einließt)

  /**
   *Apply the n-fold cross Validation method on this decision tree
   *
   *@param n number of folds
   *@return the pairs of predicted and real label of each instance
   */
  public EvalTable crossValidation(int n) {
     EvalTable result = new EvalTable();
     if (n < 1 || n > this.numAtt()) {
       throw new RuntimeException("Illegal number of folds: Must be positive" +
       "and not more than number of instances!\n");
     }
     //Daten zufällig sortieren?
     Object[][] croValData = this.data.clone();
     Integer[] trainInstances;
     Integer[] testInstances;
     int d;
     int e;
     for (int i = 1; i <= n; i++) {
       //nochmal genau anschauen, ob hier alles so stimmt
       if (croValData.length % n == 0 || i > croValData.length % n) {
         trainInstances = new Integer[croValData.length - (croValData.length / n)];
         testInstances = new Integer[(croValData.length / n)];
       } else {
         trainInstances = new Integer[croValData.length - (croValData.length / n + 1)];
         testInstances = new Integer[(croValData.length / n + 1)];
       }
       d = 0;
       e = 0;
       for (int j = 0; j < croValData.length; j++) {
         if (j % n + 1 != i) {
           trainInstances[d] = Integer.valueOf(j);
           d++;
         } else {

         }
       }
       DecisionTreeInfoGain croValTree = this.train(trainInstances);
       result.add(croValTree.test(testInstances));
     }
     return result;
   }

  public DecisionTreeInfoGain train(Integer[] instances) {
    return null;
   }

  /**
    *
    *
    *
    */
  private Object[] test(Integer[] instances) {
    return null;
  }

  public void apply(Integer label, Integer[] instances, Node parent,
  int madeSplits) {
    if (madeSplits + 1 < this.numAtt()) {
      LinkedList evalRes = this.searchCrit(label, instances);
      if (evalRes != null) {
        //Neue Sub-Datasets entsprechend des Splitkriterions erstellen
        //attMap ist die HashMap, die als Werte HashMaps enthält, die jeweils die
        //gleiche Ausprägung des Attributs unter splitCrit haben
        HashMap<Object,HashMap> attMap = (HashMap) evalRes.removeLast();
        Integer splitCrit = (Integer) evalRes.removeLast();
        //Double infoDN = (Double) evalRes.removeLast();
        parent.setData(splitCrit);

        //value goes through all specification that the specified attribute can take
        for (HashMap<Object,ArrayList> value : attMap.values()) {
          int numInstances = 0;
          for (ArrayList<Double> sameLabel : value.values()) {
            numInstances += sameLabel.size();
          }
          Integer[] insVal = new Integer[numInstances];
          Integer i = 0;
          //Weiß gerade nicht, welche Generics
          //Doch schwieriger als gedacht
          for (ArrayList<Double> sameLabel : value.values()) {
            for (Double instance : sameLabel) {
              insVal[i] = instance.intValue();
              i++;
            }
          }
          Node node = new Node();
          this.apply(label, insVal, node, madeSplits + 1);
          //If another split was made
          if (node.hasData()) {
            node.setParent(parent);
          }
        }
      }
    }
  }

  //Find the attribute with the purest result after splitting
  public LinkedList searchCrit(Integer label, Integer[] instances) {
    Double threshold = 0.0;
    //Ähnlich zu noch zu erledigendem Task oben Struktur für Datendurchlauf implementieren
    //-> Muss hier vielleicht nicht einmal sein
    //Assign the values to the datastructure
    HashMap<Object,HashMap<Object,ArrayList>>[] eachAtt = new HashMap[this.numAtt()];
    for (int i = 0; i < eachAtt.length; i++) {
      eachAtt[i] = new HashMap();
    }
    this.initialiseDatastructure(eachAtt, label, instances);
    //Compute the information Gain for each attribute
    //Find therefore first the probabilities for each attribute split and their labels
    HashMap<Object,Double>[] frequencies = new HashMap[this.numAtt()];
    this.calculateFrequencies(frequencies, label, eachAtt);

    //Compute the info for each attribute
    Double[] info = new Double[this.numAtt()];
    this.calculateInfo(info, eachAtt, frequencies, label, instances.length);
    Double infoD = this.calculateInfoD(instances, label);

    //Find the attribute that provides the highest infoGain if splitted there
    LinkedList result = new LinkedList();
    //result.add(-1);
    Integer splitCrit = -1;
    Double maxGain = Double.valueOf(0);
    for (int i = 0; i < info.length; i++) {
      if (infoD - info[i] > threshold && infoD - info[i] > maxGain && i != label) {
        splitCrit = i;
        maxGain = infoD - info[i];
        //result.removeLast();
        //result.add(info[i]);
      }
    }
    if (splitCrit == -1) {
      return null;
    }
    result.add(splitCrit);
    result.add(eachAtt[splitCrit].clone());
    return result;
  }

  public void setData(Dataset data, Integer[] attributes) {
    //Spaltennamen beachten
    Object[][] essentialData = new Object[data.length()][attributes.length];
    int i = 0;
    for (Integer att : attributes) {
      for (int ins = 0; ins < data.length(); ins++) {
        essentialData[ins][i] = data.getData()[ins][att];
        i++;
      }
    }
    this.data = essentialData;
    if (this.data.length == 0) {
      throw new RuntimeException("Initialisation exception: No instances given!");
    }
    //Create a reduced dataset with only the important features/attributes
    /*Dataset redData = new Dataset(this.getNumIns(), attributes.length);
    for (Integer ins = 0; ins < data.getNumIns(); ins++) {
      this.setIns(ins, attributes, data);
    }*/
  }

  public void initialiseDatastructure(HashMap<Object,HashMap<Object,ArrayList>>[]
  eachAtt, Integer label, Integer[] instances) {
    for (int att = 0; att < eachAtt.length; att++) {
      if (att != label && instances != null) {
        for (Integer ins : instances) {
          if (!eachAtt[att].containsKey(this.data[ins][att])) {
            eachAtt[att].put(data[ins][att], new HashMap<Object,ArrayList>());
          }
          //Contains all instances with the same attribute but with different labels
          HashMap<Object,ArrayList> diffLab = eachAtt[att].get(data[ins][att]);
          if (!diffLab.containsKey(data[ins][label])) {
            diffLab.put(data[ins][label], new ArrayList<Double>());
          }
          diffLab.get(data[ins][label]).add(new Double(ins));
        }
      }
    }
  }

  public void calculateFrequencies(HashMap<Object,Double>[] frequencies,
  Integer label, HashMap<Object,HashMap<Object,ArrayList>>[]
  eachAtt) {
    for (int att = 0; att < eachAtt.length; att++) {
      frequencies[att] = new HashMap<Object,Double>();
      if (att != label) {
        for (Map.Entry dummy : eachAtt[att].entrySet()) {
          Object key = dummy.getKey();
          HashMap diffLab = (HashMap) dummy.getValue();
          for (Object dummy2 : diffLab.values()) {
            ArrayList similarIns = (ArrayList) dummy2;
            if (frequencies[att].containsKey(key)) {
              frequencies[att].put(key, frequencies[att].get(key) + Double.valueOf(similarIns.size()));
            } else {
              frequencies[att].put(key, Double.valueOf(similarIns.size()));
            }
          }
        }
      }
    }
  }

  public void calculateInfo(Double[] info, HashMap<Object,HashMap<Object,ArrayList>>[]
  eachAtt, HashMap<Object,Double>[] frequencies, Integer label, int frequTotal) {
    for (int att = 0; att < eachAtt.length; att++) {
      Double[] attInfoGains = new Double[eachAtt[att].size()];
      info[att] = new Double(0);
      if (att != label) {
        Integer i = 0;
        for (Map.Entry dummy : eachAtt[att].entrySet()) {
          Object key = dummy.getKey();
          HashMap<Object,ArrayList> diffLab = (HashMap) dummy.getValue();
          attInfoGains[i] = new Double(0);
          for (ArrayList dummy2 : diffLab.values()) {
            //Das richtige similarIns hier? Oder oben?
            ArrayList similarIns = dummy2;
            Double p = Double.valueOf(similarIns.size()) / frequencies[att].get(key);
            attInfoGains[i] -= p * log2(p);
          }
          i++;
        }
      }
      Integer i = 0;
      HashMap<Object,Double> dummy = frequencies[att];
      for (Map.Entry dummy2 : dummy.entrySet()) {
        Object key = dummy2.getKey();
        Double value = (Double) dummy2.getValue();
        info[att] += value / frequTotal * attInfoGains[i];
        i++;
      }
    }
  }

  public Double calculateInfoD(Integer[] instances, Integer label) {
    HashMap<Object,Integer> map = new HashMap();
    Double infoD = new Double(0);
    Double frequTotal = new Double(instances.length);
    for (Integer i : instances) {
      if (!map.containsKey(this.data[i][label])) {
        map.put(this.data[i][label], 0);
      }
      map.put(this.data[i][label], map.get(this.data[i][label]) + 1);
    }
    for (Integer j : map.values()) {
      Double d = new Double(j);
      infoD -= d / frequTotal * log2(d / frequTotal);
    }
    return infoD;
  }

  private void addNode(Node parent, Integer data) {
    Node node = new Node();
    node.setParent(parent);
    node.setData(data);
  }

  public Node getNode() {
    return this.root;
  }

  public static Double log2(Double d) {
    return Math.log(d) / Math.log(2);
  }

  public int numAtt() {
    return this.data[0].length;
  }
}
