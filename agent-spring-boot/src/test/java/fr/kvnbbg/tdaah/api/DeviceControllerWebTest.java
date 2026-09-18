package fr.kvnbbg.tdaah.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.kvnbbg.tdaah.nativebridge.NativeBridge;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DeviceController.class)
@Import(CorrelationIdFilter.class)
class DeviceControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NativeBridge nativeBridge;

    @Test
    @DisplayName("les helpers exposent uniquement la surface allowlistée")
    void helpersContract() throws Exception {
        when(nativeBridge.allowlist()).thenReturn(List.of("BUS_INVENTORY", "AI_BOOTSTRAP"));

        mvc.perform(get("/v1/device/helpers").header("X-Correlation-Id", "device-helpers"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "device-helpers"))
                .andExpect(jsonPath("$.mode").value("authorized-read"))
                .andExpect(jsonPath("$.allowlist[0]").value("BUS_INVENTORY"))
                .andExpect(jsonPath("$.allowlist[1]").value("AI_BOOTSTRAP"));
    }

    @Test
    @DisplayName("l'inventaire bus délègue uniquement au helper prévu")
    void busContract() throws Exception {
        when(nativeBridge.run(NativeBridge.Helper.BUS_INVENTORY))
                .thenReturn(Map.of("ok", true, "helper", "bus_inventory", "exitCode", 0, "output", "ok
"));

        mvc.perform(get("/v1/device/bus").header("X-Correlation-Id", "device-bus"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "device-bus"))
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.helper").value("bus_inventory"));

        verify(nativeBridge).run(NativeBridge.Helper.BUS_INVENTORY);
    }
}
