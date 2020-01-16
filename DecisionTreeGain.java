import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

//Central class that creates a decision tree model and makes predictions on unseen
//instances
public class DecisionTreeGain {

  public Node root;
  public Object[][] data;

  public DecisionTreeGain() {
    root = new Node();
  }

  /**
   *Apply the DecisionTreeGain algorithm on the dataset with default seperator ','
   *Attribute werden nach Split noch weiter als Kriterium in Betracht bezogen
   *
   *@param reader the training set
   *@return the trained decision tree model
   */
  public static DecisionTreeGain decTreeExt(Dataset data, Integer label, Integer[] attributes) {
    DecisionTreeGain model = new DecisionTreeGain();
    label = model.setData(data, attributes, label);
    Integer[] instances = new Integer[data.length()];
    for (int i = 0; i < data.length(); i++) {
      instances[i] = i;
    }
    model.root.addData(model.findFrequLabel(instances, label));
    model.apply(label, instances, model.getNode(), 0);
    System.out.println(model.root.toString(" "));
    return model;
  }

  /**
   *@param instance must store the attributes in the same order like the trained instances
   *contains different attribute values in its array
   */
  public Object findLabel(Object[] instance, Node curNode) {
    Integer splitCrit = curNode.getSplitCrit();
    if (splitCrit != null) {
      Node subnode = curNode.findSubnode(instance[splitCrit]);
      if (subnode != null) {
        return this.findLabel(instance, subnode);
      } else {
        return curNode.getFrequLabel();
      }
    } else {
      return curNode.getFrequLabel();
    }
  }

  /**
   *Apply the n-fold cross Validation method on this decision tree
   *
   *@param n number of folds
   *@return the pairs of predicted and real label of each instance
   */
  public static EvalTable[] crossValidation(int n, Dataset data, Integer label,
  Integer[] attributes) {
     EvalTable[] result = new EvalTable[n];
     if (n < 1 || n > data.length()) {
       throw new RuntimeException("Illegal number of folds: Must be positive" +
       "and not more than number of instances!\n");
     }
     Object[][] croValData = data.getData().clone();
     Integer[] trainInstances;
     Integer[] testInstances;
     int d;
     int e;
     for (int i = 1; i <= n; i++) {
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
           trainInstances[d] = j;
           d++;
         } else {
           testInstances[e] = j;
           e++;
         }
       }
       DecisionTreeGain croValTree = train(trainInstances, data, label, attributes);
       result[i - 1] = croValTree.test(testInstances, label);
     }
     return result;
   }

  public static DecisionTreeGain train(Integer[] instances, Dataset data, Integer label,
  Integer[] attributes) {
    DecisionTreeGain model = new DecisionTreeGain();
    label = model.setData(data, attributes, label);
    model.root.addData(model.findFrequLabel(instances, label));
    model.apply(label, instances, model.getNode(), 0);
    return model;
   }

  //Test all the instances on this model and return the confusion matrix
  private EvalTable test(Integer[] instances, Integer label) {
    EvalTable table = new EvalTable();
    for (int i = 0; i < instances.length; i++) {
      table.add(this.findLabel(this.data[instances[i]], this.root),
      this.data[instances[i]][label]);
    }
    return table;
  }

  //Creates the decision tree by proceeding on the Node object (call by reference)
  //Finds the split for the Node and then uses recursion to determine the split
  //attributes to all subnodes
  private void apply(Integer label, Integer[] instances, Node parent,
  int madeSplits) {
    if (madeSplits + 1 < this.numAtt()) {
      LinkedList evalRes = this.searchCrit(label, instances);
      if (evalRes != null) {
        HashMap<Object,HashMap> attMap = (HashMap) evalRes.removeLast();
        Integer splitCrit = (Integer) evalRes.removeLast();
        parent.addData(splitCrit);
        //value goes through all specification that the specified attribute can take
        for (Map.Entry dummy : attMap.entrySet()) {
          Object attVal = dummy.getKey();
          HashMap<Object,ArrayList> value = (HashMap) dummy.getValue();
          int numInstances = 0;
          for (ArrayList<Double> sameLabel : value.values()) {
            numInstances += sameLabel.size();
          }
          Integer[] insVal = new Integer[numInstances];
          Integer i = 0;
          for (ArrayList<Double> sameLabel : value.values()) {
            for (Double instance : sameLabel) {
              insVal[i] = instance.intValue();
              i++;
            }
          }
          Node node = new Node();
          node.addData(this.findFrequLabel(insVal, label));
          node.addData(attVal);
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
  private LinkedList searchCrit(Integer label, Integer[] instances) {
    Double threshold = 0.0;
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
    Double[] gainRatio = new Double[this.numAtt()];
    Double infoD = this.calculateInfoD(instances, label);
    this.calulateGainRatio(gainRatio, info, instances.length, frequencies, label, infoD);

    //Find the attribute that provides the highest infoGain if splitted there
    LinkedList result = new LinkedList();
    Integer splitCrit = -1;
    Double maxGain = Double.valueOf(0);
    for (int i = 0; i < gainRatio.length; i++) {
      if (i != label && gainRatio[i] > threshold && gainRatio[i] > maxGain) {
        splitCrit = i;
        maxGain = gainRatio[i];
      }
    }
    if (splitCrit == -1) {
      return null;
    }
    result.add(splitCrit);
    result.add(eachAtt[splitCrit].clone());
    return result;
  }

  //Initialise this decision tree with all the data with the specified attributes
  private Integer setData(Dataset data, Integer[] attributes, Integer label) {
    Object[][] essentialData = new Object[data.length()][attributes.length];
    int i = 0;
    for (Integer att : attributes) {
      for (int ins = 0; ins < data.length(); ins++) {
        essentialData[ins][i] = data.getData()[ins][att];
      }
      if (att == label) {
        label = i;
      }
      i++;
    }
    this.data = essentialData;
    if (this.data.length == 0) {
      throw new RuntimeException("Initialisation exception: No instances given!");
    }
    return label;
  }

 //Initialise the data for the split search procedure
  private void initialiseDatastructure(HashMap<Object,HashMap<Object,ArrayList>>[]
  eachAtt, Integer label, Integer[] instances) {
    for (int att = 0; att < eachAtt.length; att++) {
      if (att != label && instances != null) {
        for (Integer ins : instances) {
          if (!eachAtt[att].containsKey(this.data[ins][att])) {
            eachAtt[att].put(data[ins][att], new HashMap<Object,ArrayList>());
          }
          //Contains all instances with the same values of attribute but with
          //different labels
          HashMap<Object,ArrayList> diffLab = eachAtt[att].get(data[ins][att]);
          if (!diffLab.containsKey(data[ins][label])) {
            diffLab.put(data[ins][label], new ArrayList<Double>());
          }
          diffLab.get(data[ins][label]).add(new Double(ins));
        }
      }
    }
  }

  private void calculateFrequencies(HashMap<Object,Double>[] frequencies,
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

  //Calculate the info gain that would result of the specified attribute split
  private void calculateInfo(Double[] info, HashMap<Object,HashMap<Object,ArrayList>>[]
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

  private void calulateGainRatio(Double[] gainRatio, Double[] info, int length,
  HashMap<Object,Double>[] frequencies, Integer label, Double infoD) {
    for (int att = 0; att < gainRatio.length; att++) {
      if (att != label) {
        Double splitInfo = new Double(0);
        for (Double d : frequencies[att].values()) {
          Double p = d / (new Double(length));
          splitInfo -= p * log2(p);
        }
        //Necessary since otherwise splitInfo is calculated as Infinity due to
        //lacks in computing the logarithm for zero (splitInfo converges to zero
        //for p approaching to zero)
        if (splitInfo != 0.0) {
          gainRatio[att] = (infoD - info[att]) / splitInfo;
        } else {
          gainRatio[att] = 0.0;
        }
      }
    }
  }

  //InfoD refers to the Info without conducting an attribute split
  private Double calculateInfoD(Integer[] instances, Integer label) {
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

  //Find the common label of the instances
  //Is used to implement the majority vote, the most frequent label is stored in a Node
  private Object findFrequLabel(Integer[] instances, Integer label) {
    if (instances.length == 0) {
      throw new RuntimeException("warum hier keine Instanzen angegeben?");
    }
    Object frequLabel = null;
    HashMap<Object,Integer> allLabels = new HashMap();
    Integer count = 0;
    for (Integer ins : instances) {
      if (!allLabels.containsKey(this.data[ins][label])) {
        allLabels.put(this.data[ins][label],0);
      }
      allLabels.put(this.data[ins][label], allLabels.get(this.data[ins][label]) + 1);
    }
    for (Map.Entry dummy : allLabels.entrySet()) {
      Integer labelCount = (Integer) dummy.getValue();
      Object CurLabel = dummy.getKey();
      if (labelCount > count) {
        count = labelCount;
        frequLabel = CurLabel;
      }
    }
    return frequLabel;
  }

  private Node getNode() {
    return this.root;
  }

  public static Double log2(Double d) {
    return Math.log(d) / Math.log(2);
  }

  private int numAtt() {
    return this.data[0].length;
  }
}
