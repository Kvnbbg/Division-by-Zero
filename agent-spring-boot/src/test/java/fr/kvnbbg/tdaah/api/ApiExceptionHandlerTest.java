package fr.kvnbbg.tdaah.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.kvnbbg.tdaah.domain.DivisionRuleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * La politique d'erreurs est vérifiée sur la surface HTTP réelle : plusieurs de
 * ces cas sont levés par Spring AVANT que le contrôleur ne s'exécute, et un
 * test unitaire sur les méthodes du gestionnaire ne les verrait jamais.
 */
@WebMvcTest(DivisionController.class)
@Import({ApiExceptionHandler.class, DivisionRuleService.class})
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("corps JSON illisible : 400 avec un code stable, pas la page Spring")
    void corpsIllisible() throws Exception {
        mvc.perform(post("/v1/exercise-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ ceci n'est pas du JSON"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("malformed_body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("champ obligatoire absent : 422 qui NOMME le champ manquant")
    void champManquant() throws Exception {
        mvc.perform(post("/v1/exercise-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numerator\":10}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("denominator")));
    }

    @Test
    @DisplayName("division par zéro : 422, et jamais Infinity")
    void divisionParZero() throws Exception {
        mvc.perform(post("/v1/exercise-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numerator\":10,\"denominator\":0}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("une division valide passe toujours")
    void divisionValide() throws Exception {
        mvc.perform(post("/v1/exercise-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numerator\":10,\"denominator\":2}"))
                .andExpect(status().isOk());
    }
}
