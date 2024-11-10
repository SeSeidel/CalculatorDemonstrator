package Calculator.tree_elements;

import Calculator.general.EqualityTupel;
import Calculator.net_elements.Neuron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class TreePath {
    private TreeEdge[] edges;
    private int decLevelNum;

    private HashSet<EqualityTupel> connEquTupels = new HashSet<>();         // Für Prüfung zur Zyklenprefention nach Alg Ausarbeitung, Punkt 4.

    public TreePath (TreeEdge[] pathEdges, int pathDecLevelNum) {
        edges = pathEdges;
        decLevelNum = pathDecLevelNum;
    }
    public void deleteEdgesWithoutInputs(){
        ArrayList<TreeEdge> newList = new ArrayList();
        for (TreeEdge edge: edges) if (!edge.getCorrespInputs().isEmpty()) newList.add(edge);
        edges = newList.toArray(new TreeEdge[newList.size()]);
    }

    public TreeEdge[] getEdges() { return edges; }

    public int getDecLevelNum() { return decLevelNum; }

    public void addEquTupel(EqualityTupel tupel) { connEquTupels.add(tupel); }
    public void removeEquTupel(EqualityTupel tupel) { connEquTupels.remove(tupel); }
    public HashSet<EqualityTupel> getConnEquTupel() { return connEquTupels; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreePath treepath = (TreePath) o;
        return this.edges.equals(treepath.edges);
    }
}
