module dev.backCalculator.nn {
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires io.jhdf;
    requires javafx.controls;
    requires java.desktop;

    exports Calculator.net_elements;
    exports Calculator.element_types;
    exports Calculator.general;
}
