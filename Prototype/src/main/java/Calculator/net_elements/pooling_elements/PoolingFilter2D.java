package Calculator.net_elements.pooling_elements;

import Calculator.net_elements.NeuralLayer;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONObject;

public class PoolingFilter2D extends PoolingFilter {

    public PoolingFilter2D(int x, int y) { this.poolingSizes[0] = x; this.poolingSizes[1] = y; }

    public void setFilterNeuronsValidPooling(NeuralLayer layer, JSONObject layerConfig, NeuralLayer predecessor) {
        int xRange = predecessor.getCnnKernel().getOutputSize(0);
        int yRange = predecessor.getCnnKernel().getOutputSize(1);
        int xStride = layer.getStrides()[0];
        int yStride = layer.getStrides()[1];
        ArrayList<double[]> neuronWeights = new ArrayList<>();
        for (int y = 0; y + poolingSizes[1] - 1 < yRange; y = y + yStride) {            // bestimmt alle Eckpunkte für die Erstellung
            for (int x = 0; x + poolingSizes[0] - 1 < xRange; x = x + xStride) {        // von Verbindungen größer 0 um über diese Verbindungen zu poolen
                double[] weights = new double[xRange*yRange];
                Arrays.fill(weights, 0.0);
                for (int b = y; b < y+poolingSizes[1]; b++) {                           // legt die jeweiligen Neuronen-Verbindungen für die Poolinganwendung beim aktuellen Eckpunkt an
                    for (int a = x; a < x+poolingSizes[0]; a++) {
                        if (b < yRange && a < xRange) { weights[b*xRange + a] = 1; }
                    }
                }
                neuronWeights.add(weights);
                outputSizes[0] = x;
            }
            outputSizes[1]++;
        }
        outputSizes[0]++;
        createFiltersForPool(layer, predecessor.getNumOfFilters(), neuronWeights);
    }

    public void setFilterNeuronsSamePooling(NeuralLayer layer, JSONObject layerConfig, NeuralLayer predecessor) { // nochmal prüfen Anhand der Quellen
        int xRange = predecessor.getCnnKernel().getOutputSize(0);
        int yRange = predecessor.getCnnKernel().getOutputSize(1);
        int xStride = layer.getStrides()[0];
        int yStride = layer.getStrides()[1];
        ArrayList<double[]> neuronWeights = new ArrayList<>();
        int üXleft = (poolingSizes[0] - Math.max(xRange%xStride, 1)) / -2;        // benötigtes 0er-Padding nach links -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)
        int üYup = (poolingSizes[1] - Math.max(yRange%yStride, 1)) / -2;          // benötigtes 0er-Padding nach oben -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)

        for (int y = üYup; y < yRange; y = y + yStride) {                         // bestimmt alle Eckpunkte für Übertragung
            for (int x = üXleft; x < xRange; x = x + xStride){                    // der Kernelwerte in (2-dim) Basismatrix bei Valid-Padding
                double[] weights = new double[xRange*yRange];
                Arrays.fill(weights, 0.0);
                for (int b = y; b < y+poolingSizes[1]; b++) {                     // legt die jeweiligen Neuronen-Verbindungen für die Kernelanwendung beim aktuellen Eckpunkt an
                    for (int a = x; a < x+poolingSizes[0]; a++) {
                        if (b > -1 && a > -1) { weights[b*xRange + a] = 1; }      // Eintragung nur, wenn der Punkt keine neg. Koordinaten hat (entspräche mult. mit Padding-Werten -> wäre 0)
                    }
                }
                neuronWeights.add(weights);
                outputSizes[0] = x;
            }
            outputSizes[1]++;
        }
        outputSizes[0]++;
        createFiltersForPool(layer, predecessor.getNumOfFilters(), neuronWeights);
    }
}