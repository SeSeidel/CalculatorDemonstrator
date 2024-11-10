package Calculator.tree_elements;

import Calculator.gui.TreeEdgeContainer;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;
import java.util.HashSet;

public class TreeNode {

    private ArrayList<TreeEdge> sucessorEdges = new ArrayList<>();
    private ArrayList<HashSet<PixelInput>> relInputVariantsForTree = new ArrayList<>();

    public TreeNode(){ relInputVariantsForTree.add(new HashSet<>()); }

    public void addSingleEdge(TreeEdge edge){ sucessorEdges.add(edge); }
    public ArrayList<TreeEdge> getSucEdges() { return sucessorEdges; }
    public boolean hasSuccEdges() { return !sucessorEdges.isEmpty(); }
    public boolean hasEdge(TreeEdge edge) {
        for (TreeEdge e: sucessorEdges) if (e.equalEdge(edge)) return true;
        return false;
    }
    public ArrayList<HashSet<PixelInput>> getRelInputVariantsForTree() { return relInputVariantsForTree; }

    public ArrayList<TreeEdge> getAllFollowUpEdges(){
        ArrayList<TreeEdge> out = new ArrayList<>();
        if (hasSuccEdges()){                            // Ordnet bei Verzweigung Folgepfade mit allen Knoten hintereinander an.
            for (TreeEdge edge: sucessorEdges) {        // Die Anordnung entspricht jener der Container aus getAllGuiContainer.
                out.add(edge);
                out.addAll(edge.getSuccessorNodeInTree().getAllFollowUpEdges());
            }
        }
        return out;
    }

    public ArrayList<TreeEdgeContainer> getAllGuiContainer(int xPos, GridPane predGridPane){
        ArrayList<TreeEdgeContainer> out = new ArrayList<>();
        if (sucessorEdges.size() > 1){
            int yPos = 0;
            GridPane preAnchor = new GridPane();
            for (TreeEdge edge: sucessorEdges){
                GridPane newPane = new GridPane();
                newPane.setPadding(new Insets(5,5,5,5));
                if (edge.hasCombinedSubTreeRoot()) out.add(new TreeEdgeContainer(edge.toString(), edge.getCombinedSubTreeRoot(), 0, newPane, yPos, preAnchor, edge.getSuccessorNodeInTree().hasSuccEdges(), edge.getRelInputVariantsForTree(), relInputVariantsForTree));
                else out.add(new TreeEdgeContainer(edge.toString(), 0, newPane, yPos, preAnchor, edge.getSuccessorNodeInTree().hasSuccEdges(), edge.getRelInputVariantsForTree(), relInputVariantsForTree));
                yPos = yPos+1;
                if (edge.hasSucessorNodeInTree()) out.addAll(edge.getSuccessorNodeInTree().getAllGuiContainer(2, newPane));
            }
            predGridPane.add(preAnchor, xPos, 0);
        }
        else if (sucessorEdges.size() > 0){
            TreeEdge edge = sucessorEdges.get(0);
            if (edge.hasCombinedSubTreeRoot()) out.add(new TreeEdgeContainer(edge.toString(), edge.getCombinedSubTreeRoot(), xPos, predGridPane, edge.getSuccessorNodeInTree().hasSuccEdges(), edge.getRelInputVariantsForTree(), relInputVariantsForTree));
            else out.add(new TreeEdgeContainer(edge.toString(), xPos, predGridPane, edge.getSuccessorNodeInTree().hasSuccEdges(), edge.getRelInputVariantsForTree(), relInputVariantsForTree));
            if (edge.hasSucessorNodeInTree()) out.addAll(edge.getSuccessorNodeInTree().getAllGuiContainer(xPos+2, predGridPane));
        }
        return out;
    }

    public void calcRelInputVariantsForTree(ArrayList<HashSet<PixelInput>> predNodeList, ArrayList<HashSet<PixelInput>> predEdgeList){
        relInputVariantsForTree.clear();
        for (HashSet<PixelInput> predNodeInp: predNodeList){
            for (HashSet<PixelInput> predEdgeInp: predEdgeList){
                HashSet<PixelInput> newInpSet = new HashSet<>();
                newInpSet.addAll(predNodeInp); newInpSet.addAll(predEdgeInp);
                relInputVariantsForTree.add(newInpSet);
            }}
        if (relInputVariantsForTree.isEmpty()) relInputVariantsForTree.add(new HashSet<>());
    }
}
