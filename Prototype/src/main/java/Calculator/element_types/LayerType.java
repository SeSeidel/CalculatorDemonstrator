package Calculator.element_types;

/**
 * Enum zur Nachbildung des LayerType aus Keras.
 */
public enum LayerType {
    NONE(""),

    CORE_DENSE("Dense"),
    CORE_ACTIVATION("Activation"),
    CORE_DROPOUT("Dropout"),
    CORE_FLATTEN("Flatten"),
    CORE_RESHAPE("Reshape"),
    CORE_MERGE("Merge"),
    CORE_PERMUTE("Permute"),
    CORE_REPEAT_VECTOR("RepeatVector"),
    CORE_ACTIVITY_REGULARIZATION("ActivityRegularization"),
    CORE_EMBEDDING("SpatialDropout1D"),
    CORE_MASKING("SpatialDropout2D"),
    CORE_LAMBDA("SpatialDropout3D"),

    CONVOLUTION_CONV_1D("Conv1D"),
    CONVOLUTION_CONV_2D("Conv2D"),
    CONVOLUTION_CONV_3D("Conv3D"),
    CONVOLUTION_ATROUS_1D("AtrousConvolution1D"),
    CONVOLUTION_ATROUS_2D("AtrousConvolution2D"),
    CONVOLUTION_CROPPING_1D("Cropping1D"),
    CONVOLUTION_CROPPING_2D("Cropping2D"),
    CONVOLUTION_CROPPING_3D("Cropping3D"),
    CONVOLUTION_UP_SAMPLING_1D("UpSampling1D"),
    CONVOLUTION_UP_SAMPLING_2D("UpSampling2D"),
    CONVOLUTION_UP_SAMPLING_3D("UpSampling3D"),
    CONVOLUTION_ZERO_PADDING_1D("ZeroPadding1D"),
    CONVOLUTION_ZERO_PADDING_2D("ZeroPadding2D"),
    CONVOLUTION_ZERO_PADDING_3D("ZeroPadding3D"),
    CONVOLUTION_SEPARABLECONV_1D("SeparableConv1D"),
    CONVOLUTION_SEPARABLECONV_2D("SeparableConv2D"),
    CONVOLUTION_DEPTHWISECONV_2D("DepthwiseConv2D"),
    CONVOLUTION_TRANSPOSE_2D("Conv2DTranspose"),
    CONVOLUTION_TRANSPOSE_3D("Conv3DTranspose"),

    POOLING_MAX_1D("MaxPooling1D"),
    POOLING_MAX_2D("MaxPooling2D"),
    POOLING_MAX_3D("MaxPooling3D"),
    POOLING_AVERAGE_1D("AveragePooling1D"),
    POOLING_AVERAGE_2D("AveragePooling2D"),
    POOLING_AVERAGE_3D("AveragePooling3D"),
    POOLING_GLOBALMAX_1D("GlobalMaxPooling1D"),
    POOLING_GLOBALMAX_2D("GlobalMaxPooling2D"),
    POOLING_GLOBALMAX_3D("GlobalMaxPooling3D"),
    POOLING_GLOBALAVERAGE_1D("GlobalAveragePooling1D"),
    POOLING_GLOBALAVERAGE_2D("GlobalAveragePooling2D"),
    POOLING_GLOBALAVERAGE_3D("GlobalAveragePooling3D"),

    ADVANCED_LEAKYRELU("LeakyReLU"),
    ADVANCED_PRELU("PReLU"),
    ADVANCED_ELU("ELU"),
    ADVANCED_THRESHOLDEDRELU("ThresholdedReLU"),

    NORMALIZATION_BATCH_NORMALIZATION("BatchNormalization");

    private final String kerasName;

    LayerType(String kerasName) {
        this.kerasName = kerasName;
    }

    public String getKerasName() {
        return kerasName;
    }
}
