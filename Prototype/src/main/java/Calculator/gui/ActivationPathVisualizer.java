package Calculator.gui;

import Calculator.net_elements.NeuralLayer;
import Calculator.net_elements.Neuron;
import Calculator.element_types.LayerType;
import javafx.scene.control.Button;
import Calculator.net_elements.NeuralNet;
import java.util.ArrayList;
import java.util.Iterator;

public final class ActivationPathVisualizer {

    private static ArrayList<Integer> relInputNeurons = new ArrayList<>();
    private static ArrayList<Integer> relOutputNeurons = new ArrayList<>();

    public static void addToRelInputNeurons(Integer neuronNum) {
        if (!relInputNeurons.contains(neuronNum)) { relInputNeurons.add(neuronNum); }
        relInputNeurons.add(neuronNum);
    }
    public static void clearRelInputNeurons() { relInputNeurons.clear(); }

    public static ArrayList<Integer> getRelevantOutputNeurons() { return relOutputNeurons; }

    public static void setButtonColorTeal(Button button) { button.setStyle("-fx-background-color: linear-gradient(#ccffff, #00e6e6); -fx-background-radius: 3; -fx-border-color:darkgrey; -fx-border-width: 1 1 1 1; -fx-border-radius: 3;"); }
    public static void setButtonColorOrange(Button button) { button.setStyle("-fx-background-color: linear-gradient(#ffba00, #ff7b00); -fx-background-radius: 3; -fx-border-color:darkgrey; -fx-border-width: 1 1 1 1; -fx-border-radius: 3;"); }
    public static void resetButtonColor(Button button) { button.setStyle(""); }

    public static void visualizeFirstLayerPath() {
        LayerVisualizer.resetFirstLayerButtonVisuals();
        Iterator<Integer> relIterator = relInputNeurons.iterator();
        while (relIterator.hasNext()) {
            setButtonColorTeal(LayerVisualizer.getFirstLayerButton(relIterator.next()));
        }
    }
    public static void calculatePaths (NeuralNet net, double threshold) {
        clearRelInputNeurons();
        net.setRelevantNetWeights(threshold);
        visualizeFirstLayerPath();
    }

    private static void visualizeFirstLayerActivations(NeuralNet net) {
        for (int i = 0; i < net.getInputSize(); i++){
            if (relInputNeurons.contains(i) && net.getActInputValue(i) != 0) { setButtonColorOrange(LayerVisualizer.getFirstLayerButton(i)); }
        }
    }
    private static void hideFirstLayerActivations(NeuralNet net) {
        for (int i = 0; i < net.getInputSize(); i++){
            if (relInputNeurons.contains(i) && net.getActInputValue(i) != 0) { setButtonColorTeal(LayerVisualizer.getFirstLayerButton(i)); }
        }
    }
    private static void visualizeNonInputActivations(NeuralNet net, Boolean mark) {
        Neuron[] actFilter;
        for (NeuralLayer actLayer: net.getNeuralLayers()){
            if (!actLayer.getLayerType().equals(LayerType.CORE_FLATTEN)) {
                for (int i = 0; i < actLayer.getNumOfFilters(); i++){
                    actFilter = actLayer.getFilterNeurons(i);
                    for (Neuron actNeuron: actFilter) {
                        if (mark.equals(true)) { actNeuron.markRelatedButtonActive(); }
                        else { actNeuron.unmarkRelatedButtonActive(); }
                    }
                }
            }
        }
    }
    public static void markActivatedNeurons (NeuralNet net, Boolean mark) {
        if (mark.equals(true)) { visualizeFirstLayerActivations(net); }
        else { hideFirstLayerActivations(net); }
        visualizeNonInputActivations(net, mark);
    }


}
