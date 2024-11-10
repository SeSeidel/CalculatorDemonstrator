package Calculator.element_types;

/**
 * Enum zur Nachbildung des ActiovationType aus Keras.
 */
public enum ActivationType {
    NONE(""),
    RELU("relu"),
    SIGMOID("sigmoid"),
    SOFTMAX("softmax"),
    SOFTPLUS("softplus"),
    SOFTSIGN("softsign"),
    TANH("tanh"),
    SELU("selu"),
    ELU("elu"),
    EXPONENTIAL("exponential"),
    HARD_SIGMOID("hard_sigmoid"),
    SWISH("swish"),
    LINEAR("linear");

    private final String kerasName;

    ActivationType(String kerasName) {
        this.kerasName = kerasName;
    }

    public String getKerasName() {
        return kerasName;
    }
}
