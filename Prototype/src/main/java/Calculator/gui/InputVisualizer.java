package Calculator.gui;

import Calculator.general.InputLoader;
import Calculator.tree_elements.PixelInput;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public final class InputVisualizer {

    public static Pair<Integer, Integer> transformInputToPixelCoord(int inputIndex, int inputWidth) { return new Pair<>(inputIndex%inputWidth, inputIndex/inputWidth); }

    public static WritableImage createInputImage(int zoomFactor, Boolean hideRelInputPixels, int inputWidth, int inputHeight, ArrayList<Pair<Integer, Integer>> actRelInputCoords, ArrayList<Pair<Integer, Integer>> actConInputCoords, double[] inputValues) {
        Color pixelColor;
        int colorValue;
        WritableImage outImage = new WritableImage(inputWidth*zoomFactor, inputHeight*zoomFactor);
        for (int j = 0; j < inputHeight; j++){
            for (int i = 0; i < inputWidth; i++){
                colorValue = InputLoader.denormToStandardGrayScaleValue(inputValues[i+(inputWidth *j)]);
                if (hideRelInputPixels) pixelColor = createPixelColor(colorValue, false, false); // Wenn das gesamte Eingabebild angezeigt werden soll!
                else {                                                                                                  // Wenn nur die relevanten Pixel für das gewählte Neurons angezeigt werden sollen!
                    Pair<Integer, Integer> actCoords = new Pair<>(i,j);
                    if (actRelInputCoords.contains(actCoords)) pixelColor = createPixelColor(colorValue, false, false);
                    else if (actConInputCoords.contains(actCoords)) pixelColor = createPixelColor(colorValue, true, false);
                    else pixelColor = createPixelColor(colorValue, false, true);
                }
                for (int zy = 0; zy < zoomFactor; zy++){
                    for (int zx = 0; zx < zoomFactor; zx++){
                        outImage.getPixelWriter().setColor(i*zoomFactor+zx ,j*zoomFactor+zy , pixelColor);
                    }}}}
        return outImage;
    }

    public static WritableImage createInputVariationImage(int zoomFactor, int inputWidth, int inputHeight, HashSet<PixelInput> relInputVariant){
        Color[][] pixels = new Color[inputWidth][inputHeight];
        Color pixelColor = Color.rgb(64, 224, 208, 0.20);
        for (Color[] subArr: pixels) Arrays.fill(subArr, pixelColor);
        WritableImage outImage = new WritableImage(inputWidth*zoomFactor, inputHeight*zoomFactor);
        for (PixelInput inp: relInputVariant) pixels[inp.getxInd()][inp.getyInd()] = createPixelColor(InputLoader.denormToStandardGrayScaleValue(inp.getValue()), false, false);
        for (int y = 0; y < inputHeight; y++){
            for (int x = 0; x < inputWidth; x++){
                pixelColor = pixels[x][y];
                for (int zy = 0; zy < zoomFactor; zy++){
                    for (int zx = 0; zx < zoomFactor; zx++){
                        outImage.getPixelWriter().setColor(x*zoomFactor+zx, y*zoomFactor+zy, pixelColor);
                    }}}}
        return outImage;
    }

    private static Color createPixelColor (int colorValue, Boolean tealSplash, Boolean alphaComponent) {
        if (tealSplash) return Color.rgb(64, 224, 208, 0.20);
        else if (alphaComponent) return Color.rgb(colorValue/2, colorValue/2, colorValue/2, 0.20);
        else return Color.rgb(colorValue, colorValue, colorValue);
    }
}
