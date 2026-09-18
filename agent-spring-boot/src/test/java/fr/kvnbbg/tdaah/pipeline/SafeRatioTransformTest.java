package fr.kvnbbg.tdaah.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SafeRatioTransformTest {

    private final SafeRatioTransform transform = new SafeRatioTransform();

    @Test
    void computesFiniteRatio() {
        var result = transform.apply(
                new PipelineModels.PipelineRecord(Map.of("numerator", 10.0, "denominator", 4.0)),
                "numerator",
                "denominator");

        assertEquals(2.5, result.orElseThrow().fields().get("ratio"));
    }

    @Test
    void rejectsZeroDenominator() {
        assertThrows(
                ZeroDivisionMeasurementException.class,
                () -> transform.apply(
                        new PipelineModels.PipelineRecord(Map.of("numerator", 10.0, "denominator", 0.0)),
                        "numerator",
                        "denominator"));
    }

    @Test
    void rejectsNaNNumerator() {
        assertThrows(
                NonFiniteMeasurementException.class,
                () -> transform.apply(
                        new PipelineModels.PipelineRecord(Map.of("numerator", Double.NaN, "denominator", 2.0)),
                        "numerator",
                        "denominator"));
    }

    @Test
    void rejectsInfiniteDenominator() {
        assertThrows(
                NonFiniteMeasurementException.class,
                () -> transform.apply(
                        new PipelineModels.PipelineRecord(Map.of("numerator", 1.0, "denominator", Double.POSITIVE_INFINITY)),
                        "numerator",
                        "denominator"));
    }

    @Test
    void rejectsOverflowingRatio() {
        assertThrows(
                NonFiniteMeasurementException.class,
                () -> transform.apply(
                        new PipelineModels.PipelineRecord(Map.of("numerator", 1e308, "denominator", 1e-308)),
                        "numerator",
                        "denominator"));
    }

    @Test
    void applyOrSkipSkipsNonFiniteAndZero() {
        var record = new PipelineModels.PipelineRecord(Map.of("numerator", 1.0, "denominator", 0.0));
        assertEquals(java.util.Optional.empty(), transform.applyOrSkip(record, "numerator", "denominator"));

        record = new PipelineModels.PipelineRecord(Map.of("numerator", Double.NaN, "denominator", 2.0));
        assertEquals(java.util.Optional.empty(), transform.applyOrSkip(record, "numerator", "denominator"));
    }
}
