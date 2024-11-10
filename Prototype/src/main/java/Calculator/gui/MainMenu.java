package Calculator.gui;

import Calculator.general.InputLoader;
import Calculator.net_elements.NeuralLayer;
import Calculator.net_elements.NeuralNet;
import Calculator.tree_elements.TreeDecision;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;

import java.util.HashSet;

public final class MainMenu {

    private static NeuralNet net;
    private static TitledPane weightWindowPane;
    private static GridPane weightWindowContent;
    private static double minAct = 0;
    private static double maxAct = 0;

    private static Boolean showActivationColor = false;
    private static Button lastSelectedNeuronButton = null;
    private static Label[] outputLabel;

    private static ComboBox<String> pathSelector = new ComboBox();

    public static void drawNetMainWindow(NeuralNet actNet){
        net = actNet;
    }

    public static void changeWeightWindowTitle(String string){
        weightWindowPane.textProperty().setValue(string);
    }
    public static void resetWeightWindowContent(){ weightWindowContent.getChildren().clear();}
    public static GridPane getWeightWindowContent() { return weightWindowContent; }

    public static NeuralNet getActualNet(){return net;}
    public static Boolean getShowActivationColor(){return showActivationColor;}
    public static void updateLastSelectedNeuronButton(Button button){ lastSelectedNeuronButton = button; }
    private static void drawNetMainWindow(GridPane grid){
        NeuralLayer layer;
        LayerVisualizer.visualizeInputLayer(grid,net.getNeuralLayers().get(1));
        for (int layerNumb=1; layerNumb < net.getNeuralLayers().size(); layerNumb++){
            layer = net.getNeuralLayers().get(layerNumb);
            LayerVisualizer.visualizeHiddenLayer(layerNumb, grid, layer);
        }
        int lastLayerNum = net.getNeuralLayers().size()-1;
        outputLabel = LayerVisualizer.visualizeOutputLine(grid, net.getNeuralLayers().get(lastLayerNum), lastLayerNum);
    }

    public static BorderPane mainPane(){
        BorderPane basePane = new BorderPane();

        GridPane neuronWindow = new GridPane();
        neuronWindow.setAlignment(Pos.CENTER);
        neuronWindow.setHgap(5); neuronWindow.setVgap(60);
        neuronWindow.setPadding(new Insets(25, 25, 25, 25));

        GridPane weightWindow = new GridPane();
        weightWindow.setAlignment(Pos.CENTER);
        weightWindow.setHgap(5); weightWindow.setVgap(20);
        weightWindow.setPadding(new Insets(25, 25, 25, 25));
        weightWindow.add(new Label("No single neuron selected"),1,0);
        weightWindowContent = weightWindow;

        ScrollPane orgScroll01 = new ScrollPane();
        orgScroll01.setContent(neuronWindow);

        ScrollPane orgScroll02 = new ScrollPane();
        orgScroll02.setContent(weightWindow);

        TitledPane orgTitled01 = new TitledPane();
        orgTitled01.textProperty().setValue("Information Window - No neuron selected!");
        orgTitled01.setContent(orgScroll02);
        orgTitled01.expandedProperty().setValue(false);
        weightWindowPane = orgTitled01;

        SplitPane orgPane01 = new SplitPane(); orgPane01.setOrientation(Orientation.VERTICAL);
        HBox orgPane1Top = new HBox(orgScroll01);
        orgPane01.getItems().addAll(orgPane1Top, orgTitled01); orgPane01.setDividerPosition(0,0.6);

        ScrollPane menuScroll03 = new ScrollPane();
        GridPane rightMain = new GridPane();
        rightMain.setAlignment(Pos.CENTER);
        rightMain.setHgap(5); weightWindow.setVgap(20);
        rightMain.setPadding(new Insets(25, 25, 25, 25));

        VBox calcBox01 = new VBox();
        calcBox01.setPadding(new Insets(0,0,20,0));
        rightMain.add(calcBox01, 0, 0);

        VBox calcBox02 = new VBox();
        calcBox02.setSpacing(5.0);
        TextField minActField = new TextField();
        minActField.setPrefWidth(165); minActField.setPromptText("req. a double, e.g. 1.0");
        minActField.setTooltip(new Tooltip("The miminum Activation that can be assigned \nto the input-neurons of the ANN."));
        TextField maxActField = new TextField();
        maxActField.setPrefWidth(165); maxActField.setPromptText("req. a double, e.g. 1.0");
        maxActField.setTooltip(new Tooltip("The maximum Activation that can be assigned \nto the input-neurons of the ANN."));

        Tooltip neuralThresholdTooltip = new Tooltip("How much smaller can a single weight be,\ncompared to the average weight, before\nit is ignored. (Applied to neuron connections)");
        Label neuralThresholdLabel = new Label("Neural Sensitivity: " + 1.0);
        neuralThresholdLabel.setTooltip(neuralThresholdTooltip);

        Slider neuralThresholdSlider = new Slider(0.01, 1.0, 1.0);
        neuralThresholdSlider.setShowTickMarks(true);
        neuralThresholdSlider.setTooltip(neuralThresholdTooltip);
        neuralThresholdSlider.setMajorTickUnit(0.1);
        neuralThresholdSlider.setBlockIncrement(0.01);
        neuralThresholdSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> obs_val,
                                Number old_val, Number new_val) {
                double tmp = new_val.doubleValue()*100;
                tmp = Math.round(tmp); tmp = tmp/100;
                neuralThresholdSlider.setValue(tmp);
                neuralThresholdLabel.setText("Neural Sensitivity: " + neuralThresholdSlider.getValue());
            }
        });

        Label pathDropdownLabel = new Label("chosen converted Decisionpath:"); pathDropdownLabel.setTextFill(Color.DARKGRAY);
        HBox selectedPathsBorderBox = new HBox(); selectedPathsBorderBox.setMinSize(160, 90);
        selectedPathsBorderBox.setAlignment(Pos.CENTER); selectedPathsBorderBox.setStyle("-fx-border-color: darkgray;"  + "-fx-border-width: 1;");
        Label pathChosenDropdownLabel = new Label("(corresponding Input to path)"); pathChosenDropdownLabel.setTextFill(Color.DARKGRAY);
        selectedPathsBorderBox.getChildren().add(pathChosenDropdownLabel);

        pathSelector.setMinSize(165, 30); pathSelector.setDisable(true); pathSelector.setPromptText("none selected");
        pathSelector.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                double[] inputToImage = net.getDecTreeByNum(pathSelector.getSelectionModel().getSelectedIndex()).getDecInput();
                HashSet<Integer> corresInputsForPath = net.getDecTreeByNum(pathSelector.getSelectionModel().getSelectedIndex()).getOuterEdge().getCorrespInputs();
                ImageView imageViewToInput = TreeVisualizer.createInputView(corresInputsForPath, inputToImage, 1, true);
                selectedPathsBorderBox.getChildren().clear(); selectedPathsBorderBox.getChildren().add(imageViewToInput);
            }
        });

        Button showDecPathButton = new Button(" show chosen\n Decisionpath");
        showDecPathButton.setMinSize(165, 30); showDecPathButton.setTooltip(new Tooltip("Shows the decisiontree merged from the derived decisionpaths."));
        showDecPathButton.setDisable(true);
        showDecPathButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TreeDecision chosenDecisionPath = net.getDecTreeByNum(pathSelector.getSelectionModel().getSelectedIndex());
                DecisionWindow.showDecisionPath(TreeVisualizer.visualizeDecisionPath(chosenDecisionPath));
            }
        });

        Tooltip activationThresholdTooltip = new Tooltip("How much smaller can a single incoming\n activation be, compared to the average in the filter,\n before it is ignored. (Applied for cumouting\nhierarchy-levels in decision paths)");
        Label activationThresholdLabel = new Label("Activation Sensitivity: " + 1.0); activationThresholdLabel.setTextFill(Color.DARKGRAY);
        activationThresholdLabel.setTooltip(activationThresholdTooltip);

        Slider activationThresholdSlider = new Slider(0.01, 1.0, 1.0);
        activationThresholdSlider.setShowTickMarks(true);
        activationThresholdSlider.setTooltip(activationThresholdTooltip);
        activationThresholdSlider.setMajorTickUnit(0.1);
        activationThresholdSlider.setBlockIncrement(0.01);
        activationThresholdSlider.setDisable(true);
        activationThresholdSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                double tmp = new_val.doubleValue()*100;
                tmp = Math.round(tmp); tmp = tmp/100;
                activationThresholdSlider.setValue(tmp);
                activationThresholdLabel.setText("Activation Sensitivity: " + activationThresholdSlider.getValue());
            }
        });

        Button showDecTreeButton = new Button("show Decisiontree");
        showDecTreeButton.setMinSize(165, 30); showDecTreeButton.setTooltip(new Tooltip("Shows a visualization of the decisiontree derived from the decisionpaths."));
        showDecTreeButton.setDisable(true);
        showDecTreeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                TreeWindow.showDecisionPath(TreeVisualizer.visualizeDecisionTree(net.getRootNodeDecTree()));
            }
        });

        Button combineButton = new Button("combine Decisionpaths");
        combineButton.setMinSize(165, 30); combineButton.setTooltip(new Tooltip("Combines all currently converted Decisions into one hierarchical Decisiontree."));
        combineButton.setDisable(true);
        combineButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                net.combineConvertedDecisions();
                showDecTreeButton.setDisable(false);
            }
        });

        Button convertButton = new Button("convert cur. Input");
        convertButton.setMinSize(165, 30); convertButton.setTooltip(new Tooltip("Converts the decision belonging to the current input."));
        convertButton.setDisable(true);
        convertButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                boolean toBeSaved = net.convertAndSaveCurrentDecision(activationThresholdSlider.getValue());
                if (toBeSaved){
                    pathSelector.getItems().add(pathSelector.getItems().size()+1 + ". converted Path");
                }
                showDecPathButton.setDisable(false); pathSelector.setDisable(false); pathDropdownLabel.setTextFill(Color.BLACK);
                selectedPathsBorderBox.setStyle("-fx-border-color: black;"  + "-fx-border-width: 1;");
                pathChosenDropdownLabel.setTextFill(Color.BLACK);
                combineButton.setDisable(false);
            }
        });

        Label inputFieldLabel = new Label("Input-Source:"); inputFieldLabel.setTextFill(Color.DARKGRAY);

        TextField inputSourceField = new TextField("");
        inputSourceField.setPrefWidth(165); inputSourceField.setDisable(true);
        inputSourceField.setTooltip(new Tooltip("Enter the file-path to your input-image."));

        Button showInput = new Button("show cur. Input"); showInput.setMinSize(165, 15); showInput.setDisable(true);
        showInput.setTooltip(new Tooltip("shows the internal input as a grayscale picture "));
        showInput.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                InputWindow.showInputWindow();
            }
        });

        ToggleGroup showActivationToggleGroup = new ToggleGroup();
        ToggleButton showActivatedNeurons = new ToggleButton("mark neuron\n activations");
        showActivatedNeurons.setToggleGroup(showActivationToggleGroup);
        showActivatedNeurons.setMinSize(80, 30); showActivatedNeurons.setDisable(true);
        showActivatedNeurons.setTooltip(new Tooltip("Marks all relevant neurons with an activation that is not 0.0."));
        showActivatedNeurons.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                showActivationColor = true;
                ActivationPathVisualizer.markActivatedNeurons(net, true);
                if (lastSelectedNeuronButton!=null)lastSelectedNeuronButton.fire();
            }
        });
        ToggleButton hideActivatedNeurons = new ToggleButton("hide neuron \n activations");
        hideActivatedNeurons.setToggleGroup(showActivationToggleGroup);
        hideActivatedNeurons.setMinSize(80, 30); hideActivatedNeurons.setDisable(true);
        hideActivatedNeurons.setTooltip(new Tooltip("Hides if a relevant neuron is currently active."));
        hideActivatedNeurons.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                showActivationColor = false;
                ActivationPathVisualizer.markActivatedNeurons(net, false);
                if (lastSelectedNeuronButton!=null)lastSelectedNeuronButton.fire();
            }
        });

        Button calcActivationsForInput = new Button();  calcActivationsForInput.setMinSize(165, 30);
        calcActivationsForInput.setText("    calculate\nnew activations"); calcActivationsForInput.setDisable(true);
        calcActivationsForInput.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (InputLoader.testInputWidthAndHeigth(inputSourceField.getText())){
                    InputWindow.hideInputWindow();
                    ActivationPathVisualizer.markActivatedNeurons(net, false);
                    net.calculateNetActivations(InputLoader.loadImageAsGrayScaleArray(inputSourceField.getText(), minAct, maxAct));
                    LayerVisualizer.updateOutputLine(outputLabel, net);
                    showActivatedNeurons.setDisable(false); hideActivatedNeurons.setDisable(false);
                    hideActivatedNeurons.setSelected(true);
                    showInput.setDisable(false); convertButton.setDisable(false);
                    activationThresholdSlider.setDisable(false); activationThresholdLabel.setTextFill(Color.BLACK);
                }
                else System.out.println("Falsche Dimension des Inputs");    // noch aufhübschen (logger etc.)
            }
        });

        Label minActLable = new Label("Min. Input Value: not declared");
        Label maxActLabel = new Label("Max. Input Value: not declared");

        Button calcRelElements = new Button(); calcRelElements.setMinSize(165, 30);
        calcRelElements.setText("       calculate\nrelevant elements");
        calcRelElements.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                int triggeredExceptions = 0;
                double threshold = 0;
                try {
                    minAct = Double.parseDouble(minActField.getText());
                } catch (NumberFormatException exc) {
                    minActField.setText("Enter a float!");
                    triggeredExceptions++;
                }
                try {
                    maxAct = Double.parseDouble(maxActField.getText());
                } catch (NumberFormatException exc) {
                    maxActField.setText("Enter a float!");
                    triggeredExceptions++;
                }
                threshold = neuralThresholdSlider.getValue();
                if (triggeredExceptions < 1 && threshold != 0) {
                    InputWindow.hideInputWindow();
                    showActivatedNeurons.setDisable(true); hideActivatedNeurons.setDisable(true);
                    calcActivationsForInput.setDisable(true); showInput.setDisable(true);
                    convertButton.setDisable(true);
                    activationThresholdSlider.setDisable(true); activationThresholdLabel.setTextFill(Color.DARKGRAY);
                    net.resetNetActivations();
                    net.calculateAllActivationFactors(minAct, maxAct);
                    ActivationPathVisualizer.calculatePaths(net, threshold);
                    minActLable.setText("Min. Input Value: set to " + minAct);
                    maxActLabel.setText("Max. Input Value: set to " + maxAct);
                    inputSourceField.setDisable(false); inputFieldLabel.setTextFill(Color.BLACK);
                    calcActivationsForInput.setDisable(false);
                    if (lastSelectedNeuronButton!=null)lastSelectedNeuronButton.fire();
                }
            }
        });

        calcBox02.getChildren().add(minActLable);
        calcBox02.getChildren().add(minActField);
        calcBox02.getChildren().add(maxActLabel);
        calcBox02.getChildren().add(maxActField);
        calcBox02.getChildren().add(neuralThresholdLabel);
        calcBox02.getChildren().add(neuralThresholdSlider);
        calcBox02.getChildren().add(calcRelElements);
        calcBox02.getChildren().add(inputFieldLabel);
        calcBox02.getChildren().add(inputSourceField);
        calcBox02.getChildren().add(showInput);
        calcBox02.getChildren().add(calcActivationsForInput);
        GridPane actButtonPane = new GridPane();
        actButtonPane.add(showActivatedNeurons,0,0); actButtonPane.add(hideActivatedNeurons,1,0);
        calcBox02.getChildren().add(actButtonPane);
        calcBox02.getChildren().add(activationThresholdLabel);
        calcBox02.getChildren().add(activationThresholdSlider);
        calcBox02.getChildren().add(convertButton);
        calcBox02.getChildren().add(pathDropdownLabel);
        calcBox02.getChildren().add(pathSelector);
        calcBox02.getChildren().add(selectedPathsBorderBox);
        calcBox02.getChildren().add(showDecPathButton);
        calcBox02.getChildren().add(combineButton);
        calcBox02.getChildren().add(showDecTreeButton);
        rightMain.add(calcBox02, 0, 1);
        menuScroll03.setContent(rightMain);

        SplitPane orgPane00 = new SplitPane(); orgPane00.setOrientation(Orientation.HORIZONTAL);
        orgPane00.getItems().addAll(orgPane01, menuScroll03); orgPane00.setDividerPosition(0,0.9);

        basePane.setCenter(orgPane00);
        InputWindow.initialize();
        DecisionWindow.initialize();
        TreeWindow.initialize();
        drawNetMainWindow(neuronWindow);

        return basePane;
    }

}
