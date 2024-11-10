package Calculator.general;

import Calculator.element_types.ActivationType;
import Calculator.net_elements.NeuralLayer;
import Calculator.net_elements.Neuron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public final class MathOps {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);

    public static DecimalFormat roundWeights = new DecimalFormat("#0.0000");
    public static DecimalFormat roundOutput = new DecimalFormat("#0.0");

    /**
     * Ruft anhand des gegebenen ActivationType die zugeordnete numerische Verarbeitung des Eingabeparameters auf. Die lineare Aktivierungsfunktion wird direkt hier abgebildet.
     *
     * @param param Der mit der jeweiligen Funktion zu verarbeitende numerische Wert.
     * @param type Die Art der Funktion, mit der der numerische Werte verarbeitet werden soll.
     */
    public static double calculateActivation(double param, ActivationType type) {
        if (type == ActivationType.RELU) { return reLu(param); }
        if (type == ActivationType.SIGMOID) { return sigmoid(param); }
        if (type == ActivationType.SOFTPLUS) { return softPlus(param); }
        if (type == ActivationType.SOFTSIGN) { return softSign(param); }
        if (type == ActivationType.TANH) { return tanH(param); }
        if (type == ActivationType.SELU) { return seLu(param); }
        if (type == ActivationType.ELU) { return eLu(param); }
        if (type == ActivationType.EXPONENTIAL) { return exponential(param); }
        if (type == ActivationType.HARD_SIGMOID) { return hardSigmoid(param); }
        if (type == ActivationType.SWISH) { return swish(param); }
        if (type == ActivationType.SOFTMAX) { return exponential(param); }      // Per Definition Summation und anschließende Normierung nach Anwendung der Exponentialfunktion.
        else { return param; }                                          // Entspricht den Typen LINEAR und NONE! - für LINEAR https://www.tensorflow.org/api_docs/python/tf/keras/activations/linear
    }

    /**
     * Bildet die Rectified Linear Unit Funktion aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/relu ab.
     *
     * @param param Der mit der Rectified Linear Unit Funktion zu verarbeitende numerische Wert.
     */
    private static double reLu(double param) {                          // double alpha = 0.0;
        return Math.max(param, 0);
    }
    /**
     * Bildet die Sigmoidfunktion aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/sigmoid ab.
     *
     * @param param Der mit der Sigmoidfunktion zu verarbeitende numerische Wert.
     */
    private static double sigmoid(double param) {
        return 1 / (1 + Math.exp(-param));
    }
    /**
     * Bildet die SoftPlus-Funktion aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/softplus ab.
     *
     * @param param Der mit der SoftPlus-Funktion zu verarbeitende numerische Wert.
     */
    private static double softPlus(double param) {
        return Math.log(Math.exp(param) + 1);
    }
    /**
     * Bildet die SoftSign-Funktion aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/softsign ab.
     *
     * @param param Der mit der SoftSign-Funktion zu verarbeitende numerische Wert.
     */
    private static double softSign(double param) {
        return param / (Math.abs(param) + 1);
    }
    /**
     * Bildet die Tangens Hyperbolicus Funktion aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/tanh ab.
     *
     * @param param Der mit der Tangens Hyperbolicus Funktion zu verarbeitende numerische Wert.
     */
    private static double tanH(double param) {
        return Math.tanh(param);
    }
    /**
     * Bildet die Scaled Exponential Linear Unit aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/selu ab.
     *
     * @param param Der mit der Scaled Exponential Linear Unit zu verarbeitende numerische Wert.
     */
    private static double seLu(double param) {                          // mit double alpha = 1.67326324; double scale = 1.05070098;
        if (param > 0) { return 1.05070098 * param; }
        else { return 1.67326324 * 1.05070098 * (Math.exp(param) - 1); }
    }
    /**
     * Bildet die Funktion Exponential Linear Unit aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/elu ab.
     *
     * @param param Der mit der Funktion Exponential Linear Unit zu verarbeitende numerische Wert.
     */
    private static double eLu(double param) {                           // double alpha = 1.0;
        if (param > 0) { return param; }
        else { return Math.exp(param) - 1; }
    }
    /**
     * Bildet die Exponentialfunktion aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/exponential ab.
     *
     * @param param Der mit der Exponentialfunktion zu verarbeitende numerische Wert.
     */
    private static double exponential(double param) {
        return Math.exp(param);
    }
    /**
     * Bildet die vereinfachte Sigmoidfunktion aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/hard_sigmoid ab.
     *
     * @param param Der mit der vereinfachten Sigmoidfunktion zu verarbeitende numerische Wert.
     */
    private static double hardSigmoid(double param) {
        if (param < -2.5) { return 0.0; }
        else if (param > 2.5) { return 1.0; }
        else { return 0.2 * param + 0.5; }
    }
    /**
     * Bildet die Swish-Funktion aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/swish ab.
     *
     * @param param Der mit der vereinfachten Swish-Funktion zu verarbeitende numerische Wert.
     */
    private static double swish(double param) {
        return param * sigmoid(param);
    }
    /**
     * Bildet die SoftMax-Funktion aus TensorFlow/Keras gem. https://www.tensorflow.org/api_docs/python/tf/keras/activations/softmax ab.
     *
     * @param neurons Der Vektor mit den Neuronen der SoftMax-Schicht.
     */
    public static double[] calculateSoftMaxActivations(Neuron[] neurons){
        double[] out = new double[neurons.length];
        double sum = 0.0;
        for (Neuron n: neurons) {sum += n.getActivation(); }
        for (int i = 0; i < out.length; i++) {
            out[i] = neurons[i].setSoftMaxActivation(sum);
        }
        return out;
    }

    public static double[] interpretOutput (NeuralLayer layer, int layerNum){
        if (layer.getNumOfFilters()!=1) logger.error("Layer" + layerNum + " has more than one filter and should therefor not be an output-layer!");
        Neuron[] filterNeurons = layer.getFilterNeurons(0);
        int length = filterNeurons.length;
        double[] out = new double[length];
        if (layer.getActivationType().equals(ActivationType.SOFTMAX)){  // Umwandlung der Werte in Prozentangaben
            for (int i = 0; i < length; i++) out[i] = filterNeurons[i].getActivation()*100;
        }
        else logger.error("No interpretation in alignment with the activation function of layer" + layerNum + " implemented, yet!");
        return out;
    }

}
