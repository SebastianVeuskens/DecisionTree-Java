import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

//Store and manage the confusion matrix, here called EvalTable (evaluation table)
public class EvalTable {
  private HashMap<Object,HashMap<Object,Integer>> labelTable;

  public EvalTable() {
    this.labelTable = new HashMap();
  }

  //Increase the right field of the confusion matrix by incorporating one instance
  public void add(Object predLable, Object realLabel) {
    if (!this.labelTable.containsKey(predLable)) {
      this.labelTable.put(predLable, new HashMap());
    }
    HashMap<Object,Integer> predLab = this.labelTable.get(predLable);
    if (!predLab.containsKey(realLabel)) {
      predLab.put(realLabel, 0);
    }
    predLab.put(realLabel, predLab.get(realLabel) + 1);
  }

  public String toString() {
    String result = "p|r \n";
    for (Map.Entry dummy : this.labelTable.entrySet()) {
      HashMap<Object,Integer> map = (HashMap) dummy.getValue();
      Object key = dummy.getKey();
      result += (String) key + " | ";
      for (Integer p : map.values()) {
        result += p + " | ";
      }
      result += "\n";
    }
    return result;
  }

  public Double getAccuracy() {
    Double truePred = 0.0;
    Double allPred = 0.0;
    for (Map.Entry dummy : this.labelTable.entrySet()) {
      HashMap<Object,Integer> map = (HashMap) dummy.getValue();
      Object predLabel = dummy.getKey();
      for (Map.Entry dummy2 : map.entrySet()) {
        Object realLabel = dummy2.getKey();
        Integer num = (Integer) dummy2.getValue();
        if (realLabel.equals(predLabel)) {
          truePred += Double.valueOf(num);
        }
        allPred += Double.valueOf(num);
      }
    }
    return truePred / allPred;
  }
}
