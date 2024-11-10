package Calculator.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class TreeWindow {

    private static BorderPane treeWindowBasePane = new BorderPane();
    private static Scene treeWindowScene = new Scene(treeWindowBasePane, 1800, 600);
    public static Stage treeWindowStage = new Stage();
    private static ScrollPane orgScrollPane = new ScrollPane();

    public static void initialize() {
        treeWindowStage.setScene(treeWindowScene); treeWindowStage.setTitle("derived decisiontree");
        treeWindowBasePane.setCenter(orgScrollPane);
        orgScrollPane.setPadding(new Insets(25, 25, 25, 25));
    }

    public static void showDecisionPath(GridPane edgeVisualization){
        orgScrollPane.setContent(edgeVisualization);
        treeWindowStage.show();
    }
}
