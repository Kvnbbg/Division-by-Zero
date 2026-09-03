package fr.kvnbbg.tdaah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du service.
 *
 * <p>Il manquait : le module n'avait qu'un contrôleur, sans application pour le
 * démarrer ni descripteur de build pour le compiler.
 */
@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
