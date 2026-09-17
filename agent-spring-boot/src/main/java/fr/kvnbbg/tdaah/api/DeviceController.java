package fr.kvnbbg.tdaah.api;

import fr.kvnbbg.tdaah.nativebridge.NativeBridge;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only device helpers for enterprise support. No privilege escalation.
 */
@RestController
@RequestMapping("/v1/device")
public class DeviceController {

    private final NativeBridge nativeBridge;

    public DeviceController(NativeBridge nativeBridge) {
        this.nativeBridge = nativeBridge;
    }

    @GetMapping("/helpers")
    public Map<String, Object> helpers() {
        return Map.of(
                "author", "Kevin Marville",
                "mode", "authorized-read",
                "allowlist", nativeBridge.allowlist(),
                "note", "Presence inventory and bootstrap only; not a penetration toolkit.");
    }

    @GetMapping("/bus")
    public ResponseEntity<Map<String, Object>> bus() throws Exception {
        return ResponseEntity.ok(nativeBridge.run(NativeBridge.Helper.BUS_INVENTORY));
    }
}
