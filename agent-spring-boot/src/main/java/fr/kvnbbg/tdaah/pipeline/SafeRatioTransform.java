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
        double n = toDouble(num);
        double d = toDouble(den);
        if (d == 0.0d) {
            throw new ZeroDivisionMeasurementException(denominatorField);
        }
        Map<String, Object> out = new LinkedHashMap<>(in.fields());
        out.put("ratio", n / d);
        return Optional.of(new PipelineModels.PipelineRecord(out));
    }

    public Optional<PipelineModels.PipelineRecord> applyOrSkip(
            PipelineModels.PipelineRecord in, String numeratorField, String denominatorField) {
        try {
            return apply(in, numeratorField, denominatorField);
        } catch (ZeroDivisionMeasurementException ex) {
            return Optional.empty();
        }
    }

    private static double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0.0d;
        }
        return Double.parseDouble(value.toString());
    }
}
