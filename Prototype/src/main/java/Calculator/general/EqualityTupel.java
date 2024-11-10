package Calculator.general;

import Calculator.tree_elements.TreeEdge;
import Calculator.tree_elements.TreePath;

import java.util.HashSet;

public class EqualityTupel {

    private TreeEdge revEdge;
    private HashSet<TreePath> connPaths = new HashSet<>();

    public EqualityTupel (TreeEdge revEdge){
        this.revEdge = revEdge;
    }
    public EqualityTupel (TreeEdge revEdge, TreePath firstPath){
        this.revEdge = revEdge;
        this.connPaths.add(firstPath);
        firstPath.addEquTupel(this);
    }

    public TreeEdge getRevEdge() { return revEdge; }

    public boolean addNewPath(TreeEdge edgeToComp, TreePath pathToAdd){
        boolean toAdd = revEdge.equalEdge(edgeToComp);
        if (toAdd) {
            connPaths.add(pathToAdd);
            pathToAdd.addEquTupel(this);
        }
        return toAdd;
    }

    public int numOfPaths(){ return connPaths.size(); }
    public void removePath(TreePath path){
        path.removeEquTupel(this);
        connPaths.remove(path);
    }
    public void clearPaths(){
        for (TreePath p: connPaths) p.removeEquTupel(this);
        connPaths.clear();
    }
    public void switchPaths(HashSet<TreePath> newPaths){
        clearPaths();
        for (TreePath p: newPaths){
            connPaths.add(p);
            p.addEquTupel(this);
        }
    }
    public HashSet<TreePath> getConnPaths(){ return connPaths; }

    public EqualityTupel copyMe() {
        EqualityTupel out = new EqualityTupel(revEdge);
        out.switchPaths(connPaths);
        return out;
    }

    @Override
    public boolean equals(Object o) {       //Für saubere Datanstruktur ggf. noch layerNum einarbeiten - Sollte jedoch so gut wie nie nötig sein wenn die Gewichte UND der Bias abgefragt werden! (Lediglich für Pooling, daher Filternummer)
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EqualityTupel tupel = (EqualityTupel) o;
        if (!tupel.getRevEdge().equalEdge(this.getRevEdge())) return false;
        return tupel.getConnPaths().equals(this.getConnPaths());
    }
}
