package Calculator.gui;

import Calculator.general.MapperService;
import Calculator.general.MathOps;
import Calculator.tree_elements.*;
import com.fasterxml.jackson.databind.ser.std.StaticListSerializerBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.util.Pair;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public final class TreeVisualizer {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);

    private static int inputWidth = 1;
    private static int inputHeight = 1;
    private static HashSet<Integer> processedCorrspInputs = new HashSet<>();

    public static void setInputWitdhAndHeight(int width, int height){
        inputWidth = width; inputHeight = height;
    }
    public static int getInputWidth() { return inputWidth; }

    public static GridPane visualizeDecisionPath(TreeDecision decPath){
        GridPane decPathPane = new GridPane();
        TreePath firstPath = decPath.getOuterEdge().getPath(0);
        visualizeEdgePath(decPathPane, firstPath, decPath.getDecInput(), 0);
        visualizePathOutput(decPathPane, decPath.getDecOutput(), decPathPane.getChildren().size());
        return decPathPane;
    }

    private static void visualizePathOutput(GridPane decPathPane, double[] output, int xInd) {
        GridPane outputPane = new GridPane(); outputPane.setAlignment(Pos.CENTER);
        Label outputLabel = new Label("Output:"); outputLabel.setAlignment(Pos.CENTER);
        outputLabel.setMinSize(55, 40); outputLabel.setStyle("-fx-font-weight: bold;");
        outputPane.add(outputLabel,0,0); outputPane.setHalignment(outputLabel, HPos.CENTER);
        GridPane innerOutputPane = new GridPane(); innerOutputPane.setAlignment(Pos.CENTER);
        innerOutputPane.setPadding(new Insets(5,5,5,5));
        innerOutputPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(1.0), new BorderWidths(2.0))));
        innerOutputPane.setHgap(2);
        outputPane.add(innerOutputPane,0,1);
        for (int i = 0; i < output.length; i++){
            Label outLabel = new Label(MathOps.roundOutput.format(output[i]) + " %"); outLabel.setMinSize(60, 30); outLabel.setAlignment(Pos.CENTER);
            Ellipse outBox = new Ellipse(30, 17); outBox.setStroke(Color.BLACK); outBox.setFill(Color.TRANSPARENT);
            innerOutputPane.add(outLabel, i, 0);
            innerOutputPane.add(outBox, i, 0);
        }
        Label blindLabel = new Label(""); outputLabel.setAlignment(Pos.CENTER); outputLabel.setMinSize(55, 20);
        outputPane.add(blindLabel,0,2);
        decPathPane.add(outputPane, xInd, 0);
    }

    private static void visualizeEdgePath(GridPane basePane, TreePath path, double[] inputValues, int yInd){
        processedCorrspInputs.clear();
        int numPredNodes = 0;
        for (int i = 0; i < path.getEdges().length; i++) {
            visualizeDecEdge(basePane, path.getEdges()[i], inputValues, 2, i+numPredNodes, yInd);
            if (i < path.getEdges().length-1) {
                numPredNodes = numPredNodes+1;
                visualizeDecNode(basePane, inputValues, 2, i+numPredNodes, yInd);
            }
        }
    }

    /**
     * Erzeugt die visuelle Ausgabe einer einzelnen Kante.
     *
     * @param edge Die zu visualisierende Kante.
     * @param inputValues Der, zum jeweiligen Entscheidungspfad gehörige und für das vorliegende Netz genormte, Inputvektor. In den Tupeln der ConvertedDecisionList des Netzes gespeichert!
     * @return Eine JavaFX Group zur Visualisierung der angegebenen Kante.
     */
    private static void visualizeDecEdge(GridPane basePane, TreeEdge edge, double[] inputValues, int zoomFactor, int xInd, int yInd){
        GridPane outerPane = new GridPane(); outerPane.setAlignment(Pos.CENTER);
        GridPane innerPane = new GridPane(); innerPane.setAlignment(Pos.CENTER);
        GridPane imagePane = new GridPane();
        ImageView edgeInputView = createInputView(edge.getCorrespInputs(), inputValues, zoomFactor, false);
        imagePane.add(edgeInputView, 0,0); imagePane.setAlignment(Pos.CENTER);
        buildInnerPaneLabelAndControls(innerPane, edge, inputValues, zoomFactor);
        innerPane.setPadding(new Insets(5,5,5,5));
        innerPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(8.0), new BorderWidths(1.0))));
        innerPane.add(imagePane, 0, 1); innerPane.setHalignment(imagePane, HPos.CENTER);
        outerPane.add(innerPane,1,1);
        outerPane.add(edgeSAFLeftLine(),0,1);
        outerPane.add(edgeSAFRightLine(), 2,1);
        Slider zoomSlider = new Slider(2,4,zoomFactor);
        zoomSlider.setShowTickMarks(true); zoomSlider.setShowTickLabels(false);
        zoomSlider.setTooltip(new Tooltip("Zoomfactor for the edge"));
        zoomSlider.setMajorTickUnit(1); zoomSlider.setBlockIncrement(1);
        zoomSlider.setMaxWidth(inputWidth*zoomFactor);
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                int tmpFactor = new_val.intValue();
                ImageView zoomedInputView = createInputView(edge.getCorrespInputs(), inputValues, tmpFactor, false);
                imagePane.getChildren().clear();
                imagePane.add(zoomedInputView,0,0);
            }
        });
        outerPane.add(zoomSlider,1, 0); outerPane.setHalignment(zoomSlider, HPos.CENTER);
        basePane.add(outerPane, xInd, yInd);
        for (int i: edge.getCorrespInputs()) processedCorrspInputs.add(i);
    }
    private static void buildInnerPaneLabelAndControls(GridPane innerPane, TreeEdge edge, double[] inputValues, int zoomFactor) {
        Label edgeLabel = new Label(edge.toString()); edgeLabel.setAlignment(Pos.CENTER);
        edgeLabel.setMinSize(inputWidth*zoomFactor, 20); edgeLabel.setStyle("-fx-font-weight: bold;");
        GridPane expansionPane = new GridPane();
        expansionPane.setPadding(new Insets(5,0,5,0)); expansionPane.setVgap(5);
        innerPane.add(expansionPane,0,3);
        Button edgeButton = new Button("expand");
        edgeButton.setMinSize(inputWidth*zoomFactor, 30);
        Button collapseButton = new Button("collapse");
        collapseButton.setMinSize(inputWidth*zoomFactor, 30);
        collapseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                expansionPane.getChildren().clear();
                innerPane.add(edgeButton,0,2); innerPane.setHalignment(edgeButton, HPos.CENTER);
                innerPane.getChildren().remove(collapseButton);
            }
        });
        innerPane.add(edgeLabel, 0, 0); innerPane.setHalignment(edgeLabel, HPos.CENTER);
        innerPane.add(edgeButton,0,2); innerPane.setHalignment(edgeButton, HPos.CENTER);
        edgeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                innerPane.add(collapseButton,0,2); innerPane.setHalignment(collapseButton, HPos.CENTER);
                innerPane.getChildren().remove(edgeButton);
                for (int i = 0; i < edge.getNumOfPaths(); i++){
                    visualizeEdgePath(expansionPane, edge.getPath(i), inputValues, i);
                }}
        });
    }

    private static void visualizeDecNode(GridPane basePane, double[] inputValues, int zoomFactor, int xInd, int yInd){
        GridPane outerPane = new GridPane(); outerPane.setAlignment(Pos.CENTER);
        GridPane innerPane = new GridPane(); innerPane.setAlignment(Pos.CENTER);
        outerPane.setPadding(new Insets(5,5,5,5));
        outerPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(1.0), new BorderWidths(2.0))));
        Label nodeLabel = new Label("Node " + (xInd/2)); nodeLabel.setAlignment(Pos.CENTER);
        nodeLabel.setMinSize(55, 40); nodeLabel.setStyle("-fx-font-weight: bold;");
        innerPane.add(nodeLabel,0,0); innerPane.setHalignment(nodeLabel, HPos.CENTER);
        GridPane imagePane = new GridPane();
        ImageView nodeInputView = createInputView(processedCorrspInputs, inputValues, zoomFactor, false);
        imagePane.add(nodeInputView, 0,0); imagePane.setAlignment(Pos.CENTER);
        innerPane.add(imagePane,0,1);
        outerPane.add(innerPane, 0 , 0);
        basePane.add(outerPane, xInd, yInd);
    }
    public static ImageView createInputView(HashSet<Integer> correspInputs, double[] inputValues, int zoomFactor, Boolean hideRelInputPixels) {
        ImageView edgeInputView = new ImageView();
        ArrayList<Pair<Integer, Integer>> actRelInputCoords = new ArrayList<>();
        for (Integer i: correspInputs) actRelInputCoords.add(InputVisualizer.transformInputToPixelCoord(i, inputWidth));
        edgeInputView.setImage(InputVisualizer.createInputImage(zoomFactor, hideRelInputPixels, inputWidth, inputHeight, actRelInputCoords, new ArrayList<>(), inputValues));
        return edgeInputView;
    }

    public static GridPane visualizeDecisionTree(TreeNode treeRootNode){
        GridPane decTreePane = new GridPane();
        decTreePane.setPadding(new Insets(5,5,5,5));
        GridPane innerPane = new GridPane(); innerPane.setAlignment(Pos.CENTER);
        Label nodeLabel = new Label("[Root]"); nodeLabel.setAlignment(Pos.CENTER);
        nodeLabel.setMinSize(55, 40); nodeLabel.setStyle("-fx-font-weight: bold;");
        innerPane.add(nodeLabel,0,0); innerPane.setHalignment(nodeLabel, HPos.CENTER);
        GridPane imagePane = new GridPane();
        ImageView nodeInputView = new ImageView();  // STUB --> zu ergänzen
        imagePane.add(nodeInputView, 0,0); imagePane.setAlignment(Pos.CENTER);
        innerPane.setPadding(new Insets(5,5,5,5));
        innerPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(1.0), new BorderWidths(2.0))));
        innerPane.add(imagePane,0,1);
        decTreePane.add(innerPane, 0 , 0);
        ArrayList<TreeEdgeContainer> subTreeContainer = treeRootNode.getAllGuiContainer(1, decTreePane);
        visualizeSubTree(treeRootNode, decTreePane);
        return decTreePane;
    }
    private static void visualizeSubTree(TreeNode rootNode, GridPane containerPane){
        int cnt = 0;
        ArrayList<TreeEdgeContainer> subTreeContainer = rootNode.getAllGuiContainer(1, containerPane);
        for (TreeEdgeContainer c: subTreeContainer){
            visualizeTreeEge(c,2);
            visualizeTreeNode(c, cnt);
            cnt = cnt +1;
        }
    }
    private static void visualizeTreeEge(TreeEdgeContainer edgeCont, int zoomFactor){
        GridPane outerPane = new GridPane(); outerPane.setAlignment(Pos.CENTER);
        GridPane innerPane = new GridPane(); innerPane.setAlignment(Pos.CENTER);
        GridPane imagePane = new GridPane();
        ImageView edgeInputView = createTreeInputView(zoomFactor, edgeCont.getRelInputVariantsForTree().get(0));
        imagePane.add(edgeInputView, 0,0); imagePane.setAlignment(Pos.CENTER);
        buildInnerPaneTreeLabelAndControls(innerPane, edgeCont, zoomFactor);
        ComboBox<String> imageSelector = new ComboBox();
        imageSelector.setMinSize(inputWidth*zoomFactor, 25);
        for (HashSet<PixelInput> inpVar: edgeCont.getRelInputVariantsForTree()) imageSelector.getItems().add(imageSelector.getItems().size()+1 + ". Input");
        imageSelector.getSelectionModel().selectFirst();
        imageSelector.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                ImageView newInputView = createTreeInputView(zoomFactor, edgeCont.getRelInputVariantsForTree().get(imageSelector.getSelectionModel().getSelectedIndex()));
                imagePane.getChildren().clear();
                imagePane.add(newInputView,0,0);

            }
        });
        innerPane.setPadding(new Insets(5,5,5,5));
        innerPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(8.0), new BorderWidths(1.0))));
        innerPane.add(imageSelector, 0, 1); innerPane.setHalignment(imageSelector, HPos.CENTER);
        innerPane.add(imagePane, 0, 2); innerPane.setHalignment(imagePane, HPos.CENTER);
        outerPane.add(innerPane,1,1);
        outerPane.add(edgeSAFLeftLine(),0,1);
        outerPane.add(edgeSAFRightLine(), 2,1);
        Slider zoomSlider = new Slider(2,4,zoomFactor);
        zoomSlider.setShowTickMarks(true); zoomSlider.setShowTickLabels(false);
        zoomSlider.setTooltip(new Tooltip("Zoomfactor for the edge"));
        zoomSlider.setMajorTickUnit(1); zoomSlider.setBlockIncrement(1);
        zoomSlider.setMaxWidth(inputWidth*zoomFactor);
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                int tmpFactor = new_val.intValue();
                ImageView zoomedInputView = createTreeInputView(tmpFactor, edgeCont.getRelInputVariantsForTree().get(imageSelector.getSelectionModel().getSelectedIndex()));
                imagePane.getChildren().clear();
                imagePane.add(zoomedInputView,0,0);
            }
        });
        outerPane.add(zoomSlider,1, 0); outerPane.setHalignment(zoomSlider, HPos.CENTER);
        edgeCont.buildEdgeIcon(outerPane);
    }
    private static void buildInnerPaneTreeLabelAndControls(GridPane innerPane, TreeEdgeContainer edgeCont, int zoomFactor) {
        Label edgeLabel = new Label(edgeCont.getEdgeName()); edgeLabel.setAlignment(Pos.CENTER);
        edgeLabel.setMinSize(inputWidth*zoomFactor, 20); edgeLabel.setStyle("-fx-font-weight: bold;");
        GridPane multiWrapperPane = new GridPane();
        innerPane.add(multiWrapperPane,0,4);
        Button edgeButton = new Button("expand");
        edgeButton.setMinSize(inputWidth*zoomFactor, 25);
        Button collapseButton = new Button("collapse");
        collapseButton.setMinSize(inputWidth*zoomFactor, 25);
        collapseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                multiWrapperPane.getChildren().clear();
                innerPane.add(edgeButton,0,3); innerPane.setHalignment(edgeButton, HPos.CENTER);
                innerPane.getChildren().remove(collapseButton);
            }
        });
        innerPane.add(edgeLabel, 0, 0); innerPane.setHalignment(edgeLabel, HPos.CENTER);
        innerPane.add(edgeButton,0,3); innerPane.setHalignment(edgeButton, HPos.CENTER);
        edgeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (edgeCont.hasRoot()) visualizeSubTree(edgeCont.getRoot(), multiWrapperPane);
                innerPane.add(collapseButton,0,3); innerPane.setHalignment(collapseButton, HPos.CENTER);
                innerPane.getChildren().remove(edgeButton);
            }
        });
    }
    private static void visualizeTreeNode(TreeEdgeContainer edgeCont, int cnt){
        GridPane outerPane = new GridPane(); outerPane.setAlignment(Pos.CENTER);
        GridPane innerPane = new GridPane(); innerPane.setAlignment(Pos.CENTER);
        outerPane.setPadding(new Insets(5,5,5,5));
        outerPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(1.0), new BorderWidths(2.0))));
        Label nodeLabel = new Label("Node [" + cnt + "]"); nodeLabel.setAlignment(Pos.CENTER);
        nodeLabel.setMinSize(55, 40); nodeLabel.setStyle("-fx-font-weight: bold;");
        innerPane.add(nodeLabel,0,0); innerPane.setHalignment(nodeLabel, HPos.CENTER);
        // Bis auf weiteres nicht eingebunden da sehr speicherhungrig -> ggf. anderweitig zu lösen.
/*        GridPane imagePane = new GridPane();
        ImageView nodeInputView = new ImageView();
        imagePane.add(nodeInputView, 0,0); imagePane.setAlignment(Pos.CENTER);
        innerPane.add(imagePane,0,2);
        ComboBox<String> imageSelector = new ComboBox();
        imageSelector.setMinSize(inputWidth*2, 25);
        imageSelector.getItems().add("no Input");
        for (HashSet<PixelInput> inpVar: edgeCont.getSuccNodeInputVariantsForTree()) imageSelector.getItems().add(imageSelector.getItems().size() + ". Input");
        imageSelector.getSelectionModel().selectFirst();
        imageSelector.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                imagePane.getChildren().clear();
                if (imageSelector.getSelectionModel().getSelectedIndex() > 0){
                    ImageView newInputView = createTreeInputView(2, edgeCont.getSuccNodeInputVariantsForTree().get(imageSelector.getSelectionModel().getSelectedIndex()-1));
                    imagePane.add(newInputView,0,0);
                }}
        });
        innerPane.add(imageSelector,0,1);*/
        outerPane.add(innerPane, 0 , 0);
        edgeCont.buildNodeIcon(outerPane);
    }
    public static ImageView createTreeInputView(int zoomFactor, HashSet<PixelInput> inputVar){
        ImageView outInputView = new ImageView();
        outInputView.setImage(InputVisualizer.createInputVariationImage(zoomFactor, inputWidth, inputHeight, inputVar));
        return outInputView;
    }

    private static Polygon edgeSAFLeftLine() {
        Polygon outLine = new Polygon();
        outLine.getPoints().addAll(
                0.0, 40.0,
                20.0, 40.0,
                35.0, 35.0,
                45.0, 15.0,
                50.0, 0.0,
                50.0, 80.0,
                45.0, 65.0,
                35.0, 45.0,
                20.0, 40.0);
        outLine.setStroke(Color.BLACK); outLine.setFill(Color.BLACK);
        return outLine;
    }
    private static Polygon edgeSAFRightLine() {
        Polygon outLine = new Polygon();
        outLine.getPoints().addAll(
                50.0, 40.0,
                30.0, 40.0,
                15.0, 45.0,
                5.0, 65.0,
                0.0, 80.0,
                0.0, 0.0,
                5.0, 15.0,
                15.0, 35.0,
                30.0, 40.0);
        outLine.setStroke(Color.BLACK); outLine.setFill(Color.BLACK);
        return outLine;
    }
}