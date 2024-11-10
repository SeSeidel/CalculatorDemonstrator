package Calculator.element_types;

/**
 * Enum zur Nachbildung des InitializerType aus Keras.
 */
public enum InitializerType {
    NONE(""),
    ZEROS("Zeros"),
    ONES("Ones"),
    CONSTANT("Constant"),
    RANDOM_NORMAL("RandomNormal"),
    RANDOM_UNIFORM("RandomUniform"),
    TRUNCATED_NORMAL("TruncatedNormal"),
    VARIANCE_SCALING("VarianceScaling"),
    ORTHOGONAL("Orthogonal"),
    IDENTITY("Identity"),
    GLOROT_NORMAL("GlorotNormal"),
    GLOROT_UNIFORM("GlorotUniform"),
    HE_NORMAL("HeNormal"),
    HE_UNIFORM("HeUniform"),
    LECUN_NORMAL("LecunNormal"),
    LECUN_UNIFORM("LecunUniform");

    private final String kerasName;

    InitializerType(String kerasName) {
        this.kerasName = kerasName;
    }

    public String getKerasName() {
        return kerasName;
    }
}
