package fr.kvnbbg.tdaah.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Le portage doit rendre EXACTEMENT ce que rend web/js/convertisseur.js.
 * Deux implémentations d'un même calcul qui divergent, c'est un bug qui ne se
 * voit qu'en production, du côté que personne ne regarde.
 */
class UnitsTest {

    @Test
    @DisplayName("convertit une longueur comme le fait le JS")
    void convertitLongueur() {
        assertEquals(1000d, Units.convert(1, "km", "m").value());
        assertEquals(1d, Units.convert(1000, "m", "km").value());
        assertEquals("m", Units.convert(1, "km", "m").unit());
    }

    @Test
    @DisplayName("applique le décalage des températures, pas seulement un facteur")
    void convertitTemperature() {
        // 0 °C = 273,15 K : traiter °C comme un simple facteur donnerait 0.
        assertEquals(273.15d, Units.convert(0, "C", "K").value(), 1e-9);
        assertEquals(0d, Units.convert(273.15, "K", "C").value(), 1e-9);
        assertEquals(32d, Units.convert(0, "C", "F").value(), 1e-9);
        assertEquals(100d, Units.convert(212, "F", "C").value(), 1e-9);
    }

    @Test
    @DisplayName("rabote le bruit flottant, comme clean() en JS")
    void nettoieLeBruit() {
        // Sans nettoyage, ce trajet rend 999.9999999999999.
        assertEquals(1000d, Units.convert(1, "km", "m").value());
        assertEquals(0d, Units.clean(1e-13));
        assertEquals(42d, Units.clean(42.000000000001d));
    }

    @Test
    @DisplayName("refuse une conversion entre dimensions différentes")
    void refuseDimensionsIncompatibles() {
        Units.UnitException e = assertThrows(Units.UnitException.class, () -> Units.convert(1, "kg", "m"));
        assertTrue(e.getMessage().contains("impossible"));
    }

    @Test
    @DisplayName("refuse une unité inconnue plutôt que de deviner")
    void refuseUniteInconnue() {
        assertThrows(Units.UnitException.class, () -> Units.convert(1, "smoot", "m"));
        assertThrows(Units.UnitException.class, () -> Units.convert(1, "m", ""));
    }

    @Test
    @DisplayName("refuse la division par une mesure nulle — le geste du projet")
    void refuseDivisionParZero() {
        assertThrows(ZeroDivisionMeasurementException.class, () -> Units.ratio(10, "m", 0, "m"));
        // 0 °C n'est PAS une mesure nulle : c'est 273,15 K, le rapport existe.
        assertEquals(1d, Units.ratio(273.15, "K", 0, "C"), 1e-9);
    }

    @Test
    @DisplayName("calcule un rapport sans dimension")
    void calculeRapport() {
        assertEquals(2d, Units.ratio(2, "km", 1000, "m"), 1e-9);
    }

    @Test
    @DisplayName("refuse un rapport entre dimensions différentes")
    void refuseRapportIncompatible() {
        assertThrows(Units.UnitException.class, () -> Units.ratio(1, "kg", 1, "s"));
    }

    @Test
    @DisplayName("expose les mêmes symboles que le JS, dans le même ordre")
    void listeLesUnites() {
        assertEquals(36, Units.symbols().size());
        assertEquals("m", Units.symbols().get(0));
        assertTrue(Units.symbols().contains("kWh"));
        assertTrue(Units.symbols().contains("psi"));
    }

    @Test
    @DisplayName("explique la dimension d'une unité")
    void expliqueDimension() {
        assertEquals("vitesse", Units.explain("km/h").get("dimension"));
        assertEquals("température", Units.explain("C").get("dimension"));
        assertEquals(273.15d, Units.explain("C").get("siOffset"));
    }
}
