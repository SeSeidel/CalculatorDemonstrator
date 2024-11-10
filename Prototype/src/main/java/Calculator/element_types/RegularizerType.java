package Calculator.element_types;

/**
 * Enum zur Nachbildung des RegularizerType aus Keras.
 */
public enum RegularizerType {
    NONE(""),
    L1("L1"),
    L2("L2"),
    L1_L2("L1L2");

    private final String kerasName;

    RegularizerType(String kerasName) {
        this.kerasName = kerasName;
    }

    public String getKerasName() {
        return kerasName;
    }
}
