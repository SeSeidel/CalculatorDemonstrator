package Calculator.general;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import Calculator.gui.InputWindow;
import Calculator.gui.TreeVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InputLoader {

    private static final Logger logger = LoggerFactory.getLogger(MapperService.class);

    private static double actInputMin = 0.0;
    private static double rangeFactor = 1.0;

    private static int structInputWidth = 0;
    private static int structInputHeight = 0;

    public static void setInputWidthAndHeight(int width, int height) {
        structInputWidth = width;
        structInputHeight = height;
    }

    public static boolean testInputWidthAndHeigth(String filePath){
        BufferedImage inputImage = createNewBufferedImage(filePath);
        return (inputImage.getWidth() == structInputWidth && inputImage.getHeight() == structInputHeight);
    }

    public static double[] loadImageAsGrayScaleArray(String filePath, double inputMin, double inputMax) {
        BufferedImage inputImage = createNewBufferedImage(filePath);
        actInputMin = inputMin;
        rangeFactor = inputMax - actInputMin;
        double[] out = normGrayScaleValuesToZeroOne(grayScaleFromRGBValues(extractAllRGBValues(inputImage), filePath), inputImage.getWidth(), inputImage.getHeight());
        TreeVisualizer.setInputWitdhAndHeight(inputImage.getWidth(), inputImage.getHeight());
        return out;
    }

    private static BufferedImage createNewBufferedImage(String path) {
        BufferedImage outImage = null;
        try {
            File file = new File(path);
            outImage = ImageIO.read(file);
        } catch (IOException e) {
            logger.error("Verarbeitungsfehler! Die Bilddatei " + path + " konnte nicht gelesen werden.");
        }
        return outImage;
    }

    private static int[] extractAllRGBValues(BufferedImage bufferedImage) {
        int imageLength = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();
        int [] out = new int[imageLength*imageHeight];
        for (int j = 0; j < imageHeight; j++) {
            for (int i = 0; i < imageLength; i++) {
                out[i+(imageLength*j)] = bufferedImage.getRGB(i, j);
            }
        }
        return out;
    }

    private static int[] grayScaleFromRGBValues(int[] combinedRGBValues, String filePath) {      // Ohne den Wert des Alpha-Kanals.
        int vectorLength = combinedRGBValues.length;
        int[] intermediate = new int[3];
        int[] out = new int[vectorLength];
        for (int i = 0; i < vectorLength; i++) {
            intermediate[2] = (combinedRGBValues[i] >> 16) & 0xff;    // Roter RGB-Wert des Pixels i.
            intermediate[1] = (combinedRGBValues[i] >> 8) & 0xff;     // Grüner RGB-Wert des Pixels i.
            intermediate[0] = (combinedRGBValues[i]) & 0xff;          // Blauer RGB-Wert des Pixels i.
            if (intermediate[0] == intermediate[1] && intermediate[1] == intermediate[2]) { out[i] = intermediate[0]; }
            else {
                out[i] = (intermediate[0] + intermediate[1] + intermediate[2]) / 3;     // Vereinfachte Umwandlung, ggf noch verbessern!
                logger.info("Achtung! Die Bilddatei " + filePath + " ist kein Graustufenbild und wurde vereinfacht in ein solches umgewandelt.");
            }
        }
        return out;
    }

    private static double[] normGrayScaleValuesToZeroOne(int[] grayScaleValues, int inputWidth, int inputHeight) { // Entspricht der Normierung auf eine Spanne zischen 2 Werten.
        double[] out = new double[grayScaleValues.length];                                                                                        // Für TF i.d.R. zwischen 0 und 1.
        for (int i = 0; i < grayScaleValues.length; i++) {
            out[i] = (grayScaleValues[i] / 255.0 * rangeFactor) + actInputMin;
        }
        InputWindow.setNewNormedGrayScaleInputValues(out, inputWidth, inputHeight);
        return out;
    }

    public static int denormToStandardGrayScaleValue (double normedValue) {
        return (int)Math.round((normedValue - actInputMin) * 255 / rangeFactor);
    }
}
