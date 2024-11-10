package Calculator.net_elements.cnn_elements;

import Calculator.net_elements.NeuralLayer;
import java.util.Arrays;
import java.util.ArrayList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CnnKernel2D extends CnnKernel {
    private static final Logger logger = LoggerFactory.getLogger(CnnKernel2D.class);

    private double[][] kernelWeights;

    public CnnKernel2D() {this.kernelWeights = null; }

    @Override
    public double getWeight(int y, int x) { return kernelWeights[y][x]; }
    public double getWeight(int x) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 1-dimensional!"); }
    public double getWeight(int z, int y, int x) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 3-dimensional!"); }

    @Override
    public void setKernelWeights(double[][] kernelWeights) { this.kernelWeights = kernelWeights; }
    public void setKernelWeights(double[] kernelWeights) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 1-dimensional!"); }
    public void setKernelWeights(double[][][] kernelWeights) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 3-dimensional!"); }

    @Override
    public void setFilterNeuronsValidConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray) {    // geht davon aus, das Convolution Reihe für Reihe (also x-Richtung zuerst) abgearbeitet wird
        int xRange = layerConfig.getJSONArray("batch_input_shape").getInt(1);
        int yRange = layerConfig.getJSONArray("batch_input_shape").getInt(2);
        int xStride = layer.getStrides()[0];
        int yStride = layer.getStrides()[1];
        int xKernelLength = kernelWeights[0].length;                            // erst mit y-Achse alle Zeilen
        int yKernelLength = kernelWeights.length;                               // dann mit x-Achse in den Zeilen die Einträge
        ArrayList<double[]> neuronWeights = new ArrayList<>();                  // Anzahl der durchgeführten Convolutionen je Filter muss erst bestimmt werden

        for (int y = 0; y + yKernelLength - 1 < yRange; y = y + yStride) {      // bestimmt alle Eckpunkte für Übertragung
            for (int x = 0; x + xKernelLength - 1 < xRange; x = x + xStride) {  // der Kernelwerte in (2-dim) Basismatrix bei Valid-Padding
                double[] weights = new double[xRange*yRange];
                Arrays.fill(weights, 0.0);
                for (int b = y; b < y+yKernelLength; b++){                      // legt die jeweiligen Neuronen-Verbindungen für die Kernelanwendung beim aktuellen Eckpunkt an
                    for (int a = x; a < x+xKernelLength; a++){
                        weights[b*xRange + a] = kernelWeights[b-y][a-x];
                    }
                }
                neuronWeights.add(weights);
                outputSizes[0] = x;
            }
            outputSizes[1]++;
        }
        outputSizes[0]++;
        createFiltersForConv(layer, layerConfig, biasArray, neuronWeights);
    }

    @Override // noch zu testen!
    public void setFilterNeuronsSameConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray) {   // geht davon aus, das Convolution Reihe für Reihe (also x-Richtung zuerst) abgearbeitet wird
        int xRange = layerConfig.getJSONArray("batch_input_shape").getInt(1);
        int yRange = layerConfig.getJSONArray("batch_input_shape").getInt(2);
        int xStride = layer.getStrides()[0];
        int yStride = layer.getStrides()[1];
        int xKernelLength = kernelWeights[0].length;
        int yKernelLength = kernelWeights.length;
        ArrayList<double[]> neuronWeights = new ArrayList<>();                  // Anzahl der durchgeführten Convolutionen je Filter muss erst bestimmt werden
        int üXleft = (xKernelLength - Math.max(xRange%xStride, 1)) / -2;        // benötigtes 0er-Padding nach links -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)
        int üYup = (yKernelLength - Math.max(yRange%yStride, 1)) / -2;          // benötigtes 0er-Padding nach oben -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)

        for (int y = üYup; y < yRange; y = y + yStride) {                       // bestimmt alle Eckpunkte für Übertragung
            for (int x = üXleft; x < xRange; x = x + xStride){                  // der Kernelwerte in (2-dim) Basismatrix bei Valid-Padding
                double[] weights = new double[xRange*yRange];
                Arrays.fill(weights, 0.0);
                for (int b = y; b < y+yKernelLength; b++) {                     // legt die jeweiligen Neuronen-Verbindungen für die Kernelanwendung beim aktuellen Eckpunkt an
                    for (int a = x; a < x+xKernelLength; a++) {
                        if (b > -1 && a > -1) { weights[b*xRange + a] = kernelWeights[b-y][a-x]; }  // Eintragung nur, wenn der Punkt keine neg. Koordinaten hat (entspräche mult. mit Padding-Werten -> wäre 0)
                    }
                }
                neuronWeights.add(weights);
                outputSizes[0] = x;
            }
            outputSizes[1]++;
        }
        outputSizes[0]++;
        createFiltersForConv(layer, layerConfig, biasArray, neuronWeights);
    }

    @Override
    public void setFilterNeuronsCausalConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray)
            throws WrongKernelDimensionException { throw new WrongKernelDimensionException("Causal-padding is only an option for 1-dimensional convolution!"); }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (int y = 0; y < kernelWeights.length; y++)
            for (int x = 0; x < kernelWeights[0].length; x++)
                ret.append(getWeight(y, x) + " | ");
        return ret.toString();
    }
}