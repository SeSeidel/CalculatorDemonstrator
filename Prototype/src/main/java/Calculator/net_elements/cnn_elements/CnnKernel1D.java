package Calculator.net_elements.cnn_elements;

import Calculator.net_elements.NeuralLayer;
import java.util.Arrays;
import java.util.ArrayList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CnnKernel1D extends CnnKernel{
    private static final Logger logger = LoggerFactory.getLogger(CnnKernel2D.class);

    private double[] kernelWeights;

    public CnnKernel1D() {this.kernelWeights = null; }

    @Override
    public double getWeight(int x) { return kernelWeights[x]; }
    public double getWeight(int y, int x) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 2-dimensional!"); }
    public double getWeight(int z, int y, int x) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 3-dimensional!"); }

    @Override
    public void setKernelWeights(double[] kernelWeights) { this.kernelWeights = kernelWeights; }
    public void setKernelWeights(double[][] kernelWeights) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 2-dimensional!"); }
    public void setKernelWeights(double[][][] kernelWeights) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 3-dimensional!"); }

    @Override
    public void setFilterNeuronsValidConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray) {     //Noch nicht getestet! --> JUnit-Tests ergänzen!
        int xRange = layerConfig.getJSONArray("batch_input_shape").getInt(1);
        int xStride = layer.getStrides()[0];
        int xKernelLength = kernelWeights.length;
        ArrayList<double[]> neuronWeights = new ArrayList<>();                  // Anzahl der durchgeführten Convolutionen je Filter muss erst bestimmt werden

        for (int x = 0; x + xKernelLength - 1 < xRange; x = x + xStride) {      // bestimmt alle Eckpunkte für Übertragung der Kernelwerte in (1-dim) Basismatrix bei Valid-Padding
            double[] weights = new double[xRange];
            Arrays.fill(weights, 0.0);
            for (int a = x; a < x+xKernelLength; a++){                           // legt die jeweiligen Neuronen-Verbindungen für die Kernelanwendung beim aktuellen Eckpunkt an
                weights[a] = kernelWeights[a-x];
            }
            neuronWeights.add(weights);
            outputSizes[0]++;
        }
        createFiltersForConv(layer, layerConfig, biasArray, neuronWeights);
    }

    @Override
    public void setFilterNeuronsSameConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray) {     //Noch nicht getestet! --> JUnit-Tests ergänzen!
        int xRange = layerConfig.getJSONArray("batch_input_shape").getInt(1);
        int xStride = layer.getStrides()[0];
        int xKernelLength = kernelWeights.length;
        ArrayList<double[]> neuronWeights = new ArrayList<>();                  // Anzahl der durchgeführten Convolutionen je Filter muss erst bestimmt werden
        int üXleft = (xKernelLength - Math.max(xRange%xStride, 1)) / -2;        // benötigtes 0er-Padding nach links -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)

        for (int x = üXleft; x < xRange; x = x + xStride){                      // bestimmt alle Eckpunkte für Übertragung der Kernelwerte in (1-dim) Basismatrix bei Valid-Padding
            double[] weights = new double[xRange];
            Arrays.fill(weights, 0.0);
            for (int a = x; a < x+xKernelLength; a++) {                         // legt die jeweiligen Neuronen-Verbindungen für die Kernelanwendung beim aktuellen Eckpunkt an
                if (a > -1) { weights[a] = kernelWeights[a-x]; }                // Eintragung nur, wenn der Punkt keine neg. Koordinaten hat (entspräche mult. mit Padding-Werten -> wäre 0)
            }
            neuronWeights.add(weights);
            outputSizes[0]++;
        }
        createFiltersForConv(layer, layerConfig, biasArray, neuronWeights);
    }

    @Override
    public void setFilterNeuronsCausalConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray) {}    //not implemented!

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (int x = 0; x < kernelWeights.length; x++)
            ret.append(getWeight(x) + " | ");
        return ret.toString();
    }
}
