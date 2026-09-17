package fr.kvnbbg.tdaah.nativebridge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Allowlisted launcher for local C helpers. Never passes untrusted strings to a shell.
 * Authorized enterprise use only.
 */
@Service
public class NativeBridge {

    public enum Helper {
        BUS_INVENTORY("bus_inventory"),
        AI_BOOTSTRAP("ai_bootstrap");

        private final String binary;

        Helper(String binary) {
            this.binary = binary;
        }

        public String binary() {
            return binary;
        }
    }

    private final Path toolsDir;
    private final Duration timeout;

    public NativeBridge(
            @Value("${tdaah.native.tools-dir:tools}") String toolsDir,
            @Value("${tdaah.native.timeout-seconds:15}") long timeoutSeconds) {
        this.toolsDir = Path.of(toolsDir).toAbsolutePath().normalize();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    public Map<String, Object> run(Helper helper) throws Exception {
        Path bin = toolsDir.resolve(helper.binary()).normalize();
        if (!bin.startsWith(toolsDir) || !Files.isExecutable(bin)) {
            return Map.of(
                    "ok", false,
                    "helper", helper.name(),
                    "message", "Binary missing or not executable: " + bin);
        }
        ProcessBuilder pb = new ProcessBuilder(bin.toString());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        boolean finished = p.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            return Map.of("ok", false, "helper", helper.name(), "message", "timeout");
        }
        StringBuilder out = new StringBuilder();
        try (BufferedReader r =
                new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lines = 0;
            while ((line = r.readLine()) != null && lines++ < 200) {
                out.append(line).append('\n');
            }
        }
        return Map.of(
                "ok", p.exitValue() == 0,
                "helper", helper.name().toLowerCase(Locale.ROOT),
                "exitCode", p.exitValue(),
                "output", out.toString());
    }

    public List<String> allowlist() {
        return List.of(Helper.BUS_INVENTORY.name(), Helper.AI_BOOTSTRAP.name());
    }
}
