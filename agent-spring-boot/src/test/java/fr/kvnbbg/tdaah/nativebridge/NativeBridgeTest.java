package fr.kvnbbg.tdaah.nativebridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NativeBridgeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void allowlistMatchesCanonicalFixture() throws Exception {
        try (InputStream input =
                getClass().getResourceAsStream("/native-helper-fixture.json")) {
            Map<String, List<String>> fixture =
                    mapper.readValue(input, new TypeReference<>() {});
            NativeBridge bridge = new NativeBridge("tools", 1);

            assertEquals(fixture.get("allowlist"), bridge.allowlist());
            assertEquals(
                    List.of(NativeBridge.Helper.BUS_INVENTORY.binary(),
                            NativeBridge.Helper.AI_BOOTSTRAP.binary()),
                    fixture.get("binaries"));
        }
    }

    @Test
    void missingHelperIsReportedWithoutStartingAProcess() throws Exception {
        Path missingDir = Files.createTempDirectory("tdaah-native-missing");
        try {
            NativeBridge bridge = new NativeBridge(missingDir.toString(), 1);
            Map<String, Object> result = bridge.run(NativeBridge.Helper.BUS_INVENTORY);

            assertEquals(false, result.get("ok"));
            assertEquals("BUS_INVENTORY", result.get("helper"));
            assertTrue(result.get("message").toString().contains("Binary missing"));
        } finally {
            Files.deleteIfExists(missingDir);
        }
    }

    @Test
    void executableHelperReturnsItsExitCodeAndOutput() throws Exception {
        Path dir = Files.createTempDirectory("tdaah-native-exec");
        Path helper = dir.resolve(NativeBridge.Helper.BUS_INVENTORY.binary());
        try {
            Files.writeString(helper, "#!/bin/sh\nprintf 'inventory-ok\\n'\n");
            assertTrue(helper.toFile().setExecutable(true));

            NativeBridge bridge = new NativeBridge(dir.toString(), 2);
            Map<String, Object> result = bridge.run(NativeBridge.Helper.BUS_INVENTORY);

            assertEquals(true, result.get("ok"));
            assertEquals("bus_inventory", result.get("helper"));
            assertEquals(0, result.get("exitCode"));
            assertEquals("inventory-ok\n", result.get("output"));
        } finally {
            Files.deleteIfExists(helper);
            Files.deleteIfExists(dir);
        }
    }
}
