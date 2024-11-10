package Calculator.element_types;

/**
 * Enum zur Nachbildung des ConstraintType aus Keras.
 */
public enum ConstraintType {
    NONE(""),
    MAXNORM("MaxNorm"),
    NONNEG("NonNeg"),
    UNITNORM("UnitNorm"),
    MINMAXNORM("MinMaxNorm");

    private final String kerasName;

    ConstraintType(String kerasName) {
        this.kerasName = kerasName;
    }

    public String getKerasName() {
        return kerasName;
    }
}
