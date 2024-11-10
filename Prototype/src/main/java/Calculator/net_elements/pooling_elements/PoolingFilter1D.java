package Calculator.net_elements.pooling_elements;

import Calculator.net_elements.NeuralLayer;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;

public class PoolingFilter1D extends PoolingFilter{

    public PoolingFilter1D(int x) { this.poolingSizes[0] = x; }

    public void setFilterNeuronsValidPooling(NeuralLayer layer, JSONObject layerConfig, NeuralLayer predecessor) {   //Noch nicht getestet! --> JUnit-Tests ergänzen!
        int xRange = predecessor.getCnnKernel().getOutputSize(0);
        int xStride = layer.getStrides()[0];
        ArrayList<double[]> neuronWeights = new ArrayList<>();
        for (int x = 0; x + poolingSizes[0] - 1 < xRange; x = x + xStride) {                    // bestimmt alle Eckpunkte für die Erstellung
            double[] weights = new double[xRange];                                      // von Verbindungen größer 0 um über diese Verbindungen zu poolen
            Arrays.fill(weights, 0.0);
            for (int a = x; a < x+poolingSizes[0]; a++) {                               // legt die jeweiligen Neuronen-Verbindungen für die Poolinganwendung beim aktuellen Eckpunkt an
                if (a < xRange) { weights[a] = 1; }
            }
            neuronWeights.add(weights);
            outputSizes[0]++;
        }
        createFiltersForPool(layer, predecessor.getNumOfFilters(), neuronWeights);
    }

    public void setFilterNeuronsSamePooling(NeuralLayer layer, JSONObject layerConfig, NeuralLayer predecessor) { // nochmal prüfen Anhand der Quellen
        int xRange = predecessor.getCnnKernel().getOutputSize(0);
        int xStride = layer.getStrides()[0];
        ArrayList<double[]> neuronWeights = new ArrayList<>();
        int üXleft = (poolingSizes[0] - Math.max(xRange%xStride, 1)) / -2;        // benötigtes 0er-Padding nach links -> aus Koordinatenraum rausrechnen (da Mult-Erg mit Gewichten = 0)

        for (int x = üXleft; x < xRange; x = x + xStride){                        // bestimmt alle Eckpunkte für Übertragung der Kernelwerte in (1-dim) Basismatrix bei Valid-Padding
            double[] weights = new double[xRange];
            Arrays.fill(weights, 0.0);
            for (int a = x; a < poolingSizes[0]; a++) {                           // legt die jeweiligen Neuronen-Verbindungen für die Kernelanwendung beim aktuellen Eckpunkt an
                if (a > -1) { weights[a] = 1; }                                   // Eintragung nur, wenn der Punkt keine neg. Koordinaten hat (entspräche mult. mit Padding-Werten -> wäre 0)
            }
            neuronWeights.add(weights);
            outputSizes[0]++;
        }
        createFiltersForPool(layer, predecessor.getNumOfFilters(), neuronWeights);
    }
}
