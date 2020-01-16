import java.util.LinkedList;

//Provides a functionality to build a decision tree model, each Node contains
//the information needed for delegating a instance of which the label shall
//be predicted
public class Node {
  //Saves in that order: frequLabel, attValOfParSplit, splitCrit
  private LinkedList data = new LinkedList();
  private Node parent;
  private LinkedList<Node> children;

  public void addData(Object data) {
    this.data.add(data);
  }

  public void setParent(Node parent) {
    this.parent = parent;
    if (parent.children == null) {
      parent.children = new LinkedList();
    }
    parent.children.add(this);
  }

  public Node findSubnode(Object attVal) {
    if (this.children == null) {
      return null;
    }
    for (Node subnode : children) {
      //Ist das if hier vielleicht unnötig?
      if (subnode.getAttVal() != null) {
        if (subnode.getAttVal().equals(attVal)) {
          return subnode;
        }
      }
    }
    return null;
  }

  public Object getFrequLabel() {
    return this.data.get(0);
  }

  public Object getAttVal() {
    if (this.data.size() > 1) {
      return this.data.get(1);
    }
    return null;
  }

  public Integer getSplitCrit() {
    if (this.data.size() == 3) {
      return (Integer) this.data.get(2);
    }
    if (!this.hasParent() && this.data.size() == 2) {
      return (Integer) this.data.get(1);
    }
    return null;
  }

  //Util function for display matters
  public String toString(String tabs) {
    String result = tabs;
    result += "label " + this.data.get(0);
    if (this.data.size() > 1) {
      result += " value " + this.data.get(1);
    }
    if (this.data.size() > 2) {
      result += " split " + this.data.get(2);
    }
    result += "\n";
    if (this.children != null) {
      for (Node n : this.children) {
        result += n.toString(tabs + "  ");
      }
    }
    return result;
  }

  public boolean hasData() {
    return this.data != null;
  }

  public boolean hasParent() {
    return this.parent != null;
  }
}
