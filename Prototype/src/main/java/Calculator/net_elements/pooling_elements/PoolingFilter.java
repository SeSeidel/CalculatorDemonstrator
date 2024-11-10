package Calculator.net_elements.pooling_elements;

import Calculator.net_elements.NeuralLayer;
import Calculator.net_elements.Neuron;
import org.json.JSONObject;
import java.util.ArrayList;

public abstract class PoolingFilter {

    protected int[] poolingSizes = {0, 0, 0};
    protected int[] outputSizes = {0, 0, 0};

    /**
     * @return Längen des Poolingfilters. Erster Eintrag -> Länge in X-Dimension, zweiter Eintrag -> Länge in Y-Dimension, dritter Eintrag -> Länge in Z-Dimension.
     */
    public int[] getPoolingSizes() { return poolingSizes; }
    /**
     * @return die Länge des Poolingergebnisses in der jeweiligen Dimension
     *
     * @param index die Dimension der angefragten Länge (0 = x; 1 = y; 2 = z)
     */
    public int getOutputSize(int index) { return outputSizes[index]; }

    /**
     * Legt die Liste aller Filter an, welche als Array von Neuronen mit zugehörigem Bias und Verbindungen gespeichert werden, die aus der Anwendung des Kernels zum jeweiligen Filter resultieren!
     *
     * @param layer Conv-Schicht, dessen verschiedene Filter als Neuronen mit Verbindungen gesetzt werden sollen
     * @param filterNum Anzahl der Filter der relevanten Vorgängerschicht, die die Anzahl der Filter dieser Schicht bestimmt
     * @param neuronWeights Eine Liste mit Arrays der jeweiligen Gewichte eines einzelnen, zu einem angewandten Kernel gehörigen Neurons
     */
    protected void createFiltersForPool (NeuralLayer layer, int filterNum, ArrayList<double[]> neuronWeights){
        for (int f = 0; f < filterNum; f++){
            Neuron[] neurons = new Neuron[neuronWeights.size()];
            for (int n = 0; n < neuronWeights.size(); n++){
                neurons[n] = new Neuron(0.0, neuronWeights.get(n), f);
            }
            layer.addNeuronsForNewFilter(neurons);
        }
    }

    abstract public void setFilterNeuronsValidPooling(NeuralLayer layer, JSONObject layerConfig, NeuralLayer predecessor);
    abstract public void setFilterNeuronsSamePooling(NeuralLayer layer, JSONObject layerConfig, NeuralLayer predecessor);

}
