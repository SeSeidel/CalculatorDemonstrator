package Calculator.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;

public final class InputWindow {

    private static int inputWidth = 1;
    private static int inputHeight = 1;
    private static double[] normedGrayScaleInputValues;
    private static ArrayList<Pair<Integer, Integer>> actRelInputCoords = new ArrayList<>();             // Koordinaten, die relevante Aktivierungen der ersten Schicht abbilden.
    private static ArrayList<Pair<Integer, Integer>> actConInputCoords = new ArrayList<>();             // Koordinaten, die relevante, inaktive Neuronen der ersten Schicht abbilden.
    private static Boolean hideInputPixels = true;
    private static Boolean isShown = false;

    private static ImageView inputPictureView = new ImageView();
    private static Slider zoomSlider = new Slider(1,6,1);
    private static ToggleGroup showRelInputPixelsToggleGroup = new ToggleGroup();
    private static ToggleButton showRelInputPixels = new ToggleButton("show relevant input pixels contributing activations");
    private static ToggleButton hideRelInputPixels = new ToggleButton("hide relevant input pixels contributing activations");
    private static BorderPane bottomPane = new BorderPane();
    private static BorderPane organiserPane = new BorderPane();
    private static Scene inputWindowScene = new Scene(organiserPane);
    public static Stage inputWindowStage = new Stage();

    public static void initialize() {
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setTooltip(new Tooltip("Zoomfactor for the visualization of the internal Input"));
        zoomSlider.setMajorTickUnit(1);
        zoomSlider.setBlockIncrement(1);
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                int tmp = new_val.intValue();
                showActInternalInput(tmp, hideInputPixels);
            }
        });
        showRelInputPixels.setToggleGroup(showRelInputPixelsToggleGroup);
        showRelInputPixels.setMinSize(160, 15);
        showRelInputPixels.setTooltip(new Tooltip("Shows input pixels contributing activation value to the neuron displayed in the information window.\nInput neurons show their related pixel."));
        showRelInputPixels.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                hideInputPixels = false;
                showActInternalInput((int)zoomSlider.getValue(), hideInputPixels);
            }
        });
        hideRelInputPixels.setToggleGroup(showRelInputPixelsToggleGroup);
        hideRelInputPixels.setMinSize(160, 15);
        hideRelInputPixels.setTooltip(new Tooltip("Hides input pixels contributing activation value the neuron displayed in the information window."));
        hideRelInputPixels.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                hideInputPixels = true;
                showActInternalInput((int)zoomSlider.getValue(), hideInputPixels);
            }
        });
        organiserPane.setTop(zoomSlider);
        organiserPane.setCenter(inputPictureView);
        bottomPane.setLeft(showRelInputPixels);
        bottomPane.setRight(hideRelInputPixels);
        organiserPane.setBottom(bottomPane);
        inputWindowStage.setScene(inputWindowScene); inputWindowStage.setTitle("Internal input for current chosen neuron");
    }

    public static Boolean isShown () { return isShown; }

    public static void setNewNormedGrayScaleInputValues(double[] values, int lenght, int height) {
        inputWidth = lenght; inputHeight = height;
        normedGrayScaleInputValues = values;
    }

    public static void resetActRelInputCoords(Integer relInputNum, Boolean isActive) {            // Bei Auswahl eines Neurons der ersten Schicht.
        actRelInputCoords.clear(); actConInputCoords.clear();
        if (isActive) actRelInputCoords.add(InputVisualizer.transformInputToPixelCoord(relInputNum, inputWidth));
        else actConInputCoords.add(InputVisualizer.transformInputToPixelCoord(relInputNum, inputWidth));
    }
    public static void resetActRelInputCoords(HashSet<Integer> relInputNums, HashSet<Integer> conInputNums) {  // Bei Auswahl eines Neurons nicht aus der ersten Schicht.
        actRelInputCoords.clear(); actConInputCoords.clear();
        for (Integer neuronNum: relInputNums) { actRelInputCoords.add(InputVisualizer.transformInputToPixelCoord(neuronNum, inputWidth)); }
        for (Integer neuronNum: conInputNums) { actConInputCoords.add(InputVisualizer.transformInputToPixelCoord(neuronNum, inputWidth)); }
    }

    public static void showInputWindow() {
        hideInputPixels = true;
        organiserPane.setPrefSize(inputWidth*zoomSlider.getValue()+150, inputHeight*zoomSlider.getValue()+100);
        showActInternalInput((int)zoomSlider.getValue(), hideInputPixels);
        hideRelInputPixels.setSelected(true);
        isShown = true;
        inputWindowStage.show();
    }
    public static void hideInputWindow() { isShown = false; inputWindowStage.hide(); }

    public static void resetInputPicture() { showActInternalInput((int)zoomSlider.getValue(), hideInputPixels); }

    private static void showActInternalInput(int zoomFactor, Boolean hideRelInputPixels) {
        inputPictureView.setImage(InputVisualizer.createInputImage(zoomFactor, hideRelInputPixels, inputWidth, inputHeight, actRelInputCoords, actConInputCoords, normedGrayScaleInputValues));
    }
}
