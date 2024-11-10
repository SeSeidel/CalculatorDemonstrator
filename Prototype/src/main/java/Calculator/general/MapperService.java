package Calculator.general;

import com.fasterxml.jackson.databind.ObjectMapper;
import Calculator.net_elements.*;
import Calculator.net_elements.cnn_elements.WrongKernelDimensionException;
import Calculator.element_types.*;
import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service zum mappen eines aus Keras exportieren .h5 Models.
 */
public class MapperService {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);

    private final ObjectMapper mapper;
    private HdfFile hdfFile;
    private JSONArray layers;
    private int inputSize, units;

    /**
     * Konstruktor zur Klasse MapperService.
     */
    public MapperService() {
        mapper = new ObjectMapper();
    }

    /**
     * Extrahiert bereits oberflächliche Architekturdaten des Models und speichert diese für effizientere
     * Zugriffszeiten beim späteren Mapping.
     *
     * @param path Pfad zur exportierten .h5 Datei
     * @return das importierte Keras Model als NeuralNet
     */
    public NeuralNet getNeuralNet(String path) {
        File file = new File(path);
        hdfFile = new HdfFile(file);

        // extrahiere JSON aus h5 file Attribut model_config
        JSONObject modelConfig = new JSONObject(hdfFile.getAttribute("model_config").getData().toString());

        // extrahiere Schichten
        JSONObject config = modelConfig.getJSONObject("config");
        layers = config.getJSONArray("layers");

        // extrahiere architekturinformation aus batch_input_shape
        try {
            JSONArray batch_input_shape = layers.getJSONObject(0).getJSONObject("config").getJSONArray("batch_input_shape");
            inputSize = batch_input_shape.getInt(1);
        } catch (JSONException e) {
            inputSize = 0;
        }
        units = 0;
        return new NeuralNet(getLayers());
    }

    /**
     * Für jedes Layer im JSONArray layers wird ein NeuralLayer erstellt und dessen Attribute extrahiert und gesetzt.
     *
     * @return ArrayList von NeuralLayers mit allen Schichten des Models
     */
    private List<NeuralLayer> getLayers() {
        ArrayList<NeuralLayer> layersForNet = new ArrayList<>();

        for (int l = 0; l < layers.length(); l++) {
            NeuralLayer layer = new NeuralLayer();
            setLayerAttributes(layer, l, layersForNet);
            layersForNet.add(layer);
        }

        return layersForNet;
    }

    /**
     * Ruft alle Methoden zum Setzen der Attribute einer Schicht für diese auf.
     *
     * @param layer Schicht, dessen Attribute gesetzt werden sollen
     * @param index Index dieser Schicht im exportierten JSONArray layers
     * @param layerArrayList Liste der bereits gespeicherten Vorgängerschichten
     */
    private void setLayerAttributes(NeuralLayer layer, int index, ArrayList<NeuralLayer> layerArrayList) {
        JSONObject layerConfig = layers.getJSONObject(index).getJSONObject("config");
        String layerType = layers.getJSONObject(index).getString("class_name");

        setLayerName(layer, layerConfig);
        logger.info(">-- " + layerType + "-Layer '" + layer.getName() + "' wird eingelesen: --<");

        setLayerType(layer, index);
        setActivationType(layer, layerConfig);
        setBiasConstraintType(layer, layerConfig);
        setKernelConstraintType(layer, layerConfig);
        setBiasInitializerType(layer, layerConfig);
        setKernelInitializerType(layer, layerConfig);
        setActivityRegularizerType(layer, layerConfig);
        setBiasRegularizerType(layer, layerConfig);
        setKernelRegularizerType(layer, layerConfig);
        setPadding(layer, layerConfig);
        setPoolSize(layer, layerConfig);
        setStrides(layer, layerConfig);
        setNeurons(layer, layerConfig, layerType, index, layerArrayList);
        setConfig(layer, layerConfig);

        if (index == 0){
            try {
                JSONArray batch_input_shape = layers.getJSONObject(index).getJSONObject("config").getJSONArray("batch_input_shape");
                InputLoader.setInputWidthAndHeight(batch_input_shape.getInt(1), batch_input_shape.getInt(2));
            } catch (JSONException e) {
                InputLoader.setInputWidthAndHeight(0, 0);
            }
        }
    }

    /**
     * Extrahiert und setzt den Namen der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setLayerName(NeuralLayer layer,
                              JSONObject layerConfig) {
        try {
            layer.setName(layerConfig.getString("name"));
        } catch (JSONException e) {
            logger.info("Layer hat keinen Namen!");
        }
    }

    /**
     * Extrahiert und setzt den LayerType der Schicht, falls vorhanden.
     *
     * @param layer Schicht, dessen Attribute gesetzt werden sollen
     * @param index Index dieser Schicht im exportierten JSONArray layers
     */
    private void setLayerType(NeuralLayer layer,
                              int index) {
        try {
            layer.setLayerType(identifyLayerType(layers.getJSONObject(index).getString("class_name")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Typ!");
        }
    }

    /**
     * Extrahiert und setzt den ActivationType der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setActivationType(NeuralLayer layer,
                                   JSONObject layerConfig) {
        try {
            layer.setActivationType(identifyActivationType(layerConfig.getString("activation")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Aktivierungstyp!");
        }
    }

    /**
     * Extrahiert und setzt den BiasConstraintType der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setBiasConstraintType(NeuralLayer layer,
                                       JSONObject layerConfig) {
        try {
            JSONObject bias_constraint = layerConfig.getJSONObject("bias_constraint");
            layer.setBiasConstraintType(identifyConstraintType(bias_constraint.getString("class_name")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Bias-Constrainttyp!");
        }
    }

    /**
     * Extrahiert und setzt den KernelConstraintType der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setKernelConstraintType(NeuralLayer layer,
                                         JSONObject layerConfig) {
        try {
            JSONObject kernel_constraint = layerConfig.getJSONObject("kernel_constraint");
            layer.setKernelConstraintType(identifyConstraintType(kernel_constraint.getString("class_name")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Kernel-Constrainttyp!");
        }
    }

    /**
     * Extrahiert und setzt den BiasInitializerType der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setBiasInitializerType(NeuralLayer layer,
                                        JSONObject layerConfig) {
        try {
            JSONObject bias_initializer = layerConfig.getJSONObject("bias_initializer");
            layer.setBiasInitializerType(identifyInitializerType(bias_initializer.getString("class_name")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Bias-Initialisierungstyp!");
        }
    }

    /**
     * Extrahiert und setzt den KernelInitializerType der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setKernelInitializerType(NeuralLayer layer,
                                          JSONObject layerConfig) {
        try {
            JSONObject kernel_initializer = layerConfig.getJSONObject("kernel_initializer");
            layer.setKernelInitializerType(identifyInitializerType(kernel_initializer.getString("class_name")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Kernel-Initialisierungstyp!");
        }
    }

    /**
     * Extrahiert und setzt den ActivityRegularizerType der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setActivityRegularizerType(NeuralLayer layer,
                                            JSONObject layerConfig) {
        try {
            JSONObject activity_regularizer = layerConfig.getJSONObject("activity_regularizer");
            layer.setActivityRegularizerType(identifyRegularizerType(activity_regularizer.getString("class_name")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Activity-Regulierungstyp!");
        }
    }

    /**
     * Extrahiert und setzt den BiasRegularizerType der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setBiasRegularizerType(NeuralLayer layer,
                                        JSONObject layerConfig) {
        try {
            JSONObject bias_regularizer = layerConfig.getJSONObject("bias_regularizer");
            layer.setBiasRegularizerType(identifyRegularizerType(bias_regularizer.getString("class_name")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Bias-Regulierungstyp!");
        }
    }

    /**
     * Extrahiert und setzt den KernelRegularizerType der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setKernelRegularizerType(NeuralLayer layer, JSONObject layerConfig) {
        try {
            JSONObject kernel_regularizer = layerConfig.getJSONObject("kernel_regularizer");
            layer.setKernelRegularizerType(identifyRegularizerType(kernel_regularizer.getString("class_name")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Kernel-Regulierungstyp!");
        }
    }

    /**
     * Extrahiert und setzt den Kernel der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     * @param layerType   String aus der JSON, der den Typ der Schicht angibt.
     */
    private void setKernel(NeuralLayer layer, JSONObject layerConfig, String layerType) {                               // Für quadratische Kernel ausgelegt --> prüfen ob hinreichend!
        try {
            layer.initializeKernel(layerType);
            Dataset kernelDataset = hdfFile.getDatasetByPath("/model_weights/" + layer.getName() + "/" + layer.getName() + "/kernel:0");
            Object kernelData = kernelDataset.getData();
            int kernelSize0 = layerConfig.getJSONArray("kernel_size").getInt(0);
            if (layerType.contains("1D")) {
                double[] kernelWeights = new double[kernelSize0];
                for (int x = 0; x < kernelSize0; x++)
                    kernelWeights[x] = ((Number) Array.get(Array.get(kernelData, x), 0)).doubleValue();           // Zeile nochmal PRÜFEN !!!
                layer.setKernelWeights(kernelWeights);
            }
            else {
                int kernelSize1 = layerConfig.getJSONArray("kernel_size").getInt(1);
                if (layerType.contains("2D")) {
                    double[][] kernelWeights = new double[kernelSize1][kernelSize0];
                    for (int y = 0; y < kernelSize1; y++)
                        for (int x = 0; x < kernelSize0; x++)
                            kernelWeights[y][x] = ((Number) Array.get(Array.get(Array.get(Array.get(kernelData, y), x), 0), 0)).doubleValue();
                    layer.setKernelWeights(kernelWeights);
                }
                if (layerType.contains("3D")) {
                    int kernelSize2 = layerConfig.getJSONArray("kernel_size").getInt(2);
                    double[][][] kernelWeights = new double[kernelSize0][kernelSize0][kernelSize0];
                    for (int z = 0; z < kernelSize2; z++)
                        for (int y = 0; y < kernelSize1; y++)
                            for (int x = 0; x < kernelSize0; x++)
                                kernelWeights[z][y][x] = ((Number) Array.get(Array.get(Array.get(Array.get(Array.get(Array.get(kernelData, z), y), x), 0), 0), 0)).doubleValue();
                    layer.setKernelWeights(kernelWeights);
                }
            }
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keinen Kernel!");
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            logger.error("Daten scheinen korrupt zu sein!");
        }
    }

    /**
     * Extrahiert und setzt den PaddingType der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setPadding(NeuralLayer layer, JSONObject layerConfig) {
        try {
            layer.setPadding(identifyPaddingType(layerConfig.getString("padding")));
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat kein Padding!");
        }
    }

    /**
     * Extrahiert und setzt die Strides der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setStrides(NeuralLayer layer, JSONObject layerConfig) {
        try {
            int[] layerStrides = new int[layerConfig.getJSONArray("strides").length()];
            for (int i = 0; i < layerStrides.length; i++) {
                layerStrides[i] = layerConfig.getJSONArray("strides").getInt(i);
            }
            layer.setStrides(layerStrides);
        }  catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keine Strides!");
        }
    }

    /**
     * Extrahiert und setzt die Strides der Schicht, falls vorhanden.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setPoolSize(NeuralLayer layer, JSONObject layerConfig) {
        try {
            int[] layerPoolingSizes = new int[layerConfig.getJSONArray("pool_size").length()];
            for (int i = 0; i < layerPoolingSizes.length; i++) {
                layerPoolingSizes[i] = layerConfig.getJSONArray("pool_size").getInt(i);
            }
            layer.setPoolingFilter(layerPoolingSizes);
        }  catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat kein Pooling!");
        }
    }

    /**
     * Extrahiert und setzt die Neuronen und Verbindungen(inkl.Bias und Gewichte) der Schicht, falls vorhanden (setzt diese als einzigen Filter bei z.B. Dense).
     * Wenn nicht vorhanden werden die Neuronen und Verbindungen(inkl. Bias und Gewichte) berechnet, die der Operation der jeweiligen Schicht entsprechen würden, wenn möglich.
     * Dient der bereitstellung von direkten Feedforward-Pfaden zur Rückrechnung.
     *
     * @param layer             Schicht, deren Attribute gesetzt werden sollen
     * @param layerConfig       config dieser Schicht aus der exportierten JSON
     * @param layerType         der Wert des Tags class_name aus der JSON für die aktuelle Schicht
     * @param index             Index der aktuellen Schicht
     * @param predecessorList   Liste der bereits gespeicherten Vorgängerschichten
     */
    private void setNeurons(NeuralLayer layer, JSONObject layerConfig, String layerType, int index, ArrayList<NeuralLayer> predecessorList) {
        if (layerType.contains("Dense")) {setDenseNeurons(layer, layerConfig);}
        else {
            logger.info("Layer " + layer.getName() + " hat keine Neuronen!");
            if (!layerType.contains("Conv")) {
                logger.info("Layer " + layer.getName() + " hat keine Kernel-Filter!");
                if (layerType.contains("Pooling")){setPoolingNeurons(layer, layerConfig, index, predecessorList);}
                else if (layerType.contains("Flatten")){setFlattenNeurons(layer, index, predecessorList); }
            }
            else {setKernelNeurons(layer, layerConfig, layerType);     }
        }
    }
    private void setDenseNeurons (NeuralLayer layer, JSONObject layerConfig) { //Hilfsmethode für setNeurons(...)
        try {
            units = layerConfig.getInt("units");
            double[] bias = extractBiasArray(layer, units);

            Dataset kernelDataset = hdfFile.getDatasetByPath("/model_weights/" + layer.getName() + "/" + layer.getName() + "/kernel:0");
            Object kernelData = kernelDataset.getData();
            double[][] kernel = new double[inputSize][units];
            for (int r = 0; r < inputSize; r++)
                for (int c = 0; c < units; c++)
                    kernel[r][c] = ((Number) Array.get(Array.get(kernelData, r), c)).doubleValue();

            Neuron[] neurons = new Neuron[units];
            for (int u = 0; u < units; u++) {
                double[] weights = new double[inputSize];
                for (int r = 0; r < inputSize; r++)
                    weights[r] = kernel[r][u];
                neurons[u] = new Neuron(bias[u], weights, 0);
            }
            layer.addNeuronsForNewFilter(neurons);

            inputSize = units;
        } catch (JSONException e) {
            logger.info("Layer " + layer.getName() + " hat keine Neuronen!");
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            logger.error("Daten scheinen korrupt zu sein!");
        }
    }
    private double[] extractBiasArray(NeuralLayer layer, int arrayLength) { //Hilfsmethode für setDenseNeurons(...) und setKernelNeurons(...)
        Object biasData = hdfFile.getDatasetByPath("/model_weights/" + layer.getName() + "/" + layer.getName() + "/bias:0").getData();
        double[] biasArray = new double[arrayLength];
        for (int b = 0; b < arrayLength; b++) {
            biasArray[b] = ((Number) Array.get(biasData, b)).doubleValue();
        }
        return biasArray;
    }
    private void setKernelNeurons (NeuralLayer layer, JSONObject layerConfig, String layerType) { //Hilfsmethode für setNeurons(...)
        setKernel(layer, layerConfig, layerType);
        double[] biasArray = extractBiasArray(layer, layerConfig.getInt("filters"));
        // Valid-Padding -> kein 0er-Rahmen
        if (layerConfig.getString("padding").contains("valid")) { layer.getCnnKernel().setFilterNeuronsValidConv(layer, layerConfig, biasArray); }
        // Same-Padding -> Auffüllen der Eingangsmatrix mit gleichverteiltem 0er-Rahmen
        else if (layerConfig.getString("padding").contains("same")) { layer.getCnnKernel().setFilterNeuronsSameConv(layer, layerConfig, biasArray); }
        // Casual-Padding -> Auffüllen der Eingangsmatrix mit führendem 0er-Rahmen
        else if (layerConfig.getString("padding").contains("causal")) {
            try{ layer.getCnnKernel().setFilterNeuronsCausalConv(layer, layerConfig, biasArray); }
            catch (WrongKernelDimensionException e) { logger.error(e.getMessage()); }
        }
    }
    private void setPoolingNeurons (NeuralLayer layer, JSONObject layerConfig, int index, ArrayList<NeuralLayer> predecessorList) { //Hilfsmethode für setNeurons(...)
        if (layerConfig.getString("padding").contains("valid")) {
            try {
                layer.getPooling().setFilterNeuronsValidPooling(layer, layerConfig, predecessorList.get(index - 1));
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error("Daten scheinen korrupt zu sein!");
            }
        }
        else if (layerConfig.getString("padding").contains("same")){
            try {
                layer.getPooling().setFilterNeuronsSamePooling(layer, layerConfig, predecessorList.get(index - 1));
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error("Daten scheinen korrupt zu sein!");
            }
        }
        else {logger.error("Daten scheinen korrupt zu sein!");}
    }
    private void setFlattenNeurons (NeuralLayer layer, int index, ArrayList<NeuralLayer> predecessorList) { //Hilfsmethode für setNeurons(...)
        try {
            int neuronsSize = 0;
            for (int i = 0; i < predecessorList.get(index-1).getNumOfFilters(); i++){
                neuronsSize = neuronsSize + predecessorList.get(index-1).getFilterNeurons(i).length;
            }
            Neuron[] neurons = new Neuron[neuronsSize];
            int neuronCnt = 0;
            for (int i = 0; i < predecessorList.get(index-1).getNumOfFilters(); i++){
                for (int j = 0; j < predecessorList.get(index-1).getFilterNeurons(i).length; j++) {
                    double[] weights = new double[neuronsSize];
                    Arrays.fill(weights, 0.0); weights[neuronCnt] = 1.0;
                    neurons[neuronCnt] = new Neuron(0.0, weights,0);
                    neuronCnt++;
                }
            }
            layer.addNeuronsForNewFilter(neurons);
            inputSize = neuronsSize;
        }
        catch (ArrayIndexOutOfBoundsException e) { logger.error("Daten scheinen korrupt zu sein!"); }
    }

    /**
     * Extrahiert und setzt zusätzliche spezifische Schichtenkonfiguration.
     *
     * @param layer       Schicht, dessen Attribute gesetzt werden sollen
     * @param layerConfig config dieser Schicht aus der exportierten JSON
     */
    private void setConfig(NeuralLayer layer,
                           JSONObject layerConfig) {
        try {
            layer.setConfig(mapper.readValue(layerConfig.toString(), Map.class));
        } catch (IOException e) {
            logger.warn("Layer " + layer.getName() + " hat keine Konfiguration!");
        }
    }

    /**
     * Identifiziert den ActivationType anhand des aus dem Model extrahierten class_name
     *
     * @param class_name aus Keras Model exportierter class_name
     * @return gemappter ActivationType
     */
    private ActivationType identifyActivationType(String class_name) {
        for (ActivationType activationType : ActivationType.values())
            if (activationType.getKerasName().equals(class_name))
                return activationType;
        return ActivationType.NONE;
    }

    /**
     * Identifiziert den ConstraintType anhand des aus dem Model extrahierten class_name
     *
     * @param class_name aus Keras Model exportierter class_name
     * @return gemappter ConstraintType
     */
    private ConstraintType identifyConstraintType(String class_name) {
        for (ConstraintType constraintType : ConstraintType.values())
            if (constraintType.getKerasName().equals(class_name))
                return constraintType;
        return ConstraintType.NONE;
    }

    /**
     * Identifiziert den InitializerType anhand des aus dem Model extrahierten class_name
     *
     * @param class_name aus Keras Model exportierter class_name
     * @return gemappter InitializerType
     */
    private InitializerType identifyInitializerType(String class_name) {
        for (InitializerType initializerType : InitializerType.values())
            if (initializerType.getKerasName().equals(class_name))
                return initializerType;
        return InitializerType.NONE;
    }

    /**
     * Identifiziert den RegularizerType anhand des aus dem Model extrahierten class_name
     *
     * @param class_name aus Keras Model exportierter class_name
     * @return gemappter RegularizerType
     */
    private RegularizerType identifyRegularizerType(String class_name) {
        for (RegularizerType regularizerType : RegularizerType.values())
            if (regularizerType.getKerasName().equals(class_name))
                return regularizerType;
        return RegularizerType.NONE;
    }

    /**
     * Identifiziert den LayerType anhand des aus dem Model extrahierten class_name
     *
     * @param class_name aus Keras Model exportierter class_name
     * @return gemappter LayerType
     */
    private LayerType identifyLayerType(String class_name) {
        for (LayerType layerType : LayerType.values())
            if (layerType.getKerasName().equals(class_name))
                return layerType;
        return LayerType.NONE;
    }

    private PaddingType identifyPaddingType(String padding_name) {
        for (PaddingType paddingType : PaddingType.values())
            if (paddingType.getKerasName().equals(padding_name))
                return paddingType;
        return PaddingType.NONE;
    }
}