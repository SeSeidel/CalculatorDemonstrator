package Calculator.net_elements.pooling_elements;

import Calculator.net_elements.NeuralLayer;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;

public class PoolingFilter3D extends PoolingFilter{

    public PoolingFilter3D(int x, int y, int z) { this.poolingSizes[0] = x; this.poolingSizes[1] = y; this.poolingSizes[2] = z; }

    public void setFilterNeuronsValidPooling(NeuralLayer layer, JSONObject layerConfig, NeuralLayer predecessor) {   //Noch nicht getestet! --> JUnit-Tests ergänzen!
        int xRange = predecessor.getCnnKernel().getOutputSize(0);
        int yRange = predecessor.getCnnKernel().getOutputSize(1);
        int zRange = predecessor.getCnnKernel().getOutputSize(2);
        int xStride = layer.getStrides()[0];
        int yStride = layer.getStrides()[1];
        int zStride = layer.getStrides()[2];
        ArrayList<double[]> neuronWeights = new ArrayList<>();
        for (int z = 0; z + poolingSizes[2] - 1 < zRange; z = z + zStride) {                        // bestimmt alle Eckpunkte für die Erstellung
            for (int y = 0; y + poolingSizes[1] - 1 < yRange; y = y + yStride) {                    // von Verbindungen größer 0 um über diese Verbindungen zu poolen
                for (int x = 0; x + poolingSizes[0] -1 < xRange; x = x +xStride) {
                    double[] weights = new double[xRange*yRange*zRange];
                    Arrays.fill(weights, 0.0);
                    for (int c = z; c < z+poolingSizes[2]; c++) {                           // legt die jeweiligen Neuronen-Verbindungen für die Poolinganwendung beim aktuellen Eckpunkt an
                        for (int b = y; b < y+poolingSizes[1]; b++) {
                            for (int a = x; a < x+poolingSizes[0]; a++) {
                                if (c < zRange && b < yRange && a < xRange) { weights[c*xRange*yRange + b*xRange + a] = 1; }
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
        createFiltersForPool(layer, predecessor.getNumOfFilters(), neuronWeights);
    }

    public void setFilterNeuronsSamePooling(NeuralLayer layer, JSONObject layerConfig, NeuralLayer predecessor) {
        int xRange = predecessor.getCnnKernel().getOutputSize(0);
        int yRange = predecessor.getCnnKernel().getOutputSize(1);
        int zRange = predecessor.getCnnKernel().getOutputSize(2);
        int xStride = layer.getStrides()[0];
        int yStride = layer.getStrides()[1];
        int zStride = layer.getStrides()[2];
        ArrayList<double[]> neuronWeights = new ArrayList<>();
        int üXleft = (poolingSizes[0] - Math.max(xRange%xStride, 1)) / -2;        // benötigtes 0er-Padding nach links -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)
        int üYup = (poolingSizes[1] - Math.max(yRange%yStride, 1)) / -2;          // benötigtes 0er-Padding nach oben -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)
        int üZbefore = (poolingSizes[2] - Math.max(zRange%zStride, 1)) / -2;      // benötigtes 0er-Padding nach vorne -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)
        for (int z = üZbefore; z < zRange; z = z + zStride) {                     // bestimmt alle Eckpunkte für Übertragung
            for (int y = üYup; y < yRange; y = y + yStride) {                     // der Kernelwerte in (3-dim) Basismatrix bei Valid-Padding
                for (int x = üXleft; x < xRange; x = x + xStride) {
                    double[] weights = new double[xRange*yRange*zRange];
                    Arrays.fill(weights, 0.0);
                    for (int c = z; c < z + poolingSizes[2]; c++) {
                        for (int b = y; b < y + poolingSizes[1]; b++) {            // legt die jeweiligen Neuronen-Verbindungen für die Kernelanwendung beim aktuellen Eckpunkt an
                            for (int a = x; a < x + poolingSizes[0]; a++) {
                                if (c > -1 && b > -1 && a > -1) { weights[c*xRange*yRange + b*xRange + a] = 1; }  // Eintragung nur, wenn der Punkt keine neg. Koordinaten hat (entspräche mult. mit Padding-Werten -> wäre 0)
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
        createFiltersForPool(layer, predecessor.getNumOfFilters(), neuronWeights);
    }
}
