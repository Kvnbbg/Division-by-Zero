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
    @DisplayName("refuse un rapport dont le résultat déborde en Infinity — pas de dénominateur nul requis")
    void refuseRapportInfini() {
        // LA FAILLE : le refus ne vérifiait que `bottom == 0d`. Un dénominateur
        // réellement non nul, mais assez petit face au numérateur, fait
        // déborder la division en Double.POSITIVE_INFINITY sans jamais passer
        // par ce test — exactement ce que la classe dit refuser dans son
        // propre Javadoc : « on ne renvoie ni Infinity, ni NaN ». Avant le
        // correctif, cet appel rendait Infinity au lieu de lever.
        ZeroDivisionMeasurementException e = assertThrows(
                ZeroDivisionMeasurementException.class,
                () -> Units.ratio(1e300, "m", 1e-300, "m"));
        assertTrue(e.getMessage().contains("dépassement"));
    }

    @Test
    @DisplayName("refuse un rapport qui déborde à cause du facteur SI de l'unité, pas seulement de la valeur brute")
    void refuseRapportInfiniParFacteur() {
        // Même défaut, par un autre chemin : le facteur SI de "mL" (1e-6)
        // combiné à une valeur saisie qui, seule, n'a rien d'extrême, suffit à
        // pousser le dénominateur assez bas pour faire déborder la division.
        // Les deux unités partagent la dimension VOLUME : ce n'est pas un
        // rapport entre dimensions incompatibles qui est testé ici.
        assertThrows(
                ZeroDivisionMeasurementException.class,
                () -> Units.ratio(1e300, "m3", 1e-294, "mL"));
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

    @Test
    @DisplayName("refuse une conversion qui déborde en Infinity — le refus ne portait que sur la division")
    void refuseConversionQuiDeborde() {
        // LE TROU RESTANT. Le correctif précédent a fermé ratio() dans les deux
        // implémentations, mais convert() emprunte le même clean(), qui rend
        // explicitement toute valeur non finie telle quelle. Sondé avant
        // correctif : convert(1e308, "km", "m") rendait Infinity.
        //
        // Ce n'est pas un cas d'école : la valeur traverse l'API HTTP
        // (POST /v1/agent/convert), où Jackson sérialise un double infini en la
        // CHAÎNE JSON "Infinity". L'agent appelant reçoit un texte là où son
        // schéma annonce un nombre — et tout calcul en aval devient faux en
        // silence, au lieu d'échouer franchement.
        ZeroDivisionMeasurementException e = assertThrows(
                ZeroDivisionMeasurementException.class,
                () -> Units.convert(1e308, "km", "m"));
        assertTrue(e.getMessage().contains("dépassement"));
    }

    @Test
    @DisplayName("refuse une conversion qui déborde par le facteur SI de la cible, pas par la valeur saisie")
    void refuseConversionQuiDebordeParFacteur() {
        // Second chemin vers le même défaut : 1e305 m3 n'a rien d'extrême pour
        // un double, mais la cible mL (facteur 1e-6) divise par un millionième
        // et pousse le résultat au-delà de Double.MAX_VALUE. C'est le débordement
        // que la seule lecture de l'entrée ne permet pas d'anticiper — d'où une
        // garde sur le RÉSULTAT et non sur la valeur saisie.
        assertThrows(
                ZeroDivisionMeasurementException.class,
                () -> Units.convert(1e305, "m3", "mL"));
    }

    @Test
    @DisplayName("refuse une valeur d'entrée non finie plutôt que de la propager")
    void refuseEntreeNonFinie() {
        // Sondé avant correctif : convert(NaN, "m", "km") rendait NaN. La porte
        // d'entrée compte autant que la sortie — AgentController.asDouble()
        // s'appuie sur Double.parseDouble(), qui accepte volontiers les chaînes
        // "NaN" et "Infinity" : un client peut donc injecter du non-fini par le
        // corps de la requête.
        assertThrows(Units.UnitException.class, () -> Units.convert(Double.NaN, "m", "km"));
        assertThrows(Units.UnitException.class, () -> Units.convert(Double.POSITIVE_INFINITY, "m", "km"));
        assertThrows(Units.UnitException.class, () -> Units.ratio(Double.NaN, "m", 2, "m"));
        assertThrows(Units.UnitException.class, () -> Units.ratio(2, "m", Double.NaN, "m"));
    }

    @Test
    @DisplayName("laisse passer les conversions extrêmes mais représentables")
    void accepteLesExtremesRepresentables() {
        // La garde ne doit pas devenir un plafond arbitraire : tant que le
        // résultat tient dans un double, la conversion reste due.
        assertEquals(1e306d, Units.convert(1e300, "m3", "mL").value(), 1e291);
        assertEquals(1e-12d, Units.convert(1e-6, "mm", "km").value(), 1e-24);
    }
}
