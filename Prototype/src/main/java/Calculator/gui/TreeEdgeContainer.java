package Calculator.gui;

import Calculator.tree_elements.PixelInput;
import Calculator.tree_elements.TreeNode;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashSet;

public class TreeEdgeContainer {

    private String edgeName;
    private TreeNode root = null;
    private ArrayList<HashSet<PixelInput>> relInputVariantsForTree;
    private int xInd;
    private GridPane corrPane = null;
    private int corrPaneYInd = 0;
    private GridPane superPane = null;
    private boolean hasSuccNode;
    private ArrayList<HashSet<PixelInput>> succNodeRelInputVariantsForTree;

    public TreeEdgeContainer(String edgeName, int xInd, GridPane corrPane, boolean hasSuccNode, ArrayList<HashSet<PixelInput>> relInpVars, ArrayList<HashSet<PixelInput>> succInpVars){
        this.edgeName = edgeName;
        this.xInd = xInd;
        this.corrPane = corrPane;
        this.hasSuccNode = hasSuccNode;
        this.relInputVariantsForTree = relInpVars;
        this.succNodeRelInputVariantsForTree = succInpVars;
    }
    public TreeEdgeContainer(String edgeName, TreeNode root, int xInd, GridPane corrPane, boolean hasSuccNode, ArrayList<HashSet<PixelInput>> relInpVars, ArrayList<HashSet<PixelInput>> succInpVars){
        this.edgeName = edgeName;
        this.root = root;
        this.xInd = xInd;
        this.corrPane = corrPane;
        this.hasSuccNode = hasSuccNode;
        this.relInputVariantsForTree = relInpVars;
        this.succNodeRelInputVariantsForTree = succInpVars;
    }
    public TreeEdgeContainer(String edgeName, int xInd, GridPane corrPane, int corPY, GridPane superPane, boolean hasSuccNode, ArrayList<HashSet<PixelInput>> relInpVars, ArrayList<HashSet<PixelInput>> succInpVars){
        this.edgeName = edgeName;
        this.xInd = xInd;
        this.corrPane = corrPane;
        this.corrPaneYInd = corPY;
        this.superPane = superPane;
        this.hasSuccNode = hasSuccNode;
        this.relInputVariantsForTree = relInpVars;
        this.succNodeRelInputVariantsForTree = succInpVars;
    }
    public TreeEdgeContainer(String edgeName, TreeNode root, int xInd, GridPane corrPane, int corPY, GridPane superPane, boolean hasSuccNode, ArrayList<HashSet<PixelInput>> relInpVars, ArrayList<HashSet<PixelInput>> succInpVars){
        this.edgeName = edgeName;
        this.root = root;
        this.xInd = xInd;
        this.corrPane = corrPane;
        this.corrPaneYInd = corPY;
        this.superPane = superPane;
        this.hasSuccNode = hasSuccNode;
        this.relInputVariantsForTree = relInpVars;
        this.succNodeRelInputVariantsForTree = succInpVars;
    }
    public String getEdgeName() { return edgeName; }
    public TreeNode getRoot() { return root; }
    public int getxInd() { return xInd; }
    public GridPane getCorrPane() { return corrPane; }
    public ArrayList<HashSet<PixelInput>> getRelInputVariantsForTree() { return relInputVariantsForTree; }
    public ArrayList<HashSet<PixelInput>> getSuccNodeInputVariantsForTree() { return succNodeRelInputVariantsForTree; }

    public boolean hasRoot() { return root!=null; }
    public void buildEdgeIcon(GridPane edgeOuterPane){
        corrPane.add(edgeOuterPane, xInd, 0);
        if (superPane!=null) superPane.add(corrPane, 0, corrPaneYInd);
    }
    public void buildNodeIcon(GridPane nodeOuterPane){
        if (hasSuccNode) corrPane.add(nodeOuterPane,xInd+1,0);
    }
}
