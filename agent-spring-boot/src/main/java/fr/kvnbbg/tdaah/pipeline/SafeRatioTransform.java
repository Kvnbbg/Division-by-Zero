package fr.kvnbbg.tdaah.pipeline;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SafeRatioTransform {

    public Optional<PipelineModels.PipelineRecord> apply(
            PipelineModels.PipelineRecord in, String numeratorField, String denominatorField) {
        Object num = in.fields().get(numeratorField);
        Object den = in.fields().get(denominatorField);
        double n = toFiniteDouble(num, numeratorField);
        double d = toFiniteDouble(den, denominatorField);

        if (d == 0.0d) {
            throw new ZeroDivisionMeasurementException(denominatorField);
        }

        double ratio = n / d;
        if (!Double.isFinite(ratio)) {
            throw new NonFiniteMeasurementException("ratio", ratio);
        }

        Map<String, Object> out = new LinkedHashMap<>(in.fields());
        out.put("ratio", ratio);
        return Optional.of(new PipelineModels.PipelineRecord(out));
    }

    public Optional<PipelineModels.PipelineRecord> applyOrSkip(
            PipelineModels.PipelineRecord in, String numeratorField, String denominatorField) {
        try {
            return apply(in, numeratorField, denominatorField);
        } catch (ZeroDivisionMeasurementException | NonFiniteMeasurementException | NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static double toFiniteDouble(Object value, String field) {
        if (value == null) {
            throw new NonFiniteMeasurementException(field, Double.NaN);
        }

        final double parsed;
        if (value instanceof Number number) {
            parsed = number.doubleValue();
        } else {
            parsed = Double.parseDouble(value.toString());
        }

        if (!Double.isFinite(parsed)) {
            throw new NonFiniteMeasurementException(field, parsed);
        }
        return parsed;
    }
}
