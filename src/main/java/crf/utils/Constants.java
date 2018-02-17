package crf.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class Constants {

// Patterns
    public final static Pattern patternAttr = Pattern.compile("\\[[[0-9][0-9]?]\\]=");
    public final static Pattern patternNumeric = Pattern.compile("-?\\d+(,\\d+)?(\\.\\d+)?");

// Numbers

    // Number of tags to remember and check in path
    public final static int TAGS_NUMBER = 6;

    // Gs and Fs in CRF that are not checking tags
    public final static int EXTRA_G_FUNCTIONS = 4;
    public final static int EXTRA_F_FUNCTIONS = 0;

    // Gradient descent coefficient
    public final static Double GRAD_DESC_COEF = 0.0008;
    // Number of steps to train
    public final static int GRAD_DESC_STEPS = 500;

// Files
    private final static String FILE_OUTPUT = "crf_output\\";
    private final static String FILE_INPUT = "crf_input\\";
    private final static String FILE_RESOURCES = "crf_resources\\";
    public final static String COEFFS_FILE = FILE_RESOURCES+ "crf_coeffs.txt";
    public final static String SORTED_TRAINING_TAGS = FILE_RESOURCES + "crf_sorted_tags.txt";
    public final static String ALL_TRAINING_TAGS = FILE_RESOURCES + "crf_all_tags.txt";
    public final static String LABELING_RES_FILE = FILE_OUTPUT + "crf_label_result.txt";
    public final static String TRAIN_DATA_FILE = FILE_INPUT + "train_crf.txt";
    public final static String TEST_DATA_FILE = FILE_INPUT + "test_crf.txt";
    public final static String LABELS_FILE = FILE_INPUT + "labels.txt";

// Dates and times
    public final static DateFormat[] DATE_PATTERNS = new SimpleDateFormat[]{
            new SimpleDateFormat("dd[.|-]mm:yyyy"), new SimpleDateFormat("dd[.|-]mm"),
        new SimpleDateFormat("mm[.|-]dd[.|-]yyyy"), new SimpleDateFormat("mm[.|-]dd")};
    public final static Pattern[] TIME_PATTERNS = new Pattern[]{
            Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]"),
            Pattern.compile("(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)")
    };
}
