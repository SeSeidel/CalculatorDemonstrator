package Calculator.gui;

import Calculator.general.MapperService;
import Calculator.element_types.LayerType;
import Calculator.general.MathOps;
import Calculator.net_elements.NeuralLayer;
import Calculator.net_elements.NeuralNet;
import Calculator.net_elements.Neuron;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Ellipse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Iterator;

public final class LayerVisualizer {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);

    private static ArrayList<Button> firstLayerButtons = new ArrayList<>();


    public static Button getFirstLayerButton(int buttonNum) { return firstLayerButtons.get(buttonNum); }
    /**
     * Setzt die Darstellung der Buttons der Eingabeschicht auf die Standardeinstellungen zurück.
     */
    public static void resetFirstLayerButtonVisuals() {
        Iterator<Button> iterator = firstLayerButtons.iterator();
        while (iterator.hasNext()) {
            ActivationPathVisualizer.resetButtonColor(iterator.next());
        }
    }
    /**
     * Veranlasst die korrekte Darstellung einer versteckten Neuronenschicht im Anzeigefenster für das extrahierte Neuronale Netz..
     *
     * @param layerNum Der Index der Schicht, die dargestellt werden soll, in ihrem neuronalen Netz.
     * @param grid Das zugehörige(Grid)Pane aus der Gui, in dem die Darstellung realisiert wird.
     * @param layer Die darzustellende Neuronenschicht.
     */
    public static void visualizeHiddenLayer(int layerNum, GridPane grid, NeuralLayer layer){
        ComboBox box = new ComboBox();
        grid.add(createComboFields(layerNum, layer, box, layerWinComboEvent(layer, box)),0, layerNum);
        grid.add(layer.getChosenfilterVisualizerContainer(), 1, layerNum);
        if (layer.getLayerType().equals(LayerType.CORE_ACTIVATION)){ visualizeActivationLayer(layerNum, layer); }
        else if (layer.getLayerType().equals(LayerType.CORE_DROPOUT)){ visualizeDropoutLayer(layerNum, layer); }
        else if (layer.getLayerType().equals(LayerType.CORE_DENSE)){ visualizeDenseLayer(layerNum, layer); }
        else if (layer.getLayerType().equals(LayerType.CORE_FLATTEN)){ visualizeFlattenLayer(layerNum, layer); }
        else if (layer.getLayerType().equals(LayerType.CONVOLUTION_CONV_1D) || layer.getLayerType().equals(LayerType.CONVOLUTION_CONV_2D) || layer.getLayerType().equals(LayerType.CONVOLUTION_CONV_3D)) {
            visualizeMultiFilterLayer(layerNum, layer);
        }
        else if (layer.getLayerType().equals(LayerType.POOLING_MAX_1D) || layer.getLayerType().equals(LayerType.POOLING_MAX_2D) || layer.getLayerType().equals(LayerType.POOLING_MAX_3D)) {
            visualizeMultiFilterLayer(layerNum, layer);
        } // to be completed
    }
    /**
     * Erzeugt ein GridPane mit einem Dropdown-Menü, in dem die einzelnen Filter einer angegebenen Schicht zur Darstellung ausgewählt werden können, als Knoten bei den Koordinaten (0, 0).
     *
     * @param layerNum Der Index der Schicht, die dargestellt werden soll, in ihrem neuronalen Netz.
     * @param layer Die darzustellende Neuronenschicht.
     * @param box Die ComboBox, mit der das Dropdown-Menü angelegt werden soll.
     * @param handler Der EventHandler, der den Wechsel der Anzeige steuert.
     * @return Das Gridpane mit den betreffenden Dropdown-Menü bei der Koordinate (0, 0).
     */
    private static GridPane createComboFields(int layerNum, NeuralLayer layer, ComboBox box, EventHandler handler) {
        GridPane comboBoxGrid = new GridPane();
        Label comboBoxLabel= new Label();
        comboBoxGrid.add(comboBoxLabel, 0,0);
        for (int i = 0; i < layer.getNumOfFilters(); i++) {
            box.getItems().add("Filter " + i );
        }
        EventHandler boxEvent = handler;
        box.setOnAction(boxEvent);
        box.getSelectionModel().selectFirst();
        comboBoxLabel.setText(layer.getLayerType().getKerasName() + "Layer " + layerNum + ":");
        comboBoxGrid.add(box,0,1);
        return comboBoxGrid;
    }
    /**
     * Êrzeugt einen EventHandler, der das Umschalten der Anzeige von Filtern einer Schicht im Schichtfenster mit Hilfe eines Dropdown-Menüs steuert.
     *
     * @param layer Die Neuronenschicht, deren einzelne Filter dargestellt werden sollen.
     * @param box Die ComboBox, welche das verwendete DropDown-Menü darstellt.
     * @return Der Eventhändler zur Auswahl der Darstellung einzelner Filter der betreffenden Schicht im Schichtfenster.
     */
    private static EventHandler layerWinComboEvent(NeuralLayer layer, ComboBox box){
        EventHandler boxEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switchFilterVisualizer(layer, box.getSelectionModel().getSelectedIndex());
            }
        };
        return boxEvent;
    }
    /**
     * Wechselt die aktuelle Darstellung eines Filters der gewählten Schicht im Schichtfenster auf einen gewählten Filter dieser Schicht um.
     *
     * @param layer Die ausgewählten Schicht, deren Filter angezeigt werden.
     * @param index Der Index des Filters der Schicht, der dargestellt werden soll.
     */
    private static void switchFilterVisualizer(NeuralLayer layer, int index) {
        layer.setChosenFilterVisualizer(layer.getFilterNeuronVisuals(index));
    }
    /**
     * Êrzeugt einen EventHandler, der das Umschalten der Anzeige von Filtern einer Schicht im Gewichtsfenster mit Hilfe eines Dropdown-Menüs steuert. Nur für Flatten-Schichten, die über dem Bezugsneuron des Gewichtsfensters liegen.
     * Effektiv werden die Gewichte angezeigt, die zur betreffenden Flatten-schicht führen, kombiniert mit der Anzeige der Neuronen jener Schicht über der Flattenschicht.
     *
     * @param layerNum Der Index der Schicht, deren Filter dargestellt werden sollen, in ihrem neuronalen Netz.
     * @param box Die ComboBox, mit der das Dropdown-Menü angelegt werden soll.
     * @param neuronNum Der Index des Bezugsneurons der Gewichtsfensteranzeige in seiner jeweiligen Schicht.
     * @param pane Das GridPane, in dem die zum gewählten Filter zugeordneten Gewichte und Neuronen dargestellt werden sollen.
     * @return Der Eventhändler zur Auswahl der Darstellung einzelner gewählter Filter.
     */
    private static EventHandler weightsWinComboEventTopFlatten(int layerNum, ComboBox box, int neuronNum, GridPane pane){
        EventHandler boxEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switchWeightWinVisualizerTopFlatten(layerNum, box, neuronNum, pane);
            }
        };
        return boxEvent;
    }
    private static void switchWeightWinVisualizerTopFlatten(int layerNum, ComboBox box, int neuronNum, GridPane pane) {     //Anm.: setzt voraus, dass alle Filter gleiche Länge haben (ist bei TF akt. so) -> ggf. noch erweitern.
        pane.getChildren().clear();
        double[] weights = MainMenu.getActualNet().getNeuralLayers().get(layerNum+2).getFilterNeurons(0)[neuronNum].getIncomingWeights();
        int filterIndex = box.getSelectionModel().getSelectedIndex();
        int filterLenght = MainMenu.getActualNet().getNeuralLayers().get(layerNum).getFilterNeurons(filterIndex).length;
        double[] outgoing = new double[filterLenght];
        int cnt = 0;
        for (int i = filterIndex*filterLenght; i < (filterIndex+1)*filterLenght; i++) {
            outgoing[cnt] = weights[i];
            cnt++;
        }
        drawWWSingleFilterTopFlatten(layerNum, 0, filterIndex, filterLenght, pane, MainMenu.getActualNet().getNeuralLayers().get(layerNum+2).getFilterNeurons(0)[neuronNum]); // fokussiertes Neuron ist das Neuron unter der Flatten-Schicht, die nur einen Filter hat
        drawWWinWeights(outgoing, 1, pane);
    }
    /**
     * Êrzeugt einen EventHandler, der das Umschalten der Anzeige von Filtern einer Schicht im Gewichtsfenster mit Hilfe eines Dropdown-Menüs steuert. Nur für Schichten mit mehreren Filtern, die über dem Bezugsneuron des Gewichtsfensters liegen und keine Flatten-Schichten sind.
     *
     * @param layerNum Der Index der Schicht, deren Filter dargestellt werden sollen, in ihrem neuronalen Netz.
     * @param box Die ComboBox, mit der das Dropdown-Menü angelegt werden soll
     * @param neuronNum Der Index des Bezugsneurons der Gewichtsfensteranzeige in seiner jeweiligen Schicht.
     * @param pane Das GridPane, in dem die zum gewählten Filter zugeordneten Gewichte und Neuronen dargestellt werden sollen.
     * @return Der Eventhändler zur Auswahl der Darstellung einzelner gewählter Filter.
     */
    private static EventHandler weightsWinComboEventTop(int layerNum, ComboBox box, int neuronNum, GridPane pane){
        EventHandler boxEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switchWeightWinVisualizerTopFlatten(layerNum, box, neuronNum, pane);
            }
        };
        return boxEvent;
    }
    private static void switchWeightWinVisualizerTop(int layerNum, ComboBox box, int neuronNum, GridPane pane) {
        pane.getChildren().clear();
        int filterIndex = box.getSelectionModel().getSelectedIndex();
        double[] outgoing = MainMenu.getActualNet().outgoingWeights(layerNum-1, neuronNum, filterIndex);
        drawWWSingleFilterTop(layerNum, 0, box.getSelectionModel().getSelectedIndex(), pane, MainMenu.getActualNet().getNeuralLayers().get(layerNum).getFilterNeurons(filterIndex)[neuronNum]);
        drawWWinWeights(outgoing, 1, pane);
    }
    /**
     * Êrzeugt einen EventHandler, der das Umschalten der Anzeige von Filtern einer Schicht im Gewichtsfenster mit Hilfe eines Dropdown-Menüs steuert. Nur für Schichten mit mehreren Filtern, die unter dem Bezugsneuron des Gewichtsfensters liegen.
     *
     * @param layerNum Der Index der Schicht, deren Filter dargestellt werden sollen, in ihrem neuronalen Netz.
     * @param box Die ComboBox, mit der das Dropdown-Menü angelegt werden soll
     * @param neuronNum Der Index des Bezugsneurons der Gewichtsfensteranzeige in seiner jeweiligen Schicht.
     * @param pane Das GridPane, in dem die zum gewählten Filter zugeordneten Gewichte und Neuronen dargestellt werden sollen.
     * @return Der Eventhändler zur Auswahl der Darstellung einzelner gewählter Filter.
     */
    private static EventHandler weightsWinComboEventBottom(int layerNum, ComboBox box, int neuronNum, GridPane pane, Boolean neuronActive){
        EventHandler boxEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switchWeightWinVisualizerBottom(layerNum, box, neuronNum, pane, neuronActive);
            }
        };
        return boxEvent;
    }
    private static void switchWeightWinVisualizerBottom(int layerNum, ComboBox box, int neuronNum, GridPane pane, Boolean neuronActive) {
        pane.getChildren().clear();
        int filterIndex = box.getSelectionModel().getSelectedIndex();
        double[] outgoing = MainMenu.getActualNet().outgoingWeights(layerNum-1, neuronNum, filterIndex);
        drawWWinWeights(outgoing, 0, pane);
        drawWWSingleFilterBottom(layerNum, 1, box.getSelectionModel().getSelectedIndex(), pane, neuronNum, neuronActive);
    }

    public static void visualizeInputLayer (GridPane grid, NeuralLayer successorLayer){
        grid.add(new Label("Input Layer: "),0, 0);
        GridPane secondaryGrid = LayerVisualizer.getNewFilterGrid();
        grid.add(secondaryGrid,1,0);
        Button newButton = null;
        for (Integer neuronNum = 0; neuronNum < successorLayer.getFilterNeurons(0)[0].getIncomingWeights().length; neuronNum++){
            newButton = visualizeNewCommonLayerNeuron(newInputNeuron(), createNeuronName(0, 0, neuronNum), 0, neuronNum, 0);
            secondaryGrid.add(newButton,neuronNum+1,0);
            firstLayerButtons.add(neuronNum, newButton);
        }
    }
    private static Neuron newInputNeuron(){ // tbd.
        double[] weights = new double[1];
        return new Neuron(0.0, weights,0);
    }

    private static void visualizeActivationLayer(int layerNumb, NeuralLayer layer){
    }
    private static void visualizeDropoutLayer(int layerNumb, NeuralLayer layer){
    }
    private static void visualizeDenseLayer(int layerNumb, NeuralLayer layer){
        for (int neuronNum=0; neuronNum < layer.getFilterNeurons(0).length; neuronNum++){
            layer.getFilterNeuronVisuals(0).add(visualizeNewCommonLayerNeuron(layer.getFilterNeurons(0)[neuronNum], createNeuronName(layerNumb, 0, neuronNum), layerNumb, neuronNum, 0),neuronNum+1, layerNumb);
        }
        switchFilterVisualizer(layer, 0);
    }
    private static void visualizeFlattenLayer(int layerNumb, NeuralLayer layer){
        Label flattenLabel = new Label("- flattened to " + layer.getFilterNeurons(0).length + " neurons in 1 dimension -");
        flattenLabel.setPadding(new Insets(10,10,10,10));
        flattenLabel.setStyle("-fx-border-color:darkgrey; -fx-border-width: 1 1 1 1; -fx-border-radius: 3; -fx-background-color: linear-gradient(#f6f6f6, #d8d8d8);");
        layer.getFilterNeuronVisuals(0).setPadding(new Insets(0, 0,0, 5));
        layer.getFilterNeuronVisuals(0).add(flattenLabel, 0, 0);
        switchFilterVisualizer(layer,0);
    }
    private static void visualizeMultiFilterLayer(int layerNumb, NeuralLayer layer){
        for (int filterct = 0; filterct < layer.getNumOfFilters(); filterct ++){
            for (int neuronNum=0; neuronNum < layer.getFilterNeurons(filterct).length; neuronNum++) {
                layer.getFilterNeuronVisuals(filterct).add(visualizeNewCommonLayerNeuron(layer.getFilterNeurons(filterct)[neuronNum], createNeuronName(layerNumb, filterct, neuronNum), layerNumb, neuronNum, filterct), neuronNum+1, layerNumb);
            }
        }
        switchFilterVisualizer(layer, 0);
    }

    public static Label[] visualizeOutputLine(GridPane grid, NeuralLayer outputLayer, int lastLayerNum){
        Label[] out = new Label[outputLayer.getFilterNeurons(0).length];
        double[] values = MathOps.interpretOutput(outputLayer, lastLayerNum);
        grid.add(new Label("interprated Output:"),0,lastLayerNum+1);
        GridPane newLineGrid = getNewFilterGrid(); newLineGrid.setPadding(new Insets(0, 0,0, 5));
        grid.add(newLineGrid,1, lastLayerNum+1);
        for (int i = 0; i < outputLayer.getFilterNeurons(0).length; i++){
            Label outLabel = new Label(MathOps.roundOutput.format(values[i]) + " %"); outLabel.setMinSize(60, 30); outLabel.setAlignment(Pos.CENTER);
            Ellipse outBox = new Ellipse(29, 17); outBox.setStroke(Color.BLACK); outBox.setFill(Color.TRANSPARENT);
            newLineGrid.add(outLabel, i,0);
            newLineGrid.add(outBox, i, 0);
            out[i] = outLabel;
        }
        return out;
    }
    public static void updateOutputLine(Label[] outputLineLabels, NeuralNet net){
        int lastLayerNum = net.getNeuralLayers().size()-1;
        NeuralLayer outputLayer = net.getNeuralLayers().get(lastLayerNum);
        double[] values = MathOps.interpretOutput(outputLayer, lastLayerNum);
        for (int i = 0; i < outputLineLabels.length; i++) outputLineLabels[i].setText(MathOps.roundOutput.format(values[i]) + " %");
    }
    /**
     * Erzeugte eine grafische Darstellung der Neuronen eines einzelnen Filters in einem Gridpane. Gilt nur für Flatten-Schichten ÜBER dem fokussierten Neuron.
     *
     * @param layerNum Index der Schicht in der sich der darzustellende Filter befindet.
     * @param v Höhenindex für die Reihe, in welcher der Filter dargestellt werden soll.
     * @param filterNum Index des Filters innerhalb seiner jeweiligen Schicht.
     * @param pane Gridpane, in welchem der Filter dargestellt werden soll.
     */
    private static void drawWWSingleFilterTopFlatten(int layerNum, int v, int filterNum, int filterLenght, GridPane pane, Neuron focusedNeuron){
        int neuronsLenght;
        Neuron[] curFilter = MainMenu.getActualNet().getNeuralLayers().get(layerNum).getFilterNeurons(filterNum);
        neuronsLenght = curFilter.length;
        for (int neuronNum = 0; neuronNum < neuronsLenght; neuronNum++) {
            Boolean isRelevantNeuron = focusedNeuron.hasRelevantWeigthToPredecessor(neuronNum + filterNum*filterLenght);        // das zentrale Neuron hat rel. Verb zum neuronNum Neuron in der Vorgängerschicht
            Boolean hasBeenActivated = curFilter[neuronNum].getActivation()!=0.0;
            drawWWinSNeuron(createNeuronName(layerNum, filterNum, neuronNum), neuronNum, v, pane, isRelevantNeuron, hasBeenActivated);
        }
    }
    /**
     * Erzeugte eine grafische Darstellung der Neuronen eines einzelnen Filters in einem Gridpane. Gilt nur für Flatten-Schichten UNTER dem fokussierten Neuron.
     *
     * @param layerNum Index der Schicht in der sich der darzustellende Filter befindet.
     * @param v Höhenindex für die Reihe, in welcher der Filter dargestellt werden soll.
     * @param filterNum Index des Filters, in dem sich das fokussierte Neuron innerhalb seiner Schicht befindet.
     * @param pane Gridpane, in welchem der Filter dargestellt werden soll.
     * @param focusedNeuronNum Das zur Darstellung im Gewichtsfenster ausgewählte Neuron.
     */
    private static void drawWWSingleFilterBottomFlatten(int layerNum, int v, int filterNum, int filterLenght, GridPane pane, int focusedNeuronNum, Boolean focusedNeuronActive){
        Neuron curBottomNeuron; Boolean isRelevantNeuron; Boolean hasBeenActivated;
        int neuronsLenght = MainMenu.getActualNet().getNeuralLayers().get(layerNum).getFilterNeurons(0).length;
        for (int neuronNum = 0; neuronNum < neuronsLenght; neuronNum++) {
            curBottomNeuron = MainMenu.getActualNet().getNeuralLayers().get(layerNum).getFilterNeurons(0)[neuronNum];
            isRelevantNeuron = curBottomNeuron.hasRelevantWeigthToPredecessor(focusedNeuronNum + filterLenght*filterNum);
            hasBeenActivated = focusedNeuronActive && curBottomNeuron.getActivation()!=0.0;
            drawWWinSNeuron(createNeuronName(layerNum, 0, neuronNum), neuronNum, v, pane, isRelevantNeuron, hasBeenActivated);
        }
    }
    /**
     * Erzeugte eine grafische Darstellung der Neuronen eines einzelnen Filters in einem Gridpane, der ÜBER dem fokussierten Neuron liegt.
     *
     * @param layerNum Index der Schicht in der sich der darzustellende Filter befindet.
     * @param v Höhenindex für die Reihe, in welcher der Filter dargestellt werden soll.
     * @param filterNum Index des Filters innerhalb seiner jeweiligen Schicht.
     * @param pane Gridpane, in welchem der Filter dargestellt werden soll.
     * @param focusedNeuron
     */
    private static void drawWWSingleFilterTop(int layerNum, int v, int filterNum, GridPane pane, Neuron focusedNeuron){
        int neuronsLenght; Boolean isRelevantNeuron; Boolean hasBeenActivated;
        if (layerNum == 0){                                                                                             // Eingabeschicht (Keine vollwertigen Neuronen)
            neuronsLenght = MainMenu.getActualNet().getNeuralLayers().get(layerNum+1).getFilterNeurons(filterNum)[0].getIncomingWeights().length;
            for (int neuronNum = 0; neuronNum < neuronsLenght; neuronNum++) {
                isRelevantNeuron = focusedNeuron.hasRelevantWeigthToPredecessor(neuronNum);
                hasBeenActivated = MainMenu.getActualNet().getActInputValue(neuronNum) != 0.0;
                drawWWinSNeuron(createNeuronName(layerNum, filterNum, neuronNum), neuronNum, v, pane, isRelevantNeuron, hasBeenActivated);
            }
        }
        else {                                                                                                          // Alle Nicht-Eingangs-Schichten die über dem fokussierten Neuron angezeigt werden
            Neuron[] curFilter = MainMenu.getActualNet().getNeuralLayers().get(layerNum).getFilterNeurons(filterNum);
            neuronsLenght = curFilter.length;
            for (int neuronNum = 0; neuronNum < neuronsLenght; neuronNum++) {
                isRelevantNeuron = focusedNeuron.hasRelevantWeigthToPredecessor(neuronNum);
                hasBeenActivated = curFilter[neuronNum].getActivation() != 0.0;
                drawWWinSNeuron(createNeuronName(layerNum, filterNum, neuronNum), neuronNum, v, pane, isRelevantNeuron, hasBeenActivated);
            }
        }
    }
    /**
     * Erzeugte eine grafische Darstellung der Neuronen eines einzelnen Filters in einem Gridpane, der UNTER dem fokussierten Neuron liegt.
     *
     * @param layerNum Index der Schicht in der sich der darzustellende Filter befindet.
     * @param v Höhenindex für die Reihe, in welcher der Filter dargestellt werden soll.
     * @param filterNum Index des Filters innerhalb seiner jeweiligen Schicht.
     * @param pane Gridpane, in welchem der Filter dargestellt werden soll.
     * @param focusedNeuronNum
     */
    private static void drawWWSingleFilterBottom(int layerNum, int v, int filterNum, GridPane pane, int focusedNeuronNum, Boolean focusedNeuronActive){
        Neuron curBottomNeuron; int neuronsLenght; Boolean isRelevantNeuron; Boolean hasBeenActivated;
        neuronsLenght = MainMenu.getActualNet().getNeuralLayers().get(layerNum).getFilterNeurons(filterNum).length;
        for (int neuronNum = 0; neuronNum < neuronsLenght; neuronNum++) {
            curBottomNeuron = MainMenu.getActualNet().getNeuralLayers().get(layerNum).getFilterNeurons(filterNum)[neuronNum];
            isRelevantNeuron = curBottomNeuron.hasRelevantWeigthToPredecessor(focusedNeuronNum);
            hasBeenActivated = focusedNeuronActive && curBottomNeuron.getActivation()!=0.0;
            drawWWinSNeuron(createNeuronName(layerNum, filterNum, neuronNum), neuronNum, v, pane, isRelevantNeuron, hasBeenActivated);
        }
    }

    /**
     * Erzeugte eine grafische Darstellung einer Neuronenschicht mit einem einzelnen Filter im Gewichtsfenster des Visualizers ÜBER dem fokussierten Neuron.
     *
     * @param layerNum Index der darzustellenden Schicht.
     * @param v Höhenindex für die Reihe, in welcher der gewählte Filter der Schicht dargestellt werden soll.
     * @param filterNum Index des Filters, der aus der Schicht zur Darstelllung ausgewählt werden soll.
     * @param neuron Das Neuron, von dem ausgehend die darzustellende Schicht betrachtet wird.
     * @param neuronNum Der Index, welcher angibt das wievielte Neuron seines Filters neuron ist.
     */
    private static void drawWWSingleFilterLayerTop(int layerNum, int v, int filterNum, Neuron neuron, int neuronNum){
        GridPane pane = new GridPane(); pane.setHgap(5);
        MainMenu.getWeightWindowContent().add(pane, 1, v);
        if (v > 0) {
            double[] outgoing = null;
            if (MainMenu.getActualNet().getNeuralLayers().get(layerNum).getLayerType().equals(LayerType.CORE_FLATTEN)) { // Für den Sonderfall, das die Vbdg. zum Flattenlayer auf dessen Nachfolger abgebildet werden soll.
                try {
                    outgoing = MainMenu.getActualNet().outgoingWeights(layerNum, neuronNum, filterNum);
                    drawWWSingleFilterTop(layerNum+1, 1, 0, pane, neuron);
                } catch (IndexOutOfBoundsException e) {
                    logger.error("Flatten-Layer " + layerNum + " hat keine Nachfolgerschicht!");
                }
            }
            else {
                outgoing = MainMenu.getActualNet().outgoingWeights(layerNum - 1, neuronNum, filterNum);
                drawWWSingleFilterTop(layerNum, 1, filterNum, pane, neuron);
            }
            drawWWinWeights(outgoing, 0, pane);
            pane.setPadding(new Insets(-20, 0, 0, -5));
        }
        else {
            drawWWSingleFilterTop(layerNum, 0, filterNum, pane, neuron);
            drawWWinWeights(neuron.getIncomingWeights(),1 , pane);
            pane.setPadding(new Insets(0, 0, -20, -5));
        }
    }
    /**
     * Erzeugte eine grafische Darstellung einer Neuronenschicht mit einem einzelnen Filter im Gewichtsfenster des Visualizers UNTER dem fukussierten Neuron.
     *
     * @param layerNum Index der darzustellenden Schicht.
     * @param v Höhenindex für die Reihe, in welcher der gewählte Filter der Schicht dargestellt werden soll.
     * @param filterNum Index des Filters, der aus der Schicht zur Darstelllung ausgewählt werden soll.
     * @param neuron Das Neuron, von dem ausgehend die darzustellende Schicht betrachtet wird.
     * @param neuronNum Der Index, welcher angibt das wievielte Neuron seines Filters neuron ist.
     */
    private static void drawWWSingleFilterLayerBottom(int layerNum, int v, int filterNum, Neuron neuron, int neuronNum, Boolean neuronActive){
        GridPane pane = new GridPane(); pane.setHgap(5);
        MainMenu.getWeightWindowContent().add(pane, 1, v);
        if (v > 0) {
            double[] outgoing = null;
            if (MainMenu.getActualNet().getNeuralLayers().get(layerNum).getLayerType().equals(LayerType.CORE_FLATTEN)) {            // Für den Sonderfall, das die Vbdg. zum Flattenlayer auf dessen Nachfolger abgebildet werden soll.
                try {
                    outgoing = MainMenu.getActualNet().outgoingWeights(layerNum, neuronNum, filterNum);
                    int filterLenght = MainMenu.getActualNet().getNeuralLayers().get(layerNum-1).getFilterNeurons(0).length; // Anm.: setzt voraus, dass alle Filter gleiche Länge haben (ist bei TF akt. so) -> ggf. noch erweitern
                    drawWWSingleFilterBottomFlatten(layerNum+1, 1, filterNum, filterLenght, pane, neuronNum, neuron.getActivation()!=0.0); // richtige filterNum übergeben!
                } catch (IndexOutOfBoundsException e) {
                    logger.error("Flatten-Layer " + layerNum + " hat keine Nachfolgerschicht!");
                }
            }
            else {
                outgoing = MainMenu.getActualNet().outgoingWeights(layerNum - 1, neuronNum, filterNum);
                drawWWSingleFilterBottom(layerNum, 1, filterNum, pane, neuronNum, neuronActive);
            }
            drawWWinWeights(outgoing, 0, pane);
            pane.setPadding(new Insets(-20, 0, 0, -5));
        }
        else {
            drawWWSingleFilterBottom(layerNum, 0, filterNum, pane, neuronNum, neuronActive);
            drawWWinWeights(neuron.getIncomingWeights(),1 , pane);
            pane.setPadding(new Insets(0, 0, -20, -5));
        }
    }

    /**
     * Erzeugte eine grafische Darstellung einer Neuronenschicht mit mehreren Filtern im Gewichtsfenster des Visualizers.
     *
     * @param layerNum Index der darzustellenden Schicht.
     * @param v Höhenindex für die Reihe, in welcher der gewählte Filter der Schicht dargestellt werden soll.
     * @param neuronNum Der Index, welcher angibt das wievielte Neuron seines Filters das Neuron ist, von dem ausgehend die darzustellende Schicht betrachtet wird.
     */
    private static void drawWWMultiFilterLayer(int layerNum, int v, int neuronNum, Boolean neuronActive){
        ComboBox box = new ComboBox();
        GridPane pane = new GridPane(); pane.setHgap(5);
        MainMenu.getWeightWindowContent().add(pane, 1, v);
        if (v > 0) {
            pane.setPadding(new Insets(-20, 0, 0, -5));
            MainMenu.getWeightWindowContent().add(createComboFields(layerNum, MainMenu.getActualNet().getNeuralLayers().get(layerNum), box, weightsWinComboEventBottom(layerNum, box, neuronNum, pane, neuronActive)), 0, v);
            switchWeightWinVisualizerBottom(layerNum, box, neuronNum, pane, neuronActive);
        }
        else {
            pane.setPadding(new Insets(0, 0, -20, -5));
            if (MainMenu.getActualNet().getNeuralLayers().get(layerNum).getLayerType().equals(LayerType.CORE_FLATTEN)) {// Für den Sonderfall, das die Vbdg. zum Flattenlayer auf Multifilter-Vorgänger abgebildet werden soll.
                try {
                    MainMenu.getWeightWindowContent().add(createComboFields(layerNum-1, MainMenu.getActualNet().getNeuralLayers().get(layerNum-1), box, weightsWinComboEventTopFlatten(layerNum-1, box, neuronNum, pane)), 0, v);
                    switchWeightWinVisualizerTopFlatten(layerNum-1, box, neuronNum, pane);
                } catch (IndexOutOfBoundsException e) {
                    logger.error("Flatten-Layer " + layerNum + " hat keine Vorgängerschicht!");
                }
            }
            else {
                MainMenu.getWeightWindowContent().add(createComboFields(layerNum, MainMenu.getActualNet().getNeuralLayers().get(layerNum), box, weightsWinComboEventTop(layerNum, box, neuronNum, pane)), 0, v);
                switchWeightWinVisualizerTop(layerNum, box, neuronNum, pane);
            }
        }
    }
    /**
     * Erzeugt ein Ellipsenobjekt zur grafischen Repräsentation eines einzelnen Neurons in einem GridPane.
     *
     * @param name Die Bezeichnung des darzustellenden Neurons.
     * @param h Der horizontale Index für die Platzierung der Ellipse im GridPane.
     * @param v Der vertikale Index für die Platzierung der Ellipse im GridPane.
     * @param pane Das GridPane, in dem die Ellipse verankert werden soll.
     */
    private static void drawWWinSNeuron(String name, int h, int v, GridPane pane, Boolean isRelevantNeuron, Boolean hasBeenActivated){
        Ellipse newNeuron = new Ellipse(30, 17); newNeuron.setStroke(Color.BLACK); newNeuron.setFill(Color.LIGHTGRAY);
        if (isRelevantNeuron) {
            if (hasBeenActivated && MainMenu.getShowActivationColor()) newNeuron.setFill(Color.ORANGE);
            else newNeuron.setFill(Color.CYAN);
        }
        Label newNeuronL = new Label(name); newNeuronL.setMaxWidth(60); newNeuronL.setMaxHeight(30); newNeuronL.setAlignment(Pos.CENTER);
        pane.add(newNeuron, h+1, v);
        pane.add(newNeuronL, h+1, v);
    }
    /**
     * Stellt eine angegebene Menge von Gewichten im Anzeigenfenster für die gewichteten Verbindungen eines Einzelneurons dar.
     *
     * @param weights Array mit den Double-Werten aller Gewichte, die im Anzeigenfenster abgebildet werden sollen.
     * @param v Höhenwert für die Abbildung der Gewichte im Anzeigenfenster.
     */
    private static void drawWWinWeights(double[] weights, int v, GridPane pane){
        for (int weightNumb = 0; weightNumb < weights.length; weightNumb++){
            Label weight = new Label(MathOps.roundWeights.format(weights[weightNumb])); weight.setMaxWidth(60); weight.setAlignment(Pos.CENTER);
            pane.add(weight, weightNumb+1 ,v);
            Rectangle weightBox = new Rectangle(60, 30); weightBox.setStroke(Color.DARKGRAY); weightBox.setFill(Color.TRANSPARENT);
            pane.add(weightBox, weightNumb+1, v);
        }
    }
    /**
     * @param layerNum Gibt an, in der wievielten Schicht das Neuron enthalten ist.
     * @param filterNum Gibt an, der wievielte Filter der Schicht der Filter ist, der das Neuron enthält.
     * @param neuronNum Gibt an, an welcher Stelle das Neuron in seinem Filter steht.
     * @return Ein String, der als eindeutiger Bezeichner für ein einzelnes Neuron in einem neuronalen Netz geeignet ist.
     */
    private static String createNeuronName(int layerNum, int filterNum, int neuronNum){
        if (layerNum == 0){return ("Inp" + (Integer)neuronNum);}
        else {return (((Integer) layerNum).toString() + "." + ((Integer) filterNum).toString() + "/" + ((Integer) neuronNum).toString());}
    }

    /**
     * @return Ein neu erzeugtes GridPane. Kann als Anker für die Darstellung eines einzelnen Filters im Gewichtsfenster genutzt werden.
     */
    public static GridPane getNewFilterGrid() {
        GridPane newGrid = new GridPane();
        newGrid.setHgap(5);
        return newGrid;
    }
    /**
     * Erzeugt einen Button für die grafische Darstellung eines Neurons und legt den benötigten Event-Handler für die Anzeige des Gewichtsfensters an.
     *
     * @param neuron Das Neuron, für welches ein Button zur Visualisierung erzeugt werden soll.
     * @param name Die Beschriftung für den zum Neuron gehörigen Button.
     * @param layerNum Die wievielte Schicht des Netzes enthält das Neuron neuron.
     * @param neuronNum Das wievielte Neuron seines zugehörigen Filters ist dieses Neuron.
     * @param filterNum Der wievielte Filter der Schicht enthält das Neuron neuron.
     * @return Ein Button für die grafische Darstellung eines einzelnen Neurons.
     */
    private static Button visualizeNewCommonLayerNeuron(Neuron neuron, String name, int layerNum, int neuronNum, int filterNum){
        Button newBtn = new Button(); newBtn.setMinSize(60, 30);
        newBtn.setText(name);
        NeuralNet net = MainMenu.getActualNet();
        int layerFilterCt = MainMenu.getActualNet().getNeuralLayers().get(layerNum).getNumOfFilters();
        int succLayerFilterCt;
        if (layerNum == MainMenu.getActualNet().getNeuralLayers().size()-1) { succLayerFilterCt = 1; }           // Wenn Nachfolgeschicht letzte Schicht (Output) ist -> Anzahl Nachfolgerfilter muss 1 sein.
        else { succLayerFilterCt = MainMenu.getActualNet().getNeuralLayers().get(layerNum+1).getNumOfFilters(); }   // Wenn es noch eine Nachfolgeschicht im Netz gibt -> Anzahl Nachfolgerfilter erfragen.
        newBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (layerNum > 0) { MainMenu.changeWeightWindowTitle("Information Window - Weights of neuron " + name + " (with average incoming activation " + neuron.getCurAvgIncActivationForNeuron() +
                        " | average abs. weight " + (neuron.getWeightsSum() / neuron.getIncomingWeights().length) + " | activationtype " + net.getNeuralLayers().get(layerNum).getActivationType().getKerasName() + "):"); }
                else { MainMenu.changeWeightWindowTitle("Information Window - Weights of neuron " + name + " (with incoming activation " +
                        net.getActInputValue(neuronNum) + " | average abs. weight 1 | activationtype none):"); }
                MainMenu.resetWeightWindowContent();
                if (layerFilterCt == succLayerFilterCt) { createEqualFiltersButtonEvent(neuron, name, layerNum, neuronNum, filterNum); }
                else if (layerFilterCt < succLayerFilterCt && layerNum == 0) { createInputLayerFilterButtonEvent(name, neuronNum); }
                else if (layerFilterCt < succLayerFilterCt) { }                                                   // Noch zu impelemntieren -> Wenn mitten im Netz von FF wieder auf Multifilter gewechselt wird.
                else { createManyToSparseFiltersButtonEventBottom(neuron, name, layerNum, neuronNum, filterNum); }
                MainMenu.updateLastSelectedNeuronButton(newBtn);
            // System.out.println("avgMaxActivation of the layer: " + MainMenu.getActualNet().getNeuralLayers().get(layerNum).getAvgMaxFactorActivation());
            }
        });
        neuron.setButtonLink(newBtn);
        return newBtn;
    }

    /**
     * Erzeugt ein Event für einen Neuronen-Button. Der Button soll in einer Schicht liegen, auf die eine Schicht mit gleicher Filterzahl folgt oder welche die Ausgabeschicht ist.
     *
     * @param neuron Das Neuron, das zum dem Button gehört für den das Event erzeugt werden soll.
     * @param name Der Bezeichner für das zum Button gehörige Neuron.
     * @param layerNum Die wievielte Schicht des Netzes enthält das Neuron neuron.
     * @param neuronNum Das wievielte Neuron seines zugehörigen Filters ist dieses Neuron.
     * @param filterNum Der wievielte Filter der Schicht enthält das Neuron neuron.
     */
    private static void createEqualFiltersButtonEvent(Neuron neuron, String name, int layerNum, int neuronNum, int filterNum) {
        int v = 0;
        if (layerNum > 0) {
            if (MainMenu.getActualNet().getNeuralLayers().get(layerNum-1).getLayerType().equals(LayerType.CORE_FLATTEN)) {
                drawWWMultiFilterLayer(layerNum-1, v, neuronNum, neuron.getActivation()!=0.0);
            }
            else {
                drawWWSingleFilterLayerTop(layerNum-1, v, filterNum, neuron, neuronNum);
            }
            v = v+2;
        }
        createWWNeuron(name, layerNum, filterNum, neuronNum, v);
        if (layerNum < MainMenu.getActualNet().getNeuralLayers().size()-1) {
            drawWWSingleFilterLayerBottom(layerNum+1,v+2, filterNum, neuron, neuronNum, neuron.getActivation()!=0.0);
        }
    }
    /**
     * Erzeugt ein Event für einen Neuronen-Button. Der Button soll in der Eingabe-Schicht liegen.
     *
     * @param name Der Bezeichner für das zum Button gehörige Neuron.
     * @param neuronNum Das wievielte Neuron seines zugehörigen Filters ist dieses Neuron.
     */
    private static void createInputLayerFilterButtonEvent(String name, int neuronNum) {    // Momentan nur für die Eingabe-Schicht genutzt -> Gezielt für Eingabeschicht konzipieren, da diese keine Neuronenobjekte hat!
        int v = 0;                                                                                        // Muss folglich Zusatzinformationen wie Aktivierung (Input) manuell aufnehmen.
        NeuralLayer neuronsLayer = MainMenu.getActualNet().getNeuralLayers().get(0);
        GridPane singleNeuronPane = new GridPane(); MainMenu.getWeightWindowContent().add(singleNeuronPane, 1, v);
        InputWindow.resetActRelInputCoords(neuronNum, MainMenu.getActualNet().getActInputValue(neuronNum)!=0.0);
        if (InputWindow.isShown()) { InputWindow.resetInputPicture(); }
        Boolean isRelevantNeuron = false;
        Boolean hasBeenActivated = false;
        for (int i = 0; i < MainMenu.getActualNet().getNeuralLayers().get(1).getNumOfFilters(); i++){
            isRelevantNeuron = MainMenu.getActualNet().getNeuralLayers().get(1).hasRelevantWeightsToPredecessorNeuronStandard(i, neuronNum);   // auf Flatten muss hier nicht geprüft werden -> wäre defekte Netzstruktur
        }
        Label minActNeuron = new Label(" | Minimum-Input-Activation: Not calculated yet!" );
        if (!neuronsLayer.getMinFactorActivations().isEmpty()) {
            minActNeuron.setText(" | Minimum-Input-Activation: " + neuronsLayer.getMinFactorActivations().get(0)[neuronNum]);
        }
        Label maxActNeuron = new Label(" | Maximum-Input-Activation: Not calculated yet! | " );
        if (!neuronsLayer.getMaxFactorActivations().isEmpty()) {
            maxActNeuron.setText(" | Maximum-Input-Activation: " + neuronsLayer.getMaxFactorActivations().get(0)[neuronNum] + " | ");
        }
        minActNeuron.setMaxHeight(30); minActNeuron.setAlignment(Pos.CENTER);
        maxActNeuron.setMaxHeight(30); maxActNeuron.setAlignment(Pos.CENTER);
        Rectangle activationBox = new Rectangle(250, 30); activationBox.setStroke(Color.BLACK); activationBox.setFill(Color.TRANSPARENT);
        Label actBoxL = new Label("Activation: " + MainMenu.getActualNet().getActInputValue(neuronNum)); actBoxL.setMaxWidth(250); actBoxL.setMaxHeight(30); actBoxL.setAlignment(Pos.CENTER);
        Rectangle biasBox = new Rectangle(200, 30); biasBox.setStroke(Color.BLACK); biasBox.setFill(Color.TRANSPARENT);
        Label biasBoxL = new Label("Bias: 0.0");
        singleNeuronPane.add(biasBox,2,0);
        biasBoxL.setMaxWidth(200); biasBoxL.setMaxHeight(30); biasBoxL.setAlignment(Pos.CENTER); singleNeuronPane.add(biasBoxL, 2, 0);
        if (MainMenu.getActualNet().inputAvailable()) {
            actBoxL.setText("Activation: " + MainMenu.getActualNet().getActInputValue(neuronNum));
            hasBeenActivated = MainMenu.getActualNet().getActInputValue(neuronNum)!=0.0;
        }
        drawWWinSNeuron(name, 0, v, singleNeuronPane, isRelevantNeuron, hasBeenActivated);
        singleNeuronPane.add(minActNeuron, 3, 0);
        singleNeuronPane.add(maxActNeuron, 4, 0);
        singleNeuronPane.add(activationBox, 5, 0); singleNeuronPane.add(actBoxL, 5, 0);
        drawWWMultiFilterLayer(1, v+2, neuronNum, hasBeenActivated);
    }
    /**
     * Erzeugt ein Event für einen Neuronen-Button. Der Button soll in einer Schicht liegen, auf die eine Schicht mit kleinerer Filterzahl folgt.
     *
     * @param neuron Das Neuron, das zum dem Button gehört für den das Event erzeugt werden soll.
     * @param name Der Bezeichner für das zum Button gehörige Neuron.
     * @param layerNum Die wievielte Schicht des Netzes enthält das Neuron neuron.
     * @param neuronNum Das wievielte Neuron seines zugehörigen Filters ist dieses Neuron.
     * @param filterNum Der wievielte Filter der Schicht enthält das Neuron neuron.
     */
    private static void createManyToSparseFiltersButtonEventBottom(Neuron neuron, String name, int layerNum, int neuronNum, int filterNum) {
        int v = 0;
        if (layerNum > 0) {
            drawWWSingleFilterLayerTop(layerNum-1, v, filterNum, neuron, neuronNum);
            v = v+2;
        }
        createWWNeuron(name, layerNum, filterNum, neuronNum, v);
        if (layerNum < MainMenu.getActualNet().getNeuralLayers().size()-1) {
            drawWWSingleFilterLayerBottom(layerNum+1, v+2, filterNum, neuron, neuronNum, neuron.getActivation()!=0.0);
        }
    }
    /**
     * Erzeugt die Darstellung für die Informationen des fokussierten Neurons im Gewichtsfenster.
     */
    private static void createWWNeuron(String name, int layerNum, int filterNum, int neuronNum, int verticalNum) {
        NeuralLayer neuronsLayer = MainMenu.getActualNet().getNeuralLayers().get(layerNum);
        Neuron thisNeuron = neuronsLayer.getFilterNeurons(filterNum)[neuronNum];
        GridPane singleNeuronPane = new GridPane(); MainMenu.getWeightWindowContent().add(singleNeuronPane, 1, verticalNum);
        InputWindow.resetActRelInputCoords(thisNeuron.getRelevantInputFields(), thisNeuron.getConnectedInputFields());
        if (InputWindow.isShown()) { InputWindow.resetInputPicture(); }
        Boolean isRelevantNeuron = false;
        if (layerNum+1 < MainMenu.getActualNet().getNeuralLayers().size()){
            NeuralLayer succLayer = MainMenu.getActualNet().getNeuralLayers().get(layerNum+1);
            for (int i = 0; i < succLayer.getNumOfFilters(); i++){
                if (succLayer.getLayerType() == LayerType.CORE_FLATTEN) {
                    isRelevantNeuron = succLayer.hasRelevantWeightsToPredecessorNeuronFlatten(i*succLayer.getFilterNeurons(0).length, neuronNum); // geht wieder von gleicher Länge aller Filter einer Schicht aus.
                }
                else { isRelevantNeuron = succLayer.hasRelevantWeightsToPredecessorNeuronStandard(i, neuronNum); }
            }
        }
        else { isRelevantNeuron = ActivationPathVisualizer.getRelevantOutputNeurons().contains(neuronNum); }
        drawWWinSNeuron(name, 0, 0, singleNeuronPane, isRelevantNeuron, thisNeuron.getActivation()!=0.0);
        Label minActNeuron = new Label(" | Minimum-Input-Activation: Not calculated yet!" );
        if (!neuronsLayer.getMinFactorActivations().isEmpty()) {
            minActNeuron.setText(" | Minimum-Input-Activation: " + neuronsLayer.getMinFactorActivations().get(filterNum)[neuronNum]);
        }
        Label maxActNeuron = new Label(" | Maximum-Input-Activation: Not calculated yet! | " );
        if (!neuronsLayer.getMaxFactorActivations().isEmpty()) {
            maxActNeuron.setText(" | Maximum-Input-Activation: " + neuronsLayer.getMaxFactorActivations().get(filterNum)[neuronNum] + " | ");
        }
        Rectangle activationBox = new Rectangle(250, 30); activationBox.setStroke(Color.BLACK); activationBox.setFill(Color.TRANSPARENT);
        Rectangle biasBox = new Rectangle(200, 30); biasBox.setStroke(Color.BLACK); biasBox.setFill(Color.TRANSPARENT);
        Label actBoxL = new Label("Activation: " + thisNeuron.getActivation());
        Label biasBoxL = new Label("Bias: " + thisNeuron.getBias());
        singleNeuronPane.add(biasBox,2,0);
        biasBoxL.setMaxWidth(200); biasBoxL.setMaxHeight(30); biasBoxL.setAlignment(Pos.CENTER); singleNeuronPane.add(biasBoxL, 2, 0);
        minActNeuron.setMaxHeight(30); minActNeuron.setAlignment(Pos.CENTER); singleNeuronPane.add(minActNeuron, 3, 0);
        maxActNeuron.setMaxHeight(30); maxActNeuron.setAlignment(Pos.CENTER); singleNeuronPane.add(maxActNeuron, 4, 0);
        singleNeuronPane.add(activationBox, 5, 0);
        actBoxL.setMaxWidth(250); actBoxL.setMaxHeight(30); actBoxL.setAlignment(Pos.CENTER); singleNeuronPane.add(actBoxL, 5, 0);
        Label inputSum = new Label(" -[ incoming sum of acvtivations: " + thisNeuron.getIncomingActivationSum() + " ]- ");
        inputSum.setMaxHeight(30); inputSum.setAlignment(Pos.CENTER); singleNeuronPane.add(inputSum, 6, 0);
    }
}