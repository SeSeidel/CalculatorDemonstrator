package Calculator.net_elements;

import Calculator.general.*;
import Calculator.element_types.LayerType;
import Calculator.gui.MainMenu;
import Calculator.net_elements.activation_elements.ActivationMessage;
import Calculator.tree_elements.TreeDecision;
import Calculator.tree_elements.TreeEdge;
import Calculator.tree_elements.TreeNode;
import Calculator.tree_elements.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Klasse zur Nachbildung eines künstlichen Neuronalen Netztes mit Schichten der generischen Klasse NeuralLayer.
 */
public class NeuralNet {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);
    private final List<NeuralLayer> neuralLayers;
    private ArrayList<TreeDecision> convertedDecisionsList = new ArrayList<>();
    private HashSet<TreePath> actEdgeSubPaths = new HashSet<>();
    private EquEdgesSet actEquEdgesSet;
    private TreeNode rootNodeDecTree;

    private double[] actInput = new double[0];

    /**
     * Konstruktor des Neuronalen Netztes
     *
     * @param neuralLayers Liste der Schichten vom Typ NeuralLayer des Netztes
     */
    public NeuralNet(List<NeuralLayer> neuralLayers) {
        this.neuralLayers = neuralLayers;
    }

    /**
     * @return Liste der Schichten vom Typ NeuralLayer des Netztes
     */
    public List<NeuralLayer> getNeuralLayers() {
        return neuralLayers;
    }

    public int getInputSize() { return neuralLayers.get(1).getFilterNeurons(0)[0].getIncomingWeights().length; }

    public double getActInputValue(int index) {
        if (actInput.length > 0) return actInput[index];
        else return 0.0;
    }
    public Boolean inputAvailable() { return actInput.length != 0; }
    public TreeDecision getDecTreeByNum(int num) { return convertedDecisionsList.get(num); }
    public TreeNode getRootNodeDecTree() { return rootNodeDecTree; }

    /**
     * @param layerNum Schicht in der das Neuron mit den gesuchten Ausgangsverbindungen liegt
     * @param neuronNum Zielneuron in der angegebenen Schicht mit den gesuchten Ausgangsverbindungen
     * @param filterNum Filter des Zielneurons in der angegebenen Schicht.
     * @return Array mit allen Gewichten der ausgehenden Verbindungen des Zielneurons
     */
    public double[] outgoingWeights(int layerNum, int neuronNum, int filterNum) {
        try {
            if (layerNum == neuralLayers.size()-1 ) { throw new wrongLayerNum1Exception("The layer " + layerNum + " in this net has no successor layer to be connected to!"); }
            if (layerNum > neuralLayers.size()-1 ) { throw new wrongLayerNum2Exception("The layer " + layerNum + " in this net does not exist!"); }
            if (MainMenu.getActualNet().getNeuralLayers().get(layerNum).getLayerType().equals(LayerType.CORE_FLATTEN)){
                int filterLenght = neuralLayers.get(layerNum-1).getFilterNeurons(0).length; //Anm.: setzt voraus, dass alle Filter gleiche Länge haben (ist bei TF akt. so) -> ggf. noch erweitern
                return neuralLayers.get(layerNum+1).getWeightsFromPredecessorNeurons(layerNum, neuronNum+(filterNum*filterLenght), 0);
            }
            else{ return neuralLayers.get(layerNum+1).getWeightsFromPredecessorNeurons(layerNum, neuronNum, filterNum); }
        }
        catch(wrongLayerNum1Exception e) {System.out.println(e.getMessage()); return null;}
        catch(wrongLayerNum2Exception e) {System.out.println(e.getMessage()); return null;}
    }

    /**
     * Berechnet die Abschätzungen für die minimal und maximal möglichen Eingangsaktivierungen der Filter aller Schichten.
     *
     * @param minInput
     * @param maxInput
     */
    public void calculateAllActivationFactors(double minInput, double maxInput) {
        if (minInput > maxInput){
            logger.error("Der Minimum Input ist mit " + minInput + " groesser als der Maximum Input mit " + maxInput + "!");
            System.exit(1);
        }
        else {
            neuralLayers.get(0).initMinMaxInputActivations(minInput, maxInput, this);                       // Initialisiert Eingabevektoren für die Eingabschicht jeweils mit den Minimal- und Maximalaktivierungen
            ArrayList<double[]> minPredFactorActivations = neuralLayers.get(0).getMinFactorActivations();        // Setzt die Minimaleingaben der Eingabeschicht als erste Abschätzung der Minimalaktivierungen.
            ArrayList<double[]> maxPredFactorActivations = neuralLayers.get(0).getMaxFactorActivations();        // Setzt die Maximaleingaben der Eingabeschicht als erste Abschätzung der Maximalaktivierungen.
            NeuralLayer actLayer;
            for (int i = 1; i < neuralLayers.size() - 1; i++) {                                                  // Für alle Folgeschichten:
                actLayer = neuralLayers.get(i);
                actLayer.calcLayerActivationFactors(minPredFactorActivations, maxPredFactorActivations);         // Berechnet für die aktuelle Schicht die jeweils eigenen Abschätzungen für die möglichen eingehenden Minimal- und Maximalaktivierungen.
                actLayer.activateLayersFactorActivations(minPredFactorActivations, maxPredFactorActivations);    // Verrechnet die Abschätzungen für die eingehenden Minimal- und Maximalaktivierungen mit der Aktivierungsfunktion der Schicht.
                minPredFactorActivations = actLayer.getMinFactorActivations();                                   // Setzt die neuen Abschätzung der eingehenden Minimalaktivierungen für die Folgeschicht.
                maxPredFactorActivations = actLayer.getMaxFactorActivations();                                   // Setzt die neuen Abschätzung der eingehenden Maximalaktivierungen für die Folgeschicht.
            }
        }
    }

    /**
     * Erfasst, gemessen an einem gegebenen Grenzwert, welche Verbindungen bei Berechnungen beachtet werden sollen.
     *
     * @param treshold Der zu berücksichtigende Grenzwert.
     */
    public void setRelevantNetWeights(double treshold) {
        NeuralLayer lastLayer = neuralLayers.get(neuralLayers.size()-1);
        if (lastLayer.getNumOfFilters() == 1) {                                                                // ggf. Try-Catch-Block verwenden
            lastLayer.setRelevantSoftMaxLayerWeights(treshold, neuralLayers.size()-2);
        }
        else { logger.error("Die Daten scheinen korrupt zu sein -> mehr als ein Filter in Ausgabeschicht"); }
        for (int i = neuralLayers.size()-2; i > 0; i--) {                                                  // Rückrechnung von unten nach oben
            neuralLayers.get(i).setRelevantLayerWeights(treshold, this, i, getPredMaxFactorActivations(i), getPredMinFactorActivations(i));
        }
    }
    /**
     * Hilfsmethode für "setRelevantWeights"
     *
     * @param layerNum Für die wievielte Schicht im Netz werden die Max-Aktivierungen ihres Vorgängers gesucht.
     */
    private ArrayList<double[]> getPredMaxFactorActivations(int layerNum) {
        ArrayList<double[]> maxActivations = null;
        if (layerNum > 0) {
            if (neuralLayers.get(layerNum-1).getLayerType() == LayerType.CORE_FLATTEN) {
                maxActivations = neuralLayers.get(layerNum-2).getMaxFactorActivations();
                int filterLenght = maxActivations.get(0).length;                            // setzt gleiche Länge aller Filter einer Schicht vorraus
                double[] flattenedArray = new double[maxActivations.size()*filterLenght];
                for (int j=0; j<maxActivations.size(); j++ ) {
                    for (int i=0; i<filterLenght; i++){
                        flattenedArray[i + j*filterLenght] = maxActivations.get(j)[i];
                    }}
                maxActivations = new ArrayList<>(); maxActivations.add(flattenedArray);
            }
            else {
                maxActivations = neuralLayers.get(layerNum - 1).getMaxFactorActivations();
                int thisLayersFilterNum = neuralLayers.get(layerNum).getNumOfFilters();
                if (maxActivations.size() < thisLayersFilterNum) {                          // Von Schichten mit einem Filter auf Schichten mit n filtern, z.B. bei Eingabe.
                    for (int i = 1; i < thisLayersFilterNum; i++) {
                        maxActivations.add(maxActivations.get(0));
                    }}}}
        else { logger.error("Die Schicht " + neuralLayers.get(layerNum).getName() + " hat keine Vorgängerschicht"); }
        return maxActivations;
    }
    private ArrayList<double[]> getPredMinFactorActivations(int layerNum) {
        ArrayList<double[]> minActivations = null;
        if (layerNum > 0) {
            if (neuralLayers.get(layerNum-1).getLayerType() == LayerType.CORE_FLATTEN) {
                minActivations = neuralLayers.get(layerNum-2).getMinFactorActivations();
                int filterLenght = minActivations.get(0).length;                            // setzt gleiche Länge aller Filter einer Schicht vorraus
                double[] flattenedArray = new double[minActivations.size()*filterLenght];
                for (int j=0; j<minActivations.size(); j++ ) {
                    for (int i=0; i<filterLenght; i++){
                        flattenedArray[i + j*filterLenght] = minActivations.get(j)[i];
                    }}
                minActivations = new ArrayList<>(); minActivations.add(flattenedArray);
            }
            else {
                minActivations = neuralLayers.get(layerNum - 1).getMinFactorActivations();
                int thisLayersFilterNum = neuralLayers.get(layerNum).getNumOfFilters();
                if (minActivations.size() < thisLayersFilterNum) {                          // Von Schichten mit einem Filter auf Schichten mit n filtern, z.B. bei Eingabe.
                    for (int i = 1; i < thisLayersFilterNum; i++) {
                        minActivations.add(minActivations.get(0));
                    }}}}
        else { logger.error("Die Schicht " + neuralLayers.get(layerNum).getName() + " hat keine Vorgängerschicht"); }
        return minActivations;
    }

    public void calculateNetActivations(double[] inputVector) {
        if (inputVector.length == getInputSize()) {                 // Zweite, zusätzliche Dimensionsabfrage, hier des eindimensionalen Eingabevektors.
            actInput = inputVector;
            ArrayList<ActivationMessage[]> inputForFilters = new ArrayList<>();
            inputForFilters.add(transformInputVectorToAktivationMessageVector(actInput));
            for (int i = 1; i < neuralLayers.size(); i++) {
                inputForFilters = neuralLayers.get(i).calcLayerActivations(inputForFilters);
            }
        }
        else {
            logger.error("Die Dimensionen der Eingabe sind nicht für das geladenen Netz geeignet!");
        }
    }
    private ActivationMessage[] transformInputVectorToAktivationMessageVector(double[] inputVector) {
        ActivationMessage[] out = new ActivationMessage[inputVector.length];
        for (int i = 0; i < inputVector.length; i++) {
            HashSet<Integer> setForInput = new HashSet<>();
            setForInput.add(i);
            out[i] = new ActivationMessage(inputVector[i], setForInput, setForInput);       // Eingehendes Pixel ist ggf aktivierungsrelevante Verbindung und auch einzige Verbindung!
        }
        return out;
    }

    public double[] calculateEdgeFirstLayerInpSums(HashSet<Integer> correspInput) {
        double[] inputActsForFilters = transformEdgeCorresInputToActVector(correspInput); // Kann nur einen Filter haben weil von Eingabeschicht zu erster Scicht!
        double[] out = neuralLayers.get(1).calcLayerFirstFilterInputSum(inputActsForFilters);
        return out;
    }
    private double[] transformEdgeCorresInputToActVector(HashSet<Integer> correspInput) {  // Erzeugt einen Eingabevektor, der nur die Eingangsaktivierungen zu correspInput enthält.
        double[] out = new double[actInput.length];
        for (int i = 0; i < actInput.length; i++) {
            if (correspInput.contains(i)) out[i] = actInput[i];
            else out[i] = 0;
        }
        return out;
    }

    public void resetNetActivations() {
        double[] resetActivations = new double[getInputSize()]; Arrays.fill(resetActivations, 0.0);
        calculateNetActivations(resetActivations);
    }

    public Boolean convertAndSaveCurrentDecision(double activationThreshold) {        // Konvertiert die aktuell berechnete Aktivierungskaskade im Netz und speichert Sie in der Entscheidungsliste.
        Converter decisionConverter = new Converter(this, actInput, activationThreshold, convertedDecisionsList.size());   // Erstellt einen neuen Converter, der für die aktuelle Entscheidung einen Pfad berechnet.
        boolean toBeSaved = true;
        for (TreeDecision actDec: convertedDecisionsList){
            if (Arrays.equals(actDec.getDecInput(), actInput)){                     // Nur Aufnahme eines Pfades je Eingabe, keine Pfad-Dopplung einer Eingabe z.B. durch mehrere Sensitivitäten.
                toBeSaved = false;
                logger.info("Der Pfad zu dieser Entscheidung wurde bereits erfasst!");
                break;
            }
        }
        if (toBeSaved) convertedDecisionsList.add(decisionConverter.convertNetDecision(neuralLayers.size()-1));
        return toBeSaved;
    }

    public void combineConvertedDecisions(){
        actEdgeSubPaths.clear();
        for (TreeDecision dec: convertedDecisionsList) actEdgeSubPaths.addAll(dec.getOuterEdge().getAllPaths());
        actEquEdgesSet = new EquEdgesSet(actEdgeSubPaths, this);
        rootNodeDecTree = new Merger(new HashSet<>(actEdgeSubPaths), actEquEdgesSet).buildNewSubTree(); // Gemeinsame Verarbeitung der Pfade in den äusseren Kanten.
        HashSet<TreeNode> nextLvlInput = new HashSet<>(); nextLvlInput.add(rootNodeDecTree);
        while(actEdgeSubPaths.size()!=0) nextLvlInput = processNextDecLvl(nextLvlInput);                //
        ArrayList<TreeEdge> treeEdgeList = rootNodeDecTree.getAllFollowUpEdges();
        for (TreeEdge subEdge: treeEdgeList) subEdge.combineRelInputVariantsForTree();
    }
    private HashSet<TreeNode> processNextDecLvl(HashSet<TreeNode> actDecLvlRootNodes){
        nextDecLvlPathList(actEquEdgesSet);                             // Liste aller Pfade für aktuellen DecLvl berechnen
        if (actEdgeSubPaths.size()==0){                                 // Ggf. noch mal andere Abbruchbedingung!
            return new HashSet<>();
        }
        else{
            actEquEdgesSet = new EquEdgesSet(actEdgeSubPaths, this);   // Mengen gleichwertiger Kanten für aktuellen DecLvl berechnen
            return createSubTreesForNextDecLvl(actDecLvlRootNodes);
        }
    }
    private HashSet<TreeNode> createSubTreesForNextDecLvl(HashSet<TreeNode> actDecLvlRootNodes){
        HashSet<TreeNode> out = new HashSet<>();
        for (TreeNode actLvlSubTree: actDecLvlRootNodes){
            for (TreeEdge edge: actLvlSubTree.getAllFollowUpEdges()){
                edge.createSubTreeForEdge(actEquEdgesSet);
                out.add(edge.getCombinedSubTreeRoot());
            }
        }
        return out;
    }
    /**
     * Gibt die in den übergebenen Kantenmengen enthaltene Gesamtmenge aller Pfade der nächsten Entscheidungsebene zurück.
     */
    private void nextDecLvlPathList(EquEdgesSet indexedEdgeSets){  // Zur Ermittlung aller Kanten der Ebene, zu der diese Pfade gehören.
        actEdgeSubPaths.clear();
        for (Iterator<HashSet<HashSet<TreeEdge>>> nonEmptySetIter = indexedEdgeSets.nonEmptySetIterator(); nonEmptySetIter.hasNext();){
            HashSet<HashSet<TreeEdge>> perNeuronIndex = nonEmptySetIter.next();
            for (HashSet<TreeEdge> set: perNeuronIndex){
                for (TreeEdge edge: set) {
                    actEdgeSubPaths.addAll(edge.getAllPaths());
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeuralNet net = (NeuralNet) o;
        return neuralLayers != null ? neuralLayers.equals(net.neuralLayers) : net.neuralLayers == null;
    }

    @Override
    public int hashCode() {
        return neuralLayers != null ? neuralLayers.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NeuralNet{" +
                "neuralLayers=" + neuralLayers +
                '}';
    }
}

class wrongLayerNum1Exception extends Exception{
     wrongLayerNum1Exception(String s){
        super(s);
    }
}
class wrongLayerNum2Exception extends Exception{
    wrongLayerNum2Exception(String s){
        super(s);
    }
}