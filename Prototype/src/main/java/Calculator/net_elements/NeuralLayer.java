package Calculator.net_elements;

import Calculator.general.MapperService;
import Calculator.element_types.*;
import Calculator.general.MathOps;
import Calculator.gui.LayerVisualizer;
import Calculator.net_elements.activation_elements.ActivationMessage;
import Calculator.net_elements.cnn_elements.*;
import Calculator.net_elements.pooling_elements.PoolingFilter;
import Calculator.net_elements.pooling_elements.PoolingFilter1D;
import Calculator.net_elements.pooling_elements.PoolingFilter2D;
import Calculator.net_elements.pooling_elements.PoolingFilter3D;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Klasse zur Nachbildung einer künstlichen Neuronalen Schicht nach Keras.
 */
public class NeuralLayer {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);
    private String name;

    private LayerType layerType;
    private ActivationType activationType;
    private ConstraintType biasConstraintType, kernelConstraintType;
    private InitializerType biasInitializerType, kernelInitializerType;
    private RegularizerType activityRegularizerType, biasRegularizerType, kernelRegularizerType;

    private CnnKernel kernel = null;
    private PoolingFilter pooling = null;
    private PaddingType padding = PaddingType.NONE;
    private int[] strides = new int[0];
    private ArrayList<Pair<Neuron[], GridPane>> filterNeurons = new ArrayList<>();

    private ArrayList<double[]> minFactorActivations = new ArrayList<>();
    private ArrayList<double[]> maxFactorActivations = new ArrayList<>();   // Liste der jeweiligen bedingten Eingangsaktivierungen von den Vorgängerneuronen, multipliziert mit deren jeweiligen Aktivierungsfunktionen.
    private double avgMaxFactorActivation = 0.0;
    private double curAvgNeuronActivation = 0.0;

    private GridPane chosenfilterVisualizer = new GridPane();
    private Map<String, String> config;

    /**
     * Konstruktor der generischen Schicht Neural Layer des Neuronalen Netztes.
     * Alle deklarierten Variablen werden mit Standardwerten initialisiert und später durch entsprechende
     * Setter-Methoden gesetzt oder, abhängig von der abzubildenen Schicht, auf dem Standardwert belassen.
     */
    public NeuralLayer() {
        name = "";
        layerType = LayerType.NONE;
        activationType = ActivationType.NONE;
        biasConstraintType = ConstraintType.NONE;
        kernelConstraintType = ConstraintType.NONE;
        biasInitializerType = InitializerType.NONE;
        kernelInitializerType = InitializerType.NONE;
        activityRegularizerType = RegularizerType.NONE;
        biasRegularizerType = RegularizerType.NONE;
        kernelRegularizerType = RegularizerType.NONE;
        config = new HashMap<>();
    }
    /**
     * @return Name der Schicht
     */
    public String getName() {
        return name;
    }
    /**
     * @param name zu setztender Name der Schicht
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return Typ der Schicht
     */
    public LayerType getLayerType() {
        return layerType;
    }
    /**
     * @param layerType zu setztender Typ der Schicht
     */
    public void setLayerType(LayerType layerType) {
        this.layerType = layerType;
    }
    /**
     * @return ActivationType der Schicht
     */
    public ActivationType getActivationType() {
        return activationType;
    }
    /**
     * @param activationType zu setztender ActivationType der Schicht
     */
    public void setActivationType(ActivationType activationType) {
        this.activationType = activationType;
    }
    /**
     * @return denn aktuell gespeicherten Kernel der Conv-Schicht
     */
    public CnnKernel getCnnKernel() { return kernel; }

    public ArrayList<double[]> getMinFactorActivations() { return minFactorActivations; }
    public ArrayList<double[]> getMaxFactorActivations() { return maxFactorActivations; }
    public double getAvgMaxFactorActivation() { return avgMaxFactorActivation; }
    public double getCurAvgNeuronActivation() { return curAvgNeuronActivation; }

    public void initMinMaxInputActivations(double minInput, double maxInput, NeuralNet net){
        if (this.equals(net.getNeuralLayers().get(0))){
            int vecLenght = net.getInputSize();
            double[] pureMinInputVector = new double[vecLenght]; Arrays.fill(pureMinInputVector, minInput);
            double[] pureMaxInputVector = new double[vecLenght]; Arrays.fill(pureMaxInputVector, maxInput);                           // Intialisierung des maximalen Eingabevektors. --> to be improved!
            minFactorActivations.clear(); minFactorActivations.add(pureMinInputVector);
            maxFactorActivations.clear(); maxFactorActivations.add(pureMaxInputVector);
            avgMaxFactorActivation = maxInput;
        }
    }

    /**
     * @param grid das GridPane, welches den jeweils angezeigten Filter visualisiert
     */
    public void setChosenFilterVisualizer(GridPane grid) {
        chosenfilterVisualizer.getChildren().clear();
        chosenfilterVisualizer.add(grid,0,0);
    }
    /**
     * @return der Containerknoten des GridPanes, welches den jeweils angezeigten Filter visualisieren soll
     */
    public Node getChosenfilterVisualizerContainer() { return chosenfilterVisualizer; }

    /**
     * @param filter neu hinzu zufügendes Neuronen-Array für die Filterliste von Convolutionsschichten
     */
    public void addNeuronsForNewFilter(Neuron[] filter) { this.filterNeurons.add(new Pair<>(filter, LayerVisualizer.getNewFilterGrid())); }

    /**
     * @return ein einzelner Filter als Array von Neuronen mit zugehörigem Bias und Gewichten
     */
    public Neuron[] getFilterNeurons(int index) { return filterNeurons.get(index).getKey(); }
    /**
     * @return das GridPane, welches den angegebenen Filter visualisiert
     */
    public GridPane getFilterNeuronVisuals(int index) { return filterNeurons.get(index).getValue(); }

    /**
     * @return die Anzahl der (Convolutions)- Filter dieser Schicht
     */
    public int getNumOfFilters() { return filterNeurons.size(); }

    /**
     * @return Die Anzahl aller Neuronen in allen Filtern dieser Schicht.
     */
    public int allFiltersLength() {
        int val= 0;
        for (int i = 0; i < getNumOfFilters(); i++){
            val = val + filterNeurons.get(i).getKey().length;
        }
        return val;
    }

    /**
     * Initialisiert einen Kernel mit typgerechter Dimension.
     */
    public void initializeKernel(String layerType) {
        if (layerType.contains("1D")) { this.kernel = new CnnKernel1D(); }
        if (layerType.contains("2D")) { this.kernel = new CnnKernel2D(); }
        if (layerType.contains("3D")) { this.kernel = new CnnKernel3D(); }
    }

    /**
     * @param kernelWeights neu zu setzende Gewichtsmatrix für den 1-dimensionalen Kernel der Conv-Schicht
     */
    public void  setKernelWeights(double[] kernelWeights) {
        try { this.kernel.setKernelWeights(kernelWeights); }
        catch(WrongKernelDimensionException e) {System.out.println(e.getMessage());}
    }

    /**
     * @param kernelWeights neu zu setzende Gewichtsmatrix für den 2-dimensionalen Kernel der Conv-Schicht
     */
    public void  setKernelWeights(double[][] kernelWeights) {
        try { this.kernel.setKernelWeights(kernelWeights); }
        catch(WrongKernelDimensionException e) {System.out.println(e.getMessage());}
    }

    /**
     * @param kernelWeights neu zu setzende Gewichtsmatrix für den 3-dimensionalen Kernel der Conv-Schicht
     */
    public void  setKernelWeights(double[][][] kernelWeights) {
        try { this.kernel.setKernelWeights(kernelWeights); }
        catch(WrongKernelDimensionException e) {System.out.println(e.getMessage());}
    }

    /**
     * @return das auf die Conv-Schicht angewandte Padding
     */
    public PaddingType getPadding() { return padding; }

    /**
     * @param padding zu setztendes Padding der Schicht
     */
    public void  setPadding(PaddingType padding) { this.padding = padding; }

    /**
     * @return Pooling der Schicht
     */
    public PoolingFilter getPooling() { return pooling; }

    /**
     * @param poolingSizes zu setztende Größen des Poolings der Schicht in den jeweiligen Dimensionen (anhand der Config)
     */
    public void  setPoolingFilter(int[] poolingSizes) {
        if(poolingSizes.length == 1){ this.pooling = new PoolingFilter1D(poolingSizes[0]); }
        else if (poolingSizes.length == 2) { this.pooling = new PoolingFilter2D(poolingSizes[0], poolingSizes[1]); }
        else { this.pooling = new PoolingFilter3D(poolingSizes[0], poolingSizes[1], poolingSizes[3]); }
    }

    /**
     * @return Strides der Schicht
     */
    public int[] getStrides() { return strides; }

    /**
     * @param strides zu setztende Strides der Schicht
     */
    public void  setStrides(int[] strides) { this.strides = strides; }

    /**
     * @return BiasConstraintType der Schicht
     */
    public ConstraintType getBiasConstraintType() {
        return biasConstraintType;
    }

    /**
     * @param biasConstraintType zu setztender BiasConstraintType der Schicht
     */
    public void setBiasConstraintType(ConstraintType biasConstraintType) {
        this.biasConstraintType = biasConstraintType;
    }

    /**
     * @return KernelConstraintType der Schicht
     */
    public ConstraintType getKernelConstraintType() {
        return kernelConstraintType;
    }

    /**
     * @param kernelConstraintType zu setztender KernelConstraintType der Schicht
     */
    public void setKernelConstraintType(ConstraintType kernelConstraintType) {
        this.kernelConstraintType = kernelConstraintType;
    }

    /**
     * @return BiasInitializerType der Schicht
     */
    public InitializerType getBiasInitializerType() {
        return biasInitializerType;
    }

    /**
     * @param biasInitializerType zu setztender BiasInitializerType der Schicht
     */
    public void setBiasInitializerType(InitializerType biasInitializerType) {
        this.biasInitializerType = biasInitializerType;
    }

    /**
     * @return KernelInitializerType der Schicht
     */
    public InitializerType getKernelInitializerType() {
        return kernelInitializerType;
    }

    /**
     * @param kernelInitializerType zu setztender KernelInitializerType der Schicht
     */
    public void setKernelInitializerType(InitializerType kernelInitializerType) {
        this.kernelInitializerType = kernelInitializerType;
    }

    /**
     * @return ActivityRegularizerType der Schicht
     */
    public RegularizerType getActivityRegularizerType() {
        return activityRegularizerType;
    }

    /**
     * @param activityRegularizerType zu setztender ActivityRegularizerType der Schicht
     */
    public void setActivityRegularizerType(RegularizerType activityRegularizerType) {
        this.activityRegularizerType = activityRegularizerType;
    }

    /**
     * @return BiasRegularizerType der Schicht
     */
    public RegularizerType getBiasRegularizerType() {
        return biasRegularizerType;
    }

    /**
     * @param biasRegularizerType zu setztender BiasRegularizerType der Schicht
     */
    public void setBiasRegularizerType(RegularizerType biasRegularizerType) {
        this.biasRegularizerType = biasRegularizerType;
    }

    /**
     * @return KernelRegularizerType der Schicht
     */
    public RegularizerType getKernelRegularizerType() {
        return kernelRegularizerType;
    }

    /**
     * @param kernelRegularizerType zu setztender KernelRegularizerType der Schicht
     */
    public void setKernelRegularizerType(RegularizerType kernelRegularizerType) {
        this.kernelRegularizerType = kernelRegularizerType;
    }

    /**
     * @return zusätzliche spezifische Schichtenkonfiguration
     */
    public Map<String, String> getConfig() {
        return config;
    }

    /**
     * @param config zu setzende zusätzliche spezifische Schichtenkonfiguration
     */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    /**
     * Gibt ein Array mit den Gewichten aller Verbindungen zurück, die von dieser Schicht auf ein angegebenes Neuron eines angegebnen Filters der Vorgängerschicht zeigen.
     *
     * @param layerNum Der Index der Vorgängerschicht im Netz.
     * @param neuronNum Der Index des Zielneurons im zugehörigen Filter.
     * @param filterNum Der Index des Filters, in dem sich das Zielneuron in der Vorgängerschicht befindet.
     */
    public double[] getWeightsFromPredecessorNeurons(int layerNum, int neuronNum, int filterNum) {
        try{
            double[] weights = null;
            if (layerType.equals(LayerType.CORE_DENSE)) { weights = getNeuronWeightsForSingleFilter(filterNum, neuronNum); }
            else if (layerType.equals(LayerType.CORE_ACTIVATION)) {}
            else if (layerType.equals(LayerType.CORE_DROPOUT)) {}
            else if (layerType.equals(LayerType.CORE_FLATTEN)) { weights = getNeuronWeightsForSingleFilter(filterNum, neuronNum); }
            else if (layerType.equals(LayerType.CONVOLUTION_CONV_1D) || layerType.equals(LayerType.CONVOLUTION_CONV_2D)  || layerType.equals(LayerType.CONVOLUTION_CONV_3D)) { weights = getNeuronWeightsForSingleFilter(filterNum, neuronNum); }
            else if (layerType.equals(LayerType.POOLING_MAX_1D) || layerType.equals(LayerType.POOLING_MAX_2D)  || layerType.equals(LayerType.POOLING_MAX_3D)) { weights = getNeuronWeightsForSingleFilter(filterNum, neuronNum); }
            else { logger.error("Für denn Layertyp " + layerType + " koennen keine Gewichte aus der Vorgaengerschicht ermittelt werden!"); }
            return weights;
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("The neuron " + neuronNum + " in layer " + layerNum + " does not exist!");
            return null;
        }
    }
    /**
     * Hilfsmethode für getWeightsFromPredecessorNeurons.
     */
    private double[] getNeuronWeightsForSingleFilter(int filterNum, int neuronNum) {
        double[] weights = new double[getFilterNeurons(filterNum).length];
        for (Integer index=0; index < getFilterNeurons(filterNum).length; index++){
            weights[index] = getFilterNeurons(filterNum)[index].getIncomingWeights()[neuronNum];
        }
        return weights;
    }

    /**
     * Gibt zurück, ob es in dieser Schicht zu berücksichtigende gewichtete Verbindungen zu einem bestimmten Neuron der Vorgängerschicht gibt. Gilt NICHT für Flatten-Schichten.
     *
     * @param filterNum Der Index des zum angegeben Neuron gehörigen Filter.
     * @param predNeuronNum Der Index des gesuchten Vorgängerneurons.
     */
    public boolean hasRelevantWeightsToPredecessorNeuronStandard(int filterNum, int predNeuronNum) {
        boolean hasRelevantWeights = false;
        for (int i = 0; i < getFilterNeurons(filterNum).length; i++) {                                                                  // Durchsuche alle Neuronen des selben Filters
            if (getFilterNeurons(filterNum)[i].hasRelevantWeigthToPredecessor(predNeuronNum)) { hasRelevantWeights = true; break; }
        }
        return hasRelevantWeights;
    }

    /**
     * Gibt zurück, ob es in dieser Schicht zu berücksichtigende gewichtete Verbindungen zu einem bestimmten Neuron der Vorgängerschicht gibt. Gilt NUR für Flatten-Schichten.
     *
     * @param filterSectionStart Index, an dem die Auflistung aller Werte eines bestimmten Einzelfilters im Gesamtfilter der folgenden Flatten-Schicht beginnt.
     * @param predNeuronNum Die Nummer des gesuchten Vorgängerneurons.
     */
    public boolean hasRelevantWeightsToPredecessorNeuronFlatten(int filterSectionStart, int predNeuronNum) {
        int flattenedNeuronNum = filterSectionStart + predNeuronNum;
        return getFilterNeurons(0)[flattenedNeuronNum].hasRelevantWeigthToPredecessor(flattenedNeuronNum);
    }

    /**
     * Setzt die relevanten Verbindungen einer SoftMax-Schicht.
     *
     * @param tresholdFactor Relative Mindestabweichung, die eine maximale Gewichtsdifferenz für eine Verbindung erfüllen muss.
     * @param predLayerNum Nummer der vorletzten Schicht des Neuronalen Netzes.
     */
    public void setRelevantSoftMaxLayerWeights(double tresholdFactor, int predLayerNum) {   // ggf. noch Sicherheitsabfrage mit neuem Typ für SoftMax
        int neuronCnt = getFilterNeurons(0).length;
        int predNeuronCnt = getFilterNeurons(0)[0].getIncomingWeights().length;
        double[][] incomingWeightGroups = new double[predNeuronCnt][neuronCnt];         // Matrix für eingehende Gewichte - Je X Vorgängerneuronen Y Gewichte zu den Neuronen der SoftMax-Schicht
        for (int i = 0; i < predNeuronCnt; i++){                                        // Befüllung der Matrix
            incomingWeightGroups[i] = getWeightsFromPredecessorNeurons(predLayerNum, i, 0);  // Es muss immer eine Dense-Layer über einem SoftMax-Layer liegen, daher filterNum 0!
        }
        double[][] maxDiffPerWeight = new double[neuronCnt][predNeuronCnt];     // Matrix für Speicherung der maximalen Differenzbeträge - Die Maximaldifferenz der X Eingangsgewichte von SoftMax je einem der Y Vorgängerneuronen
        double[] weightsDiff; double actMax; double sumDiff = 0;
        for (int i = 0; i < predNeuronCnt; i++){                                // Die Indizes i und j sind entsprechend der Betragsbezeichnung in Kapitel 6.3.2 !
            for (int j = 0; j < neuronCnt; j++){
                weightsDiff = calcDifferencesAbs(j,incomingWeightGroups[i]);
                actMax = 0;
                for (double actDiff: weightsDiff){ if (actMax < actDiff) actMax = actDiff; }
                sumDiff = sumDiff + actMax;
                maxDiffPerWeight[j][i] = actMax;                    // Methode "maxDiffPerWeight" durch vertauschte Reihen und Spalten für späteren Zugriff durch "addRelevantWeightSoftMax" optimiert!
            }
        }
        double tresholdSoftMax = sumDiff / neuronCnt / predNeuronCnt * tresholdFactor;     // Der Vergleichsgrenzwert wird über den maximalen Differenzbeträgen aller Gewichte aller Ausgabeneuronen gebildet.         // noch rausoptimieren durch Umstellung von maxDiffPerWeight
        for (int k = 0; k < neuronCnt; k++){
            getFilterNeurons(0)[k].addRelevantWeightsSoftMax(tresholdSoftMax, maxDiffPerWeight[k], layerType);
            // System.out.println("Max. Differenz " + 11 + "|" + k + " : " + maxDiffPerWeight[k][11]); // Testausgabe zur Kontrolle der max Differenzbeträge - kann nachher gelöscht werden!
        }
    }
    /**
     * Hilfsmethode für setRelevantSoftMaxLayerWeights.
     */
    private double[] calcDifferencesAbs(int weightNum, double[] weights) {
        double[] diff = new double[weights.length];
        for (int i = 0; i < weights.length; i++){
            diff[i] = Math.abs(weights[weightNum] - weights[i]);
        }
        return diff;
    }

    /**
     * Erfasst, gemessen an einem gegebenen Grenzwert, welche Verbindungen bei Berechnungen beachtet werden sollen.
     *
     * @param treshold Der zu berücksichtigende Grenzwert.
     * @param layerNum Die wievielte Schicht im Netzt dies ist.
     * @param net Das zu dieser Schicht gehörige Netz.
     * @param predMaxFactorActivations Die oberen Grenzen der Maximalaktivierungen aller Neuronen aller Filter der Vorgängerschicht.
     * @param predMinFactorActivations Die unteren Grenzen der Minimalaktivierungen aller Neuronen aller Filter der Vorgängerschicht.
     */
    public void setRelevantLayerWeights(double treshold, NeuralNet net, int layerNum, ArrayList<double[]> predMaxFactorActivations, ArrayList<double[]> predMinFactorActivations){
        NeuralLayer succLayer = net.getNeuralLayers().get(layerNum + 1);
        int sectionStart = 0;                                                   // nur bei Flatten-Schichten notwendig, ansonsten Overhead.
        Neuron actNeuron;
        for (int i = 0; i < filterNeurons.size(); i++) {
            int sectionLenght = filterNeurons.get(i).getKey().length;
            for (int j = 0; j < sectionLenght; j++) {
                boolean relevantNeuron;
                if (succLayer.getLayerType().equals(LayerType.CORE_FLATTEN)) { relevantNeuron = succLayer.hasRelevantWeightsToPredecessorNeuronFlatten(sectionStart, j); }
                else { relevantNeuron = succLayer.hasRelevantWeightsToPredecessorNeuronStandard(i, j); }
                actNeuron = filterNeurons.get(i).getKey()[j];
                actNeuron.setRelevantNeuronWeights(treshold, relevantNeuron, layerNum, layerType, predMaxFactorActivations.get(i), predMinFactorActivations.get(i));
            }
            sectionStart = sectionStart + sectionLenght;                        // nur bei Flatten-Schichten notwendig, ansonsten Overhead
        }
    }
    /**
     * Berechne für die Neuronen aller Filter dieser Schicht die oberen und unteren Grenzen der maximalen und minimalen eingehenden Gesamtaktivierungen.
     *
     * @param minPredFactorActivations 2D Vektor mit den unteren Grenzen der minimalen Ausgangsaktivierungen der Neuronen aller Filter der Vorgängerschicht dieser Schicht.
     * @param maxPredFactorActivations 2D Vektor mit den oberen Grenzen der maximalen Ausgangsaktivierungen der Neuronen aller Filter der Vorgängerschicht dieser Schicht.
     */
    public void calcLayerActivationFactors(ArrayList<double[]> minPredFactorActivations, ArrayList<double[]> maxPredFactorActivations) {
        if (!(minPredFactorActivations.size() == maxPredFactorActivations.size())) {    // Prüft ob min und max Eingaben für die Gleiche Anzahl an Filtern vorliegen.
            logger.error("Es gibt " + minPredFactorActivations.size() + " Filtervektoren für Minimaleingaben und " + maxPredFactorActivations.get(0).length + "Filtervektoren für Maximaleingaben");
            System.exit(1);                                                      // Abbruch bei abweichender Anzahl der Eingabevektoren!
        }
        if (!(minPredFactorActivations.get(0).length == maxPredFactorActivations.get(0).length)) {  // Prüft ob min und max Eingaben für die Gleiche Anzahl an Neuronen vorliegen.
            logger.error("Vektor der Minimaleingaben hat Länge " + minPredFactorActivations.get(0).length + ", Vektor der Maximaleingaben hat Länge " + maxPredFactorActivations.get(0).length);
            System.exit(1);                                                                  // Abbruch bei abweichender Länge der Eingabevektoren!
        }
        if (maxPredFactorActivations.size() == 1) logger.info("Berechne Max-Aktivierungen für Schicht " + this.getLayerType().getKerasName() + ":  - Vorgänger hat einen Filter");
        else if (maxPredFactorActivations.size() == filterNeurons.size()) logger.info("Berechne Max-Aktivierungen Schicht " + this.getLayerType().getKerasName() + ": - Vorgänger hat die selbe Filterzahl");
        else logger.info("Berechne Max-Aktivierungen Schicht " + this.getLayerType().getKerasName());

        if (maxPredFactorActivations.size() == 1 || maxPredFactorActivations.size() == filterNeurons.size()) { // Vorgängerschicht ist entweder Schicht mit einem Filter oder der gleichen Anzahl an Filtern.
            Iterator<double[]> minPredIter = minPredFactorActivations.iterator();
            Iterator<double[]> maxPredIter = maxPredFactorActivations.iterator();
            double[] actMinPredFactorAct = minPredIter.next(); double[] actMaxPredFactorAct = maxPredIter.next();
            for (Pair<Neuron[], GridPane> actFilter: filterNeurons){      // Für jeden Filter dieser Schicht.
                for (Neuron actNeuron: actFilter.getKey()){               // Für jedes Neuron im aktuellen Filter.
                    if (layerType == LayerType.POOLING_MAX_1D || layerType == LayerType.POOLING_MAX_2D || layerType == LayerType.POOLING_MAX_3D) {   // Wäre besser / lesbarer umzusetzen mit echten Unterklassen von NeuralLayer,  da alles bis auf eine Zeile gleich und die Prüfung nicht in jeder Schleife erfolgen soll!
                        actNeuron.calcActivationFactorMaxPooling(actMinPredFactorAct, actMaxPredFactorAct);     // Berechne die Grenzen für eingehende min und max Aktivierung mit der Methode für MaxPooling-Schichten. Typprüfung trotzdem bereits hier -> bessere Laufzeit!
                    }
                    else {                                                                                       // Für Nicht-Flatten- und Nicht-MaxPooling-Schichten.
                        actNeuron.calcActivationFactorStandard(actMinPredFactorAct, actMaxPredFactorAct, activationType); // Berechne die Summe aller eingehenden min und max Aktivierungs-Grenzen.
                    }
                    avgMaxFactorActivation = avgMaxFactorActivation + actNeuron.getMaxActivationFactor();   // Addiere die Grenze der max Aktivierung zur Summe dieser Aktivierungen in dieser Schicht.
                }
                if (maxPredIter.hasNext()) {                           // Schaltet nur weiter wenn mehr als ein Filter vorhanden ist, ansonsten wird immer der einzige Filter genutzt.
                    actMinPredFactorAct = minPredIter.next();
                    actMaxPredFactorAct = maxPredIter.next();
                }
            }
            avgMaxFactorActivation = avgMaxFactorActivation / (filterNeurons.size()*this.getFilterNeurons(0).length); // Ermittlung des Durchschnitts aus der Summe.
        }
        else { if (layerType != LayerType.CORE_FLATTEN) {logger.error("Filterzahl und Anzahl Vorgängerfilter stimmen nicht überein!"); System.exit(1); }} // Bei Flatten-Schichten wird einfach nichts getan!
    }

    /**
     * Löst die Aktivierungsfunktionen der Neuronen dieser Schicht mit deren bedingten Verbindungsstärken / abgeschätzten max. und min. Aktivierungen aus und legt Ergebnisse in factorActivations ab.
     *
     * @param minIncActivations 2D Vektor mit den unteren Grenzen der minimalen Gesamteingansaktivierungen der Neuronen aller Filter dieser Schicht.
     * @param maxIncActivations 2D Vektor mit den oberen Grenzen der maximalen Gesamteingansaktivierungen der Neuronen aller Filter dieser Schicht.
     */
    public void activateLayersFactorActivations(ArrayList<double[]> minIncActivations, ArrayList<double[]> maxIncActivations){
        if (!(minIncActivations.size() == maxIncActivations.size())) { // Prüft ob min und max Eingaben für die Gleiche Anzahl an Filtern vorliegen.
            logger.error("Es gibt " + minIncActivations.size() + " Filtervektoren für Minimaleingaben und " + maxIncActivations.get(0).length + "Filtervektoren für Maximaleingaben");
            System.exit(1);                                                   // Abbruch bei abweichender Anzahl der Eingabevektoren!
        }
        if (!(minIncActivations.get(0).length == maxIncActivations.get(0).length)) {  // Prüft ob min und max Eingaben für die Gleiche Anzahl an Neuronen vorliegen.
            logger.error("Vektor der Minimaleingaben hat Länge " + minIncActivations.get(0).length + ", Vektor der Maximaleingaben hat Länge " + maxIncActivations.get(0).length);
            System.exit(1);                                                                  // Abbruch bei abweichender Länge der Eingabevektoren!
        }
        minFactorActivations.clear();                                               // Lösche die Liste der Grenzen für ausgehende min Aktivierungen aller Neuronen der Filter dieser Schicht.
        maxFactorActivations.clear();                                               // Lösche die Liste der Grenzen für ausgehende max Aktivierungen aller Neuronen der Filter dieser Schicht.
        if (layerType == LayerType.CORE_FLATTEN) {                                  // Wenn diese Schicht eine Flatten-Schicht ist.
            int filterLength = maxIncActivations.get(0).length;                                      // Geht davon aus, dass alle Filter des Vorgängers die selbe Länge haben.
            double[] newFilterMinFactorAct = new double[maxIncActivations.size() * filterLength];    // Geht davon aus, dass alle Filter des Vorgängers die selbe Länge haben.
            double[] newFilterMaxFactorAct = new double[maxIncActivations.size() * filterLength];    // Geht davon aus, dass alle Filter des Vorgängers die selbe Länge haben.
            for (int i = 0; i < maxIncActivations.size(); i++) {             // Je Filter der Schicht. -> gem. Eingangsprüfungen für minPredFactorActivations und maxIncActivations zwingend gleich
                for (int j = 0; j < maxIncActivations.get(i).length; j++) {  // Je Neuron des Filters. -> gem. Eingangsprüfungen für minPredFactorActivations und maxIncActivations zwingend gleich
                    newFilterMinFactorAct[filterLength * i + j] = minIncActivations.get(i)[j];   // Übernimmt die zugehörige Grenze der min Aktivierung aus der Vorgängerschicht für die zugehörige Position der Flatten-Schicht.
                    newFilterMaxFactorAct[filterLength * i + j] = maxIncActivations.get(i)[j];   // Übernimmt die zugehörige Grenze der max Aktivierung aus der Vorgängerschicht für die zugehörige Position der Flatten-Schicht.
                }
            }
            minFactorActivations.add(newFilterMinFactorAct);    // Befüllt die Liste der Grenzen für ausgehende min Aktivierungen aller Positionen dieser Flatten-Schicht.
            maxFactorActivations.add(newFilterMaxFactorAct);    // Befüllt die Liste der Grenzen für ausgehende max Aktivierungen aller Positionen dieser Flatten-Schicht.
        }
        else {                                                                      // Wenn diese Schicht keine Flatten-Schicht ist.
            for (Pair<Neuron[], GridPane> filter : filterNeurons) {                 // Für jeden Filter dieser Schicht.
                Neuron[] actFilter = filter.getKey();
                double[] newFilterMinFactorAct = new double[actFilter.length];
                double[] newFilterMaxFactorAct = new double[actFilter.length];
                for (int i = 0; i < newFilterMaxFactorAct.length; i++) {    // Für jedes Neuron im aktuellen Filter. -> gem. Eingangsprüfungen für minPredFactorActivations und maxIncActivations zwingend gleich
                    if (activationType != ActivationType.SOFTMAX) {
                        newFilterMinFactorAct[i] = MathOps.calculateActivation(actFilter[i].getMinActivationFactor(), activationType);
                        newFilterMaxFactorAct[i] = MathOps.calculateActivation(actFilter[i].getMaxActivationFactor(), activationType);
                    }
                }
                minFactorActivations.add(newFilterMinFactorAct);
                maxFactorActivations.add(newFilterMaxFactorAct);
            }
        }
    }

    public ArrayList<ActivationMessage[]> calcLayerActivations(ArrayList<ActivationMessage[]> predActMessegesList) { // Methode zur Berechnugn und Weitergabe aller Neuronenaktivierungen und zugehörigen Input-Koordinaten einer Schicht.
        ArrayList<ActivationMessage[]> out = new ArrayList<>();
        if (layerType.equals(LayerType.CORE_FLATTEN)) {
            out.add(flattenLayerActivationTransformation(predActMessegesList));
        }
        else if (filterNeurons.size() == predActMessegesList.size()){
            Iterator<ActivationMessage[]> predActIterator = predActMessegesList.iterator();
            for (Pair<Neuron[], GridPane> filter : filterNeurons) {
                out.add(calcFilterActivations(filter.getKey(), predActIterator.next(), activationType));
            }}
        else if (predActMessegesList.size() == 1){
            for (Pair<Neuron[], GridPane> filter : filterNeurons) {
                out.add(calcFilterActivations(filter.getKey(), predActMessegesList.get(0), activationType));
            }}
        else {
            logger.error("Berechnung von Neuronenaktivierungen in Flattenschicht " + toString() + "nicht möglich -> Anzahl eingehender Aktivierungsfaktoren ungleich 1 und nicht deckungsgleich mit Anzahl Filter!");
            System.exit(1);
        }
        return out;
    }
    private ActivationMessage[] calcFilterActivations(Neuron[] filterNeurons, ActivationMessage[] predActMessages, ActivationType actType) { // Hilfsfunktion zur Berechnung aller Neuronenaktivierungen eines einzelnen Filters.
        ActivationMessage[] out = new ActivationMessage[filterNeurons.length];
        for (int i = 0; i < filterNeurons.length; i++) {
            if (layerType.equals(LayerType.POOLING_MAX_1D) || layerType.equals(LayerType.POOLING_MAX_2D) || layerType.equals(LayerType.POOLING_MAX_3D)) {
                out[i] = filterNeurons[i].calculateMaxPoolingNeuronActivation(predActMessages);
            } else if (layerType.equals(LayerType.CONVOLUTION_CONV_1D) || layerType.equals(LayerType.CONVOLUTION_CONV_2D) || layerType.equals(LayerType.CONVOLUTION_CONV_3D) || layerType.equals(LayerType.CORE_DENSE)) {
                out[i] = filterNeurons[i].calculateStandardNeuronActivation(predActMessages, actType);  // Für SoftMax wird als Vorbereitung je Neuron einfach die Eingangssumme berechnet.
            } else { logger.info("Berechnung von Aktivierungen für Schichttyp " + layerType + " bis jetzt nicht implementiert -> Rückgabe eines leeren Aktivierungsvektors!"); }
        }
        if (actType.equals(ActivationType.SOFTMAX)) {
            double[] actCache = MathOps.calculateSoftMaxActivations(filterNeurons);
            ActivationMessage[] out2 = new ActivationMessage[filterNeurons.length];
            for (int i = 0; i < filterNeurons.length; i++) { out2[i] = new ActivationMessage(actCache[i], out[i].getRelInputsForActivation(), out[i].getConInputsForActivation()); }
            out = out2; // Die Rückgabe wird in diesem Fall höchstens bei ungewöhnlichen Netzarchitekturen genutzt! (Nur einmal je Eingabe -> akzeptabel)!
        }
        return out;
    }
    private ActivationMessage[] flattenLayerActivationTransformation(ArrayList<ActivationMessage[]> predActMessagesList) { // Hilfsfunktion zur Weiterleitung aller Neuronenaktivierungen durch eine Flattenschicht.
        int newLength = 0;
        for (ActivationMessage[] a: predActMessagesList) { newLength = newLength + a.length;}
        ActivationMessage[] out = new ActivationMessage[newLength];
        Iterator<ActivationMessage[]> listIter = predActMessagesList.iterator();
        int offset = 0;
        while (listIter.hasNext()) {
            ActivationMessage[] next = listIter.next();
            for (int i = 0; i < next.length; i++) { out[i+offset] = next[i]; }
            offset = offset + next.length;
        }
        return out;
    }

    public void setCurAvgNeuronActivationForLayer(double[] input){
        double out = 0;
        int neuronNum = 0;
        if (layerType.equals(LayerType.NONE)){  // Geht davon aus, dass nur genau die Eingabeschicht den Typ "NONE" hat!
            neuronNum = input.length;
            for (double d: input) out = out + d;
        }
        else {
            for (int j = 0; j < getNumOfFilters(); j++){
                for (int i = 0; i < getFilterNeurons(j).length; i++){ out = out + getFilterNeurons(j)[i].getCurAvgIncActivationForNeuron(); }
                neuronNum = neuronNum + getFilterNeurons(j).length;
            }}
        curAvgNeuronActivation = (out/neuronNum);
    }

    public double[] calcLayerFirstFilterInputSum(double[] predInpActsList) { // WICHTIG: nur für Nutzung in erster Schicht (Zusatzlörung zur Laufzeitminimierung bei Kantenerzeugung).
        double[] out = new double[filterNeurons.get(0).getKey().length];     // -> ist nur für Schichtypen ausgelegt, die hier auftreten können und nur für Filter 0!
        if (layerType.equals(LayerType.POOLING_MAX_1D) || layerType.equals(LayerType.POOLING_MAX_2D) || layerType.equals(LayerType.POOLING_GLOBALMAX_3D)){  // Sollte eigentlich nie vorkommen,
            for (int i = 0; i < filterNeurons.get(0).getKey().length; i++){                                                                                 // weil sinnfreie Archtektur.
                out[i] = filterNeurons.get(0).getKey()[i].calculateMaxPoolNeuronInputSum(predInpActsList);
            }}
        else if (layerType.equals(LayerType.CORE_DENSE) || layerType.equals(LayerType.CONVOLUTION_CONV_1D) || layerType.equals(LayerType.CONVOLUTION_CONV_2D) || layerType.equals(LayerType.CONVOLUTION_CONV_3D)){
            for (int i = 0; i < filterNeurons.get(0).getKey().length; i++){                                                                                 // weil sinnfreie Archtektur.
                out[i] = filterNeurons.get(0).getKey()[i].calculateStanAndConvNeuronInputSum(predInpActsList);
            }}
        return out;
    }

    /**
     *  Gilt nicht für Flatten-Schichten. Muss eine Vorgängerschicht haben!
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NeuralLayer that = (NeuralLayer) o;

        if (!name.equals(that.name)) return false;
        if (layerType != that.layerType) return false;
        if (activationType != that.activationType) return false;
        if (biasConstraintType != that.biasConstraintType) return false;
        if (kernelConstraintType != that.kernelConstraintType) return false;
        if (biasInitializerType != that.biasInitializerType) return false;
        if (kernelInitializerType != that.kernelInitializerType) return false;
        if (activityRegularizerType != that.activityRegularizerType) return false;
        if (biasRegularizerType != that.biasRegularizerType) return false;
        if (kernelRegularizerType != that.kernelRegularizerType) return false;
        return Arrays.equals(filterNeurons.get(0).getKey(), that.filterNeurons.get(0).getKey());  //noch verbessern!
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (layerType != null ? layerType.hashCode() : 0);
        result = 31 * result + (activationType != null ? activationType.hashCode() : 0);
        result = 31 * result + (biasConstraintType != null ? biasConstraintType.hashCode() : 0);
        result = 31 * result + (kernelConstraintType != null ? kernelConstraintType.hashCode() : 0);
        result = 31 * result + (biasInitializerType != null ? biasInitializerType.hashCode() : 0);
        result = 31 * result + (kernelInitializerType != null ? kernelInitializerType.hashCode() : 0);
        result = 31 * result + (activityRegularizerType != null ? activityRegularizerType.hashCode() : 0);
        result = 31 * result + (biasRegularizerType != null ? biasRegularizerType.hashCode() : 0);
        result = 31 * result + (kernelRegularizerType != null ? kernelRegularizerType.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(filterNeurons.get(0).getKey());   //noch verbessern!
        return result;
    }

    @Override
    public String toString() {
        return "NeuralLayer{" +
                "name='" + name + '\'' +
                ", layerType=" + layerType +
                ", activationType=" + activationType +
                ", biasConstraintType=" + biasConstraintType +
                ", kernelConstraintType=" + kernelConstraintType +
                ", biasInitializerType=" + biasInitializerType +
                ", kernelInitializerType=" + kernelInitializerType +
                ", activityRegularizerType=" + activityRegularizerType +
                ", biasRegularizerType=" + biasRegularizerType +
                ", kernelRegularizerType=" + kernelRegularizerType +
                ", neurons=" + filterNeurons.toString() +
                ", map=" + config.toString() +
                '}';
    }
}
