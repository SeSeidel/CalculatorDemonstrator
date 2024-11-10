package Calculator.general;

import Calculator.net_elements.NeuralNet;
import Calculator.tree_elements.PixelInput;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Calculator.gui.MainMenu;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class Main extends Application{

    public void start(Stage mainStage) {

        Scene scene = new Scene(MainMenu.mainPane(), 1800, 800);
        mainStage.setTitle("NeuralVisualizer");
        mainStage.setScene(scene);
        mainStage.show();
    }

    public static void main(String[] args){
        System.out.println("-starting process-\n");
        MapperService testMapper = new MapperService();
        NeuralNet testNet2 = testMapper.getNeuralNet("."+ File.separator +"src"+ File.separator +"testnet"+ File.separator +"digitWithCNN.h5");
        MainMenu.drawNetMainWindow(testNet2);
        System.out.println("--------------------------------------------");
        launch(args);
    }
}
