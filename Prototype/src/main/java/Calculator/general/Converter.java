package Calculator.general;

import Calculator.element_types.LayerType;
import Calculator.gui.InputVisualizer;
import Calculator.gui.TreeVisualizer;
import Calculator.net_elements.NeuralLayer;
import Calculator.net_elements.NeuralNet;
import Calculator.tree_elements.PixelInput;
import Calculator.tree_elements.TreeDecision;
import Calculator.tree_elements.TreeEdge;
import Calculator.net_elements.Neuron;
import Calculator.tree_elements.TreePath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Converter {

    private NeuralNet net;
    private double[] actInput;
    private double activationThreshold;
    private int decNum;

    private HashSet<TreeEdge> listOfExistingEdges = new HashSet<>();

    public Converter(NeuralNet net, double[] actInput, double threshold, int netDecs) {
        this.net = net;
        this.actInput = actInput;
        activationThreshold = threshold;
        decNum = netDecs+1;
    }

    /*
    Erstellt ein Tupel aus der vollständigen äußeren Kante eines Entscheidungspfades und des zugehörigen Eingabevektors zu diesem Entscheidungspfad.
     */
    public TreeDecision convertNetDecision(int lastLayerNum){
        calcThresholdFaktorsForLayers();
        Neuron[] sourceNeurons = net.getNeuralLayers().get(net.getNeuralLayers().size()-1).getFilterNeurons(0);
        TreeEdge outerEdge = new TreeEdge(sourceNeurons,0,0,decNum);
        listOfExistingEdges.clear();                                                                                    // Sicher ist sicher! Aber eigentlich überflüssig.
        listOfExistingEdges.add(outerEdge);
        processPathsForNeurons(outerEdge, 0, net.getNeuralLayers().get(net.getNeuralLayers().size()-2), net.getNeuralLayers().size()-2, 1);
        deleteSubEdgesWithoutInputs(outerEdge);
        return new TreeDecision(actInput, outerEdge, MathOps.interpretOutput(net.getNeuralLayers().get(lastLayerNum), lastLayerNum));
    }
    /*
    Gibt eine Kante für das angegebene Neuron zurück. Dies ist entweder die bereits existierende Kante aus listOfExistingEdges oder, wenn noch keine existiert, eine neu ertsellte Kante.
    */
    private TreeEdge addEdge(Neuron neuron, int filterNum, int neuronNum, NeuralLayer predLayer, int predLayerNum, int decLvlNum){ // Initiale Erstellung über createEdgeForNeuron().
        for (TreeEdge e: listOfExistingEdges) { if (e.getSourceNeurons()[0].equals(neuron)) { return e; } }
        return createEdgeForNeuron(neuron, filterNum, neuronNum, predLayer, predLayerNum, decLvlNum);
    }
    /*
    Erstellt eine neue Kante für das angegebene Neuron. Kann daher nicht auf Eingabeschichten und Flatten-Schichten angewandt werden, da diese keine Neuronenobjekte haben.
    */
    private TreeEdge createEdgeForNeuron(Neuron neuron, int filterNum, int neuronNum, NeuralLayer predLayer, int predLayerNum, int decLvlNum){  // Fuer relevante Neuronen der Schichten 1 bis n-1.
        Neuron[] sourceNeurons = {neuron};
        TreeEdge out = new TreeEdge(sourceNeurons, filterNum, neuronNum, decNum);
        listOfExistingEdges.add(out);
        processPathsForNeurons(out, filterNum, predLayer, predLayerNum, decLvlNum);
        out.calcCorrespFirstLayerInpSums(net);     // Wird nicht für äußere Kanten berechnet, da gleichwertigkeit hier nur vom Klassifikationsergebnis abhängt!
        return out;
    }
    /*
    Veranlasst für die angegebene Kante die korrekte Erstellung des enthaltenen Pfades inklusive dessen enthaltener Kanten.
    */
    private void processPathsForNeurons(TreeEdge out, int filterNum, NeuralLayer predLayer, int predLayerNum, int decLvlNum){ // Hilfsmethode für createEdgeForNeuron() und convertNetDecision().
        Neuron[] neurons = out.getSourceNeurons();
        if (predLayerNum == 0) {                                          // Wenn die Neuronen bereits aus der ersten vollwertigen Schicht des Netzes sind (2te) -> relevante Eingabeindizes übernehmen.
            ArrayList<Integer> relConIndices = new ArrayList<>();
            for (Neuron n: neurons){ addAllNonDuplicants(relConIndices, n.getRelevantActivationConnectionIndices(actInput, (predLayer.getCurAvgNeuronActivation() * activationThreshold))); }
            HashSet<PixelInput> newRelInpVar = out.getRelInputVariantsForTree().get(0); newRelInpVar.clear();
            for (Integer i: relConIndices) {
                out.addCorrespInput(i);
                newRelInpVar.add(new PixelInput(actInput[i], InputVisualizer.transformInputToPixelCoord(i, TreeVisualizer.getInputWidth())));
            }
        }
        else {                                                              // Wenn nicht einen neuen Pfad mit den relevanten Neuronen der Vorgängerschicht erzeugen
            if (predLayer.getLayerType().equals(LayerType.CORE_FLATTEN)){   // Weiterleitung für Flattenlayer.
                NeuralLayer prePredLayer = net.getNeuralLayers().get(predLayerNum-1);
                flattenEdgesOperation(out, prePredLayer, predLayerNum-1, decLvlNum);
            }
            else {                                                          // Generische Bearbeitung für alle anderen Schichttypen.
                if (predLayer.getNumOfFilters() == 1){                      // Diese Lösung berücksichtigt den Fall Ein-Filter-Schicht auf Ein-Filter-Schicht und zusätzlich auch auf Mehrfilter-Schicht.
                    nonFlattenEdgesOperation(out,0, predLayer, predLayerNum, decLvlNum);
                }
                else {                                                      // Berücksichtigung Mehrfilter-Schicht auf Mehrfilter-Schicht und würde auch Ein-Filter-Schicht auf Ein-Filter-Schicht abdecken.
                    nonFlattenEdgesOperation(out, filterNum, predLayer, predLayerNum, decLvlNum);
                }
            }
        }
    }
    /*
   Erzeugt für ein Neuron den enthaltenen Pfad inklusive der Verknüpfung dessen enthaltener Kanten und fügt diesen Pfad dem Neuron hinzu. Wird verwendet wenn Vorgängerschicht eine Flattenschicht IST!
    */
    private void flattenEdgesOperation(TreeEdge out, NeuralLayer prePredLayer, int prePredLayerNum, int decLvlNum){ // Hilfsmethode für processPathsForNeurons().
        int filterLength = prePredLayer.getFilterNeurons(0).length;                                                             // Setzt gleiche Länge aller Filter voraus (Standard in TF).
        Neuron[] allFilterNeurons = new Neuron[prePredLayer.getNumOfFilters()*filterLength];                                           // Alle Neuronen der Flatten-Vorgängerschicht in einer Dimension.
        Neuron[] actFilter;
        for (int j = 0; j < prePredLayer.getNumOfFilters(); j++) {                                                                     // Spiegelt die Flatten-Operation und
            actFilter = prePredLayer.getFilterNeurons(j);                                                                             // vereinigt alle Filter des prePredLayer hintereinander zu einem Filter.
            for (int i = 0; i < filterLength; i++) { allFilterNeurons[j*filterLength + i] = actFilter[i]; }
        }
        ArrayList<Integer> relConIndices = new ArrayList<>();                   // Ermitteln der Indizes aller relevanten Vorvorgängerneuronen (aus allFilterNeurons) aller Neuronen der aktuellen Schicht.
        for (Neuron n: out.getSourceNeurons()) {
            addAllNonDuplicants(relConIndices, n.getRelevantActivationConnectionIndices(getNeuronActivationsFromFilter(allFilterNeurons), (prePredLayer.getCurAvgNeuronActivation() * activationThreshold)));
        }
        TreeEdge[] pathArray = new TreeEdge[relConIndices.size()];
        for (int x = 0; x < relConIndices.size(); x++) {                    // Alle rel. Neuronen, für die ein Index ermittelt wurde, in den enthaltenen Pfad aufnehmen.
            Integer neuronIndex = relConIndices.get(x);
            int filternum = neuronIndex/filterLength;
            int neuronNum = neuronIndex - (filternum*filterLength);
            pathArray[x] = addEdge(allFilterNeurons[neuronIndex], filternum, neuronNum, net.getNeuralLayers().get(prePredLayerNum-1), prePredLayerNum-1, decLvlNum+2);
            out.addAllCorrespInputs(pathArray[x].getCorrespInputs());                       // Zuordnung der jeweiligen CorrespondingInputs aus den relevanten Vorgängern.
        }
        TreePath edgePath = new TreePath(pathArray, decLvlNum);
        out.addPathWithInp(edgePath, actInput);
    }
    /*
    Erzeugt für ein Neuron den enthaltenen Pfad inklusive der Verknüpfung dessen enthaltener Kanten und fügt diesen Pfad dem Neuron hinzu. Wird verwendet wenn Vorgängerschicht KEINE Flattenschicht ist!
     */
    private void nonFlattenEdgesOperation(TreeEdge out, int filterNum, NeuralLayer predLayer, int predLayerNum, int decLvlNum){  // Hilfsmethode für processPathsForNeurons().
        ArrayList<Integer> relConIndices = new ArrayList<>();
        for (Neuron n: out.getSourceNeurons()) {
            addAllNonDuplicants(relConIndices, n.getRelevantActivationConnectionIndices(getNeuronActivationsFromFilter(predLayer.getFilterNeurons(filterNum)), (predLayer.getCurAvgNeuronActivation() * activationThreshold)));
        }
        Iterator<Integer> relConIndicesIter = relConIndices.iterator();
        TreeEdge[] pathArray = new TreeEdge[relConIndices.size()];
        for (int x = 0; x < relConIndices.size(); x++) {
            Integer neuronIndex = relConIndicesIter.next();
            pathArray[x] = addEdge(predLayer.getFilterNeurons(filterNum)[neuronIndex], filterNum, neuronIndex, net.getNeuralLayers().get(predLayerNum-1), predLayerNum-1, decLvlNum+1);
            for (Integer in: pathArray[x].getCorrespInputs()){ out.addCorrespInput(in); }   // Zuordnung der jeweiligen CorrespondingInputs aus den relevanten Vorgängern.
        }
        TreePath edgePath = new TreePath(pathArray, decLvlNum);
        out.addPathWithInp(edgePath, actInput);
    }
    /*
    Gibt zu einem Array von Neuronen eine Array von double mit deren Aktivierungen zurück.
    */
    private double[] getNeuronActivationsFromFilter(Neuron[] filterNeurons){                                         // Hilfsmethode für flattenEdgesOperation() und nonFlattenEdgesOperation().
        double[] out = new double[filterNeurons.length];
        for (int i = 0; i < out.length; i++) { out[i] = filterNeurons[i].getActivation(); }
        return out;
    }
    /*
    Veranlasst, dass für jede Schicht die Durchschnittsaktivierung für deren Neuronen berechnet und bereitgelegt wird.
    */
    private void calcThresholdFaktorsForLayers() {
        int cnt = 0;
        for (NeuralLayer n: net.getNeuralLayers()) {
            n.setCurAvgNeuronActivationForLayer(actInput);
        }
    }
    private ArrayList<Integer> addAllNonDuplicants(ArrayList<Integer> outList, ArrayList<Integer> elementsToAdd){   // Verwendung überprüfen. Kann ggf. ersatzlos gestrichen werden, weil Test an diesen Stellen nicht nötig ist!
        for (Integer i: elementsToAdd) {if (!outList.contains(i)) outList.add(i); }
        return outList;
    }

    private void deleteSubEdgesWithoutInputs(TreeEdge rootEdge){
        for (TreePath path: rootEdge.getAllPaths()){
            path.deleteEdgesWithoutInputs();
            for (TreeEdge edgeWithInput: path.getEdges()) deleteSubEdgesWithoutInputs(edgeWithInput);
        }
    }
}
