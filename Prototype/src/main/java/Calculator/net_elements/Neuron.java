package Calculator.net_elements;

import Calculator.element_types.ActivationType;
import Calculator.general.MapperService;
import Calculator.element_types.LayerType;
import Calculator.general.MathOps;
import Calculator.gui.ActivationPathVisualizer;
import Calculator.net_elements.activation_elements.ActivationMessage;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * Klasse zur Nachbildung eines künstlichen Neurons mit Bias bzw. Schwellwert und einzelnen Gewichten.
 */
public class Neuron {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);

    private final double bias;
    private final double[] weights;                                         // Gewichte zu den Vorgängerneuronen
    private ArrayList<Integer> relevantWeights = new ArrayList<>();
    private HashSet<Integer> relevantInputFields = new HashSet<>();
    private HashSet<Integer> connectedInputFields = new HashSet<>();
    private Button buttonLink;
    private double weightsSum = 0;
    private double minActivationFactor = 0;
    private double maxActivationFactor = 0;
    private double incomingActivationSum = 0;
    private Double activation = 0.0;
    private int filterNum;

    public int getFilterNum() {return filterNum; }

    /**
     * Konstruktor für ein Neuron
     *
     * @param bias    Bias bzw. Schwellwert des Neurons
     * @param weights Gewichte des Neurons als eindimensionales Array des primitiven Typs double
     */
    public Neuron(double bias, double[] weights, int filterNum) {
        this.bias = bias;
        this.weights = weights;
        this.filterNum = filterNum;
        for (int i=0; i < weights.length; i++){ weightsSum = weightsSum + Math.abs(weights[i]); }
    }

    /**
     * @return Bias bzw. Schwellwert des Neurons
     */
    public double getBias() {
        return bias;
    }

    /**
     * @return Gewichte des Neurons als eindimensionales Array des primitiven Typs double
     */
    public double[] getIncomingWeights() { return weights; }
    public ArrayList<Integer> getRelevantWeights() { return relevantWeights; }

    public double getWeightsSum() { return weightsSum; }
    public double getMinActivationFactor() { return minActivationFactor; }
    public double getMaxActivationFactor() { return maxActivationFactor; }
    public double getActivation() { return activation; }
    public HashSet<Integer> getRelevantInputFields() { return relevantInputFields; }
    public HashSet<Integer> getConnectedInputFields() { return connectedInputFields; }

    public void setButtonLink(Button button) { buttonLink = button; }
    public void markRelatedButtonRelevant() { ActivationPathVisualizer.setButtonColorTeal(buttonLink); }
    public void markRelatedButtonActive() { if (activation != 0.0) { ActivationPathVisualizer.setButtonColorOrange(buttonLink); }}
    public void unmarkRelatedButton() { ActivationPathVisualizer.resetButtonColor(buttonLink); }
    public void unmarkRelatedButtonActive() { if (activation != 0.0) { ActivationPathVisualizer.setButtonColorTeal(buttonLink); }}

    public String getNeuronButtonName(){
        return buttonLink.getText();
    }
    /**
     * Berechnet die bedingte Verbindungsstärke dieses Neurons anhand der Aktivierungen der Vorgängerschicht auf Basis derer bedingten Verbindungssstärken. Nur für Nicht-Pooling-Schichten
     *
     * @param minPredFactorActivations Die minimalen Aktivierungen der Vorgängerschicht auf Basis derer bedingten Verbindungssstärken.
     * @param maxPredFactorActivations Die maximalen Aktivierungen der Vorgängerschicht auf Basis derer bedingten Verbindungssstärken.
     */
    public void calcActivationFactorStandard(double[] minPredFactorActivations, double[] maxPredFactorActivations, ActivationType layerActType) {
        minActivationFactor = bias;
        maxActivationFactor = bias;
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] > 0) {
                minActivationFactor = minActivationFactor + (weights[i] * minPredFactorActivations[i]);
                maxActivationFactor = maxActivationFactor + (weights[i] * maxPredFactorActivations[i]);
            }
            else {
                minActivationFactor = minActivationFactor + (weights[i] * maxPredFactorActivations[i]);
                maxActivationFactor = maxActivationFactor + (weights[i] * minPredFactorActivations[i]);
            }}
        minActivationFactor = MathOps.calculateActivation(minActivationFactor, layerActType);
        maxActivationFactor = MathOps.calculateActivation(maxActivationFactor, layerActType);
    }

    /**
     * Berechnet die bedingte Verbindungsstärke dieses Neurons anhand der Aktivierungen der Vorgängerschicht auf Basis derer bedingten Verbindungssstärken. Nur für Pooling-Schichten.
     *
     * @param maxPredFactorActivations Die Aktivierungen der Vorgängerschicht auf Basis derer bedingten Verbindungssstärken.
     */
    public void calcActivationFactorMaxPooling(double[] minPredFactorActivations, double[] maxPredFactorActivations) {
        minActivationFactor = weights[0] * minPredFactorActivations[0];
        maxActivationFactor = weights[0] * maxPredFactorActivations[0];
        for (int i = 1; i < weights.length; i++){
            if (weights[i] != 0) {
                minActivationFactor = Math.max(minActivationFactor, weights[i] * minPredFactorActivations[i]);
                maxActivationFactor = Math.max(maxActivationFactor, weights[i] * maxPredFactorActivations[i]);
            }}
    }

    private void clearRelevantWeights(LayerType layerType) {
        relevantWeights.clear();
        try { unmarkRelatedButton(); }
        catch (NullPointerException e) {
            if (!layerType.equals(LayerType.CORE_FLATTEN)) { logger.error("Verarbeitungsfehler -> es wurde kein Button angelegt!"); }
        }
    }

    /**
     * Erfasst, gemessen an einem gegebenen Grenzwert, welche Verbindungen bei Berechnungen beachtet werden sollen. Nicht für Flattan-Schichten!
     *
     * @param threshold Der zu berücksichtigende Grenzwert.
     * @param isRelevantNeuron Gibt an ob dieses Neuron für tieferliegende Schichten relevant ist.
     * @param layerNum Gibt an, zur wievielten Schicht des Netzes dieses Neuron gehört.
     * @param layerType Der Typ der Schicht, zu der diese Neuron gehört.
     * @param maxPredFactorActivations
     */
    public void setRelevantNeuronWeights(double threshold, boolean isRelevantNeuron, int layerNum, LayerType layerType, double[] maxPredFactorActivations, double[] minPredFactorActivations) {
        clearRelevantWeights(layerType);
        if (isRelevantNeuron) {
            if (!layerType.equals(LayerType.CORE_FLATTEN)) {
                try { markRelatedButtonRelevant(); }
                catch (NullPointerException e) {
                    logger.error("Verarbeitungsfehler -> es wurde kein Button angelegt!");
                }}
            addRelevantWeights(threshold, layerNum, layerType, maxPredFactorActivations, minPredFactorActivations);
        }
    }

    /**
     * Hilfsfunktion für "setRelevantNeuronWeights" und "setRelevantNeuronWeightsFlatten". Enthält die Relevanzbedingungen.
     */
    private void addRelevantWeights(double treshold, int layerNum, LayerType layerType, double[] maxPredFactorActivations, double[] minPredFactorActivations) {
        double avgIncMacAct = 0;
        if (!layerType.equals(LayerType.CORE_FLATTEN)) {
            if (weights.length != maxPredFactorActivations.length || weights.length != minPredFactorActivations.length) {
                logger.error("Die Daten für Schicht " + layerNum + " sind korrupt!");
                System.exit(1);
            }
            else avgIncMacAct = calcAvgIncMaxAct(maxPredFactorActivations, minPredFactorActivations);
        }
        for (int i = 0; i < weights.length; i++) {
            if (Math.abs(weights[i]) > treshold * (weightsSum/weights.length)) {
                relevantWeights.add(i);
            }
            else if (!layerType.equals(LayerType.CORE_FLATTEN) && Math.max(Math.abs(weights[i] * maxPredFactorActivations[i]), Math.abs(weights[i] * minPredFactorActivations[i])) > treshold * avgIncMacAct){
                relevantWeights.add(i);
            }
            if (layerNum == 1) { ActivationPathVisualizer.addToRelInputNeurons(i); }
        }
    }
    private double calcAvgIncMaxAct(double[] maxPredFactorActivations, double[] minPredFactorActivations) {
        double result = 0;
        for (int i = 0; i < weights.length; i++) {
            result = result + Math.max(Math.abs(weights[i] * maxPredFactorActivations[i]), Math.abs(weights[i] * minPredFactorActivations[i]));
        }
        return result / weights.length;
    }
    /**
     * Hilfsfunktion für "setRelevantSoftMaxLayerWeights" der Klasse "NeuralLayer". Trägt den Index aller eingehenden Verbindungen, deren max. SoftMax-Differenz den geforderten relativen Grenzwert "treshold"
     * überschreitet, in die Liste der relavanten Eingangsverbindungen ein.
     *
     * @param treshold Der relative Vergleichswert für die jeweiligen SoftMax-Internen Maximaldifferenzen der Verbindungen. (Siehe T_SoftMax aus Kapitel 6.3.2)
     * @param neuronMaxDiffs Ein Array mit den jeweiligen SoftMax-Internen Maximaldifferenzen der Verbindungen. (Siehe Maximum aus B_i_j aus aus Kapitel 6.3.2)
     */
    public void addRelevantWeightsSoftMax(double treshold, double[] neuronMaxDiffs, LayerType layerType){
        clearRelevantWeights(layerType);
        for (int i = 0; i < neuronMaxDiffs.length; i++){
            if (neuronMaxDiffs[i] > treshold){
                relevantWeights.add(i);                         // Für eine SoftMax-Schicht sollte nie "(layerNum == 1)" zutreffen!
            }}
        if (!relevantWeights.isEmpty()) {                       // Markieren als relevantes Neuron wenn min 1 relevante Eingangsverbindung vorhanden ist.
            try { markRelatedButtonRelevant(); }
            catch (NullPointerException e) {
                logger.error("Verarbeitungsfehler -> es wurde kein Button angelegt!");
            }}
    }

    private void inputVecCheck (ActivationMessage[] predActMassages) {
        Boolean res = predActMassages.length == weights.length;
        if (!res) {
            logger.error("Verarbeitungsfehler bei Aktivierungsberechnung von Neuron " + toString() + " -> Anzahl Eingangsaktivierungen und Eingangsverbindungen nicht deckungsgleich!");
            System.exit(1);
        }
    }
    private void inputVecCheck2 (double[] predActVals) {
        Boolean res = predActVals.length == weights.length;
        if (!res) {
            logger.error("Verarbeitungsfehler bei Aktivierungsberechnung von Neuron " + toString() + " -> Anzahl Eingangsaktivierungen und Eingangsverbindungen nicht deckungsgleich!");
            System.exit(1);
        }
    }

    public ActivationMessage calculateStandardNeuronActivation(ActivationMessage[] predActMessages, ActivationType actType){ // Berechnet die Gesamtaktivierung des Neurons anhand seiner relevanten Gewichte für Standard-FF-Schichten.
        inputVecCheck(predActMessages);
        relevantInputFields.clear();
        connectedInputFields.clear();
        double inSum = bias;
        double tempVal;
        for (int i = 0; i < relevantWeights.size(); i++) {
            tempVal = predActMessages[relevantWeights.get(i)].getActivation()*getRelevantWeight(i);
            inSum = inSum + tempVal;
            if (tempVal != 0.0){ noteRelevantInputFieldsFromPred(predActMessages[relevantWeights.get(i)].getRelInputsForActivation()); } // Nur die Eingänge betrachten, über die Aktivierungen laufen.
            noteConnectedInputFieldsFromPred(predActMessages[relevantWeights.get(i)].getConInputsForActivation());
        }
        incomingActivationSum = inSum;
        activation = MathOps.calculateActivation(inSum, actType);
        if (activation == 0.0) { relevantInputFields.clear(); } // Aktivierungen bis hierher werden nur für Ergebnis relevant, wenn auch Aktivierung weitergeleitet wird. Tlw Dopplung mit For-Schleife.
        return new ActivationMessage(activation, relevantInputFields, connectedInputFields);
    }
    public double calculateStanAndConvNeuronInputSum(double[] predActVals){ // Unter Nutzung Minimalfunktionalität entsprechend zu calculateStandardNeuronActivation.
        inputVecCheck2(predActVals);
        double inSum = 0;
        double tempVal;
        for (int i = 0; i < relevantWeights.size(); i++) {
            tempVal = predActVals[relevantWeights.get(i)]*getRelevantWeight(i);
            inSum = inSum + tempVal;
        }
        return inSum;
    }
    public ActivationMessage calculateMaxPoolingNeuronActivation(ActivationMessage[] predActMessages) { // Berechnet die Gesamtaktivierung des Neurons anhand seiner relevanten Gewichte für Max-Pooling-Schichten.
        inputVecCheck(predActMessages);
        relevantInputFields.clear();
        connectedInputFields.clear();
        double inMax = bias;
        incomingActivationSum = inMax;
        double tempVal;
        for (int i = 0; i < relevantWeights.size(); i++) {
            tempVal = predActMessages[relevantWeights.get(i)].getActivation()*getRelevantWeight(i);
            inMax = Math.max(inMax, tempVal);
            incomingActivationSum = incomingActivationSum + tempVal;
            if (tempVal != 0.0){ noteRelevantInputFieldsFromPred(predActMessages[relevantWeights.get(i)].getRelInputsForActivation()); } // Nur die Eingänge betrachten, über die Aktivierungen laufen.
            noteConnectedInputFieldsFromPred(predActMessages[relevantWeights.get(i)].getConInputsForActivation());
        }
        activation = inMax;         // Keine gesonderte Betrachtung der Aktivierungsfunktion nach dem Pooling notwendig.
        if (activation == 0.0) { relevantInputFields.clear(); } // Aktivierungen bis hierher werden nur für Ergebnis relevant, wenn auch Aktivierung weitergeleitet wird. Tlw Dopplung mit For-Schleife.
        return new ActivationMessage(activation, relevantInputFields, connectedInputFields);
    }
    public double calculateMaxPoolNeuronInputSum(double[] predActVals){ // Unter Nutzung Minimalfunktionalität entsprechend zu calculateStandardNeuronActivation.
        inputVecCheck2(predActVals);
        double inMax = 0;
        double tempVal;
        for (int i = 0; i < relevantWeights.size(); i++) {
            tempVal = predActVals[relevantWeights.get(i)]*getRelevantWeight(i);
            inMax = Math.max(inMax, tempVal);
        }
        return inMax;
        // return inMax;
    }

    public double setSoftMaxActivation(double layerInputSum) {
        activation = activation / layerInputSum;
        return activation;
    }
    private void noteRelevantInputFieldsFromPred(HashSet<Integer> predNeuronsRelInputFields) {
        for (Integer relIndex: predNeuronsRelInputFields) { relevantInputFields.add(relIndex); }
    }
    private void noteConnectedInputFieldsFromPred(HashSet<Integer> predNeuronsConInputFields) {
        for (Integer relIndex: predNeuronsConInputFields) { connectedInputFields.add(relIndex); }
    }

    /**
     * Gibt an, ob die Verbindung des Neurons zu einem spez. Vorgängerneuron als relevant gilt.
     *
     * @param predNeuronNum die Position des Vorgängerneurons in seinem jeweiligen Filter.
     */
    public boolean hasRelevantWeigthToPredecessor(int predNeuronNum) { return relevantWeights.contains(predNeuronNum); }

    /**
     * Gibt den zugehörigen Gewichtswert der angegebenen relevanten gewichteten Verbindung zurück.
     */
    public double getRelevantWeight(int weightNumb) {return weights[relevantWeights.get(weightNumb)];}

    /**
     * Gibt ein HashSet mit den Indizes aller Verbindungen zurück, über die eine relevante Aktivierung in das Neuron einging.
     */
    public ArrayList<Integer> getRelevantActivationConnectionIndices(double[] neuronInput, double thresholdFaktor) {
        ArrayList<Integer> out = new ArrayList<>();
        for (int i = 0; i < relevantWeights.size(); i++){
            if (Math.abs(neuronInput[relevantWeights.get(i)]*getRelevantWeight(i)) >= Math.abs(thresholdFaktor)) { // thresholdFactor = Durchschnittseingangsaktivierung Schicht * Sensitivität
                out.add(relevantWeights.get(i));
            }
        }
        Collections.sort(out);
        return out;
    }

    public double getCurAvgIncActivationForNeuron(){ return (incomingActivationSum/weights.length); }
    public double getIncomingActivationSum() { return incomingActivationSum; }

    @Override
    public boolean equals(Object o) {       //Für saubere Datanstruktur ggf. noch layerNum einarbeiten - Sollte jedoch so gut wie nie nötig sein wenn die Gewichte UND der Bias abgefragt werden! (Lediglich für Pooling, daher Filternummer)
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neuron neuron = (Neuron) o;
        if (neuron.filterNum != this.filterNum) return false;
        if (neuron.bias != this.bias) return false;
        return Arrays.equals(this.weights, neuron.weights);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(bias);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(weights);
        return result;
    }

    @Override
    public String toString() {
        return "Neuron{" +
                "bias=" + bias +
                ", weights=" + Arrays.toString(weights) +
                '}';
    }
}
