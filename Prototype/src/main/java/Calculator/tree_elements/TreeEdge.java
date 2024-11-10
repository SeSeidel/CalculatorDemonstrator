package Calculator.tree_elements;

import Calculator.general.EquEdgesSet;
import Calculator.general.MapperService;
import Calculator.general.Merger;
import Calculator.net_elements.NeuralNet;
import Calculator.net_elements.Neuron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class TreeEdge {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);
    private Neuron[] sourceNeurons;                                 // Die Menge NE gem. Unterkapitel 4.2.5.
    private int sourceNeuronFilterNum;                              // Die Filternummer zu NE
    private int sourceNeuronNum;                                    // Der Index von NE im zugehörigen Filter
    private ArrayList<TreePath> paths = new ArrayList<>();          // Die Menge der Pfade path gem. Unterkapitel 4.2.5
    private HashSet<Integer> correspInputs = new HashSet<>();       // Die Menge IN gem. Unterkapitel 4.2.5 -> Füŕ die im Prototypen berücksichtigten Sonderfall der Gleichwertigkeit von Kanten. -> bei paarweiser Gleichheit auf Liste von HashSets zu erweitern!
    // private HashSet<Neuron> RelPredNeurons = new HashSet<>();    // Die Menge PE gem. Unterkapitel 4.2.5 -> Bei Verwendung paarweiser Gleichwertigkeit ggf. ändern (Menge von Mengen). Aktuell nicht benötigt!
    private ArrayList<HashSet<PixelInput>> relInputVariantsForTree = new ArrayList<>();

    private TreeNode successorNodeInTree = null;
    private TreeNode combinedSubTree = null;
    private HashSet<TreeEdge> subordinateEdges = new HashSet<>();   // Liste potentiell untergeordneter Kanten für Abgleich bei der Zuweisung untergeordneter Pfade.
    private int decNum;

    private double[] correspFirstLayerFirstFiltInpSums = null;  // Die Aktivierungssummen, die resultierend aus der Menge IN im ersten Filter der ersten verdeckten Schicht eingehen.

    public TreeEdge(Neuron[] edgeSourceNeurons, int filternum, int neuronNum, int decNum) {
        sourceNeurons = edgeSourceNeurons;
        this.sourceNeuronFilterNum = filternum;
        this.sourceNeuronNum = neuronNum;
        this.decNum = decNum;
        relInputVariantsForTree.add(new HashSet<>());
    }

    public TreePath getPath(int pathNum) { return paths.get(pathNum); }
    public ArrayList<TreePath> getAllPaths() { return paths; }
    public int getNumOfPaths() { return paths.size(); }
    public Neuron[] getSourceNeurons() { return sourceNeurons; }

    public void addPathWithInp(TreePath pathToAdd, double[] actInput) {      // Hier Prüfen, ob die einzufügenden Pfade diese Kante bereits enthalten -> Wäre unzulässige Schleife!
        if (paths.isEmpty()) {
            correspInputs.clear();                      // Wird der erste Pfad hinzugefügt, ergeben sich die relevanten Eingabeindizes aus dem Pfad.
            relInputVariantsForTree.get(0).clear();     // Entsprechend auch die erste kombination von Pixeleingaben.
        }
        if (!paths.contains(pathToAdd)){
            TreeEdge[] pathEdges = pathToAdd.getEdges();
            for (TreeEdge i: pathEdges) {
                Iterator<TreeEdge> actInternalSubEdgesIterator = i.getSubordinateEdges().iterator();
                actInternalSubEdgesIterator.forEachRemaining(j -> checkUpSubEdge(j));
                checkUpSubEdge(i);
                for (Integer d: i.getCorrespInputs()) { correspInputs.add(d); }// Die relevanten Inputs der Kante i zu den relevanten Inputs dieser Kante hinzufügen.
            }
            // for (Neuron n: i.getSourceNeurons()) { RelPredNeurons.add(n); } // Die Ursprungsneuronen der Kante i zu den relevanten Vorgängerneuronen dieser Kante hinzufügen. // Aktuell nicht benötigt!
            paths.add(pathToAdd);
        }
    }
    public void addPathWithoutInp(TreePath pathToAdd){
        if (!paths.contains(pathToAdd)) paths.add(pathToAdd);
    }

    private void checkUpSubEdge (TreeEdge edge) { // Hilfsmethode für die interne Prüfung unf Übertragung untergeordneter Kanten zu "addPath".
        if (edge.equals(this)) {
            logger.error("Die Kante " + toString() + " ist bereits in einem Pfad enthalten, darf sich aber nicht selber enthalten!");
            System.exit(1);
        }
        subordinateEdges.add(edge);
    }

    public void addCorrespInput (Integer indexToAdd) { correspInputs.add(indexToAdd); }
    public void addAllCorrespInputs (HashSet<Integer> inputsToAdd) { correspInputs.addAll(inputsToAdd); }
    public HashSet<Integer> getCorrespInputs() { return correspInputs; }

    // public HashSet<Neuron> getRelPredNeurons() { return RelPredNeurons; }    // Aktuell nicht benötigt!

    public HashSet<TreeEdge> getSubordinateEdges() { return subordinateEdges; }

    public TreeNode getSuccessorNodeInTree() { return successorNodeInTree; }
    public boolean hasSucessorNodeInTree() { return successorNodeInTree!=null; }

    public TreeNode getCombinedSubTreeRoot() { return combinedSubTree; }
    public boolean hasCombinedSubTreeRoot() { return !(combinedSubTree==null); }

    public void calcCorrespFirstLayerInpSums(NeuralNet net) { correspFirstLayerFirstFiltInpSums = net.calculateEdgeFirstLayerInpSums(correspInputs); }

    public int getSourceNeuronFilterNum(){ return sourceNeuronFilterNum; }
    public int getSourceNeuronNum(){ return sourceNeuronNum; }

    public ArrayList<HashSet<PixelInput>> getRelInputVariantsForTree() { return relInputVariantsForTree; }
    public void addRelInputVariant(HashSet<PixelInput> inpVariant) { if (!relInputVariantsForTree.contains(inpVariant)) relInputVariantsForTree.add(inpVariant); }

    public TreeEdge SemiShallowCopyForTreeNodes(TreeNode succNode){   // Nur vollwertige Kopie der Pfadliste benötigt, da nur hier Änderungen vorgenommen werden.
        TreeEdge out = new TreeEdge(this.sourceNeurons, this.sourceNeuronFilterNum, this.sourceNeuronNum, this.decNum);
        out.paths = new ArrayList<>(this.paths);        // direkte Attributübernahme statt hinzufügen der Pfad, da schneller!
        out.correspInputs = this.correspInputs;         // direkte Attributübernahme statt hinzufügen der Pfad, da schneller!
        out.subordinateEdges = this.subordinateEdges;   // direkte Attributübernahme statt hinzufügen der Pfad, da schneller!
        out.correspFirstLayerFirstFiltInpSums = this.correspFirstLayerFirstFiltInpSums;
        out.successorNodeInTree = succNode;
        out.relInputVariantsForTree = relInputVariantsForTree;
        return out;
    }

    public void createSubTreeForEdge(EquEdgesSet correspEquEdgesSet){
        HashSet<TreePath> mergerPaths = new HashSet<>(paths);
        combinedSubTree = new Merger(mergerPaths, correspEquEdgesSet).buildNewSubTree();
    }

    public void combineRelInputVariantsForTree(){       // Abgleich aller zulässigen relevanten Eingabekombinationen für die Kante aus allen Unterbäumen.
        if (getNumOfPaths() > 0){
            relInputVariantsForTree.clear();
            ArrayList<TreeEdge> subTreeEdgeList = combinedSubTree.getAllFollowUpEdges();
            for (TreeEdge subEdge: subTreeEdgeList) subEdge.combineRelInputVariantsForTree();
            createValidSubTreeInputVariants(combinedSubTree, new ArrayList<>());
        }
    }
    private void createValidSubTreeInputVariants(TreeNode nextNode, ArrayList<HashSet<PixelInput>> intermedCombList){
        for (TreeEdge succEdge: nextNode.getSucEdges()){
            ArrayList<HashSet<PixelInput>> intermedCombList2 = new ArrayList<>();
            if (intermedCombList.isEmpty()) intermedCombList2.addAll(succEdge.relInputVariantsForTree);
            else {
                for (HashSet<PixelInput> edgeComb: succEdge.relInputVariantsForTree){
                    for (HashSet<PixelInput> inputComb: intermedCombList){
                        HashSet<PixelInput> newComb = new HashSet<>();
                        newComb.addAll(inputComb); newComb.addAll(edgeComb);
                        intermedCombList2.add(newComb);
                    }}
            }   // succEdge hatt immer einen successorNode, daher dessen Nachfolgekante abfragen.
            if (succEdge.getSuccessorNodeInTree().hasSuccEdges()){
                // succEdge.getSuccessorNodeInTree().calcRelInputVariantsForTree(nextNode.getRelInputVariantsForTree(), intermedCombList2);
                createValidSubTreeInputVariants(succEdge.successorNodeInTree, intermedCombList2);
            }
            else relInputVariantsForTree.addAll(intermedCombList2);
        }
    }

    @Override
    public String toString() {
        StringBuilder NeuronString = new StringBuilder();
        for (Neuron i: sourceNeurons) { NeuronString.append(i.getNeuronButtonName()); NeuronString.append(" | "); }
        NeuronString.delete(NeuronString.length()-3, NeuronString.length()-1);
        return "E[" + decNum + "|" + NeuronString.toString() + "]";
    }

    public boolean equalEdge(TreeEdge other) {                       //gemäß definierter abgeschwächter Gleichwertigkeit von Kanten, kein normales equals, da Netz auch benötigt wird
        if (!Arrays.equals(sourceNeurons, other.sourceNeurons)) return false;               // prüfen ob alle Ableitungsneuronen für beide gleich sind - Reihenfolge wird nicht erzwungen da Menge!
        return equalFirstLayerActivations(other);                                           // Prüfen ab in der ersten Schicht die gleichen Aktivierungen erzeugt werden!
    }
    private boolean equalFirstLayerActivations(TreeEdge other){                                         // Hilfsmethode für Vergleich der zugehörigen gespeicherten Aktivierungen der ersten Schicht.
        if (!Arrays.equals(correspFirstLayerFirstFiltInpSums, other.correspFirstLayerFirstFiltInpSums)) return false;
        else return true;
    }
}
