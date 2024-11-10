package Calculator.net_elements.cnn_elements;

import Calculator.net_elements.NeuralLayer;
import java.util.Arrays;
import java.util.ArrayList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CnnKernel3D extends CnnKernel{
    private static final Logger logger = LoggerFactory.getLogger(CnnKernel2D.class);

    private double[][][] kernelWeights;

    public CnnKernel3D() {this.kernelWeights = null; }

    @Override
    public double getWeight(int z, int y, int x) { return kernelWeights[z][y][x]; }
    public double getWeight(int x) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 1-dimensional!"); }
    public double getWeight(int y, int x) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 2-dimensional!"); }

    @Override
    public void setKernelWeights(double[][][] kernelWeights) { this.kernelWeights = kernelWeights; }
    public void setKernelWeights(double[] kernelWeights) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 1-dimensional!"); }
    public void setKernelWeights(double[][] kernelWeights) throws WrongKernelDimensionException { throw new WrongKernelDimensionException("This Kernel is not 2-dimensional!"); }

    @Override
    public void setFilterNeuronsValidConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray) {     //not implemented!
        int xRange = layerConfig.getJSONArray("batch_input_shape").getInt(1);
        int yRange = layerConfig.getJSONArray("batch_input_shape").getInt(2);
        int zRange = layerConfig.getJSONArray("batch_input_shape").getInt(3);
        int xStride = layer.getStrides()[0];
        int yStride = layer.getStrides()[1];
        int zStride = layer.getStrides()[2];
        int xKernelLength = kernelWeights[0][0].length;
        int yKernelLength = kernelWeights[0].length;
        int zKernelLength = kernelWeights.length;
        ArrayList<double[]> neuronWeights = new ArrayList<>();                  // Anzahl der durchgeführten Convolutionen je Filter muss erst bestimmt werden

        for (int z = 0; z + zKernelLength - 1 < zRange; z = z + zStride) {      // bestimmt alle Eckpunkte für Übertragung
            for (int y = 0; y + yKernelLength - 1 < yRange; y = y + yStride) {  // der Kernelwerte in (3-dim) Basismatrix bei Valid-Padding
                for (int x = 0; x + xKernelLength - 1 < xRange; x = x + xStride) {
                    double[] weights = new double[xRange*yRange*zRange];
                    Arrays.fill(weights, 0.0);
                    for (int c = z; c < z + zKernelLength; c++) {               // legt die jeweiligen Neuronen-Verbindungen für die Kernelanwendung beim aktuellen Eckpunkt an
                        for (int b = y; b < y + yKernelLength; b++) {
                            for (int a = x; a < x + xKernelLength; a++) {
                                weights[c*xRange*yRange + b*xRange + a] = kernelWeights[c - z][b - y][a - x];
                            }
                        }
                    }
                    neuronWeights.add(weights);
                    outputSizes[0] = x;
                }
                outputSizes[1] = y;
            }
            outputSizes[2]++;
        }
        outputSizes[0]++; outputSizes[1]++;
        createFiltersForConv(layer, layerConfig, biasArray, neuronWeights);
    }

    @Override
    public void setFilterNeuronsSameConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray) {     //not implemented!
        int xRange = layerConfig.getJSONArray("batch_input_shape").getInt(1);
        int yRange = layerConfig.getJSONArray("batch_input_shape").getInt(2);
        int zRange = layerConfig.getJSONArray("batch_input_shape").getInt(3);
        int xStride = layer.getStrides()[0];
        int yStride = layer.getStrides()[1];
        int zStride = layer.getStrides()[2];
        int xKernelLength = kernelWeights[0][0].length;
        int yKernelLength = kernelWeights[0].length;
        int zKernelLength = kernelWeights.length;
        ArrayList<double[]> neuronWeights = new ArrayList<>();                  // Anzahl der durchgeführten Convolutionen je Filter muss erst bestimmt werden
        int üXleft = (xKernelLength - Math.max(xRange%xStride, 1)) / -2;        // benötigtes 0er-Padding nach links -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)
        int üYup = (yKernelLength - Math.max(yRange%yStride, 1)) / -2;          // benötigtes 0er-Padding nach oben -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)
        int üZbefore = (zKernelLength - Math.max(zRange%zStride, 1)) / -2;      // benötigtes 0er-Padding nach vorne -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)
        for (int z = üZbefore; z < zRange; z = z + zStride) {                   // bestimmt alle Eckpunkte für Übertragung
            for (int y = üYup; y < yRange; y = y + yStride) {                   // der Kernelwerte in (3-dim) Basismatrix bei Valid-Padding
                for (int x = üXleft; x < xRange; x = x + xStride) {
                    double[] weights = new double[xRange*yRange*zRange];
                    Arrays.fill(weights, 0.0);
                    for (int c = z; c < z + zKernelLength; c++) {
                        for (int b = y; b < y + yKernelLength; b++) {            // legt die jeweiligen Neuronen-Verbindungen für die Kernelanwendung beim aktuellen Eckpunkt an
                            for (int a = x; a < x + xKernelLength; a++) {
                                if (c > -1 && b > -1 && a > -1) { weights[c*xRange*yRange + b*xRange + a] = kernelWeights[c - z][b - y][a - x]; }  // Eintragung nur, wenn der Punkt keine neg. Koordinaten hat (entspräche mult. mit Padding-Werten -> wäre 0)
                            }
                        }
                    }
                    neuronWeights.add(weights);
                    outputSizes[0] = x;
                }
                outputSizes[1] = y;
            }
            outputSizes[2]++;
        }
        outputSizes[0]++; outputSizes[1]++;
        createFiltersForConv(layer, layerConfig, biasArray, neuronWeights);
    }

    @Override
    public void setFilterNeuronsCausalConv(NeuralLayer layer, JSONObject layerConfig, double[] biasArray)
            throws WrongKernelDimensionException { throw new WrongKernelDimensionException("Causal-padding is only an option for 1-dimensional convolution!"); }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (int z = 0; z < kernelWeights.length; z++)
            for (int y = 0; y < kernelWeights[0].length; y++)
                for (int x = 0; x < kernelWeights[0][0].length; x++)
                    ret.append(getWeight(z, y, x) + " | ");
        return ret.toString();
    }
}
