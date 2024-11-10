package Calculator.element_types;

/**
 * Enum zur Nachbildung des gewählten Paddings aus Keras.
 */
public enum PaddingType {
    NONE(""),
    VALID_PADDING("valid"),
    SAME_PADDING("same"),
    CAUSAL_PADDING("causal");

    private final String kerasName;

    PaddingType(String kerasName) {
        this.kerasName = kerasName;
    }

    public String getKerasName() {
        return kerasName;
    }
}

