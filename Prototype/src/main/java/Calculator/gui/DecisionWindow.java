package Calculator.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class DecisionWindow {

    private static BorderPane decWindowBasePane = new BorderPane();
    private static Scene decWindowScene = new Scene(decWindowBasePane, 1800, 600);
    public static Stage decWindowStage = new Stage();
    private static ScrollPane orgScrollPane = new ScrollPane();

    public static void initialize() {
        decWindowStage.setScene(decWindowScene); decWindowStage.setTitle("chosen derived decisionpath");
        decWindowBasePane.setCenter(orgScrollPane);
        orgScrollPane.setPadding(new Insets(25, 25, 25, 25));
    }

    public static void showDecisionPath(GridPane edgeVisualization){
        orgScrollPane.setContent(edgeVisualization);
        decWindowStage.show();
    }
}
