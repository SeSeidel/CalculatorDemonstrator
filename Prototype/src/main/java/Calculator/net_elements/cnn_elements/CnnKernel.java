package Calculator.net_elements.cnn_elements;

import Calculator.net_elements.NeuralLayer;
import Calculator.net_elements.Neuron;
import org.json.JSONObject;
import java.util.ArrayList;

public abstract class CnnKernel {

    protected int[] outputSizes = {0, 0, 0};

    /**
     * @return die Länge des Convolutionsergebnisses in der jeweiligen Dimension
     *
     * @param index die Dimension der angefragten Länge (0 = x; 1 = y; 2 = z)
     */
    public int getOutputSize(int index) { return outputSizes[index]; }

    abstract public double getWeight(int x) throws WrongKernelDimensionException;
    abstract public double getWeight(int y, int x) throws WrongKernelDimensionException;
    abstract public double getWeight(int z, int y, int x) throws WrongKernelDimensionException;

    abstract public void setKernelWeights(double[] kernelWeights) throws WrongKernelDimensionException;
    abstract public void setKernelWeights(double[][] kernelWeights) throws WrongKernelDimensionException;
    abstract public void setKernelWeights(double[][][] kernelWeights) throws WrongKernelDimensionException;

    /**
     * Legt die Liste aller Filter an, welche als Array von Neuronen mit zugehörigem Bias und Verbindungen gespeichert werden, die aus der Anwendung des Kernels zum jeweiligen Filter resultieren!
     *
     * @param layer Conv-Schicht, dessen verschiedene Filter als Neuronen mit Verbindungen gesetzt werden sollen
     * @param layerConfig Konfigurationseintrag aus der JSON, der zu dieser Schicht gehört
     * @param biasArray Array über die jeweiligen Bias-Werte der einzelnen Filter-Kernels dieser Schicht
     * @param neuronWeights Eine Liste mit Arrays der jeweiligen Gewichte eines einzelnen, zu einem angewandten Kernel gehörigen Neurons
     */
    protected void createFiltersForConv (NeuralLayer layer, JSONObject layerConfig, double[] biasArray, ArrayList<double[]> neuronWeights){
        int filterNum = layerConfig.getInt("filters");
        for (int f = 0; f < filterNum; f++){
            Neuron[] neurons = new Neuron[neuronWeights.size()];
            for (int n = 0; n < neuronWeights.size(); n++){
                neurons[n] = new Neuron(biasArray[f], neuronWeights.get(n), f);
            }
            layer.addNeuronsForNewFilter(neurons);
        }
    }

    abstract public void setFilterNeuronsValidConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray);
    abstract public void setFilterNeuronsSameConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray);
    abstract public void setFilterNeuronsCausalConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray) throws WrongKernelDimensionException;
}