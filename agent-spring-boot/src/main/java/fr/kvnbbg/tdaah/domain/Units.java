package fr.kvnbbg.tdaah.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Conversion de mesures dimensionnées.
 *
 * <p>Portage fidèle de web/js/convertisseur.js — mêmes unités, mêmes facteurs,
 * mêmes décalages, même nettoyage numérique. Le comportement de référence
 * existait déjà en JavaScript ; le réécrire « à peu près » aurait créé deux
 * vérités divergentes pour un même calcul.
 *
 * <p>Une dimension est un vecteur d'exposants [L, M, T, I, Th]. Deux unités ne
 * sont convertibles que si leurs vecteurs sont identiques : c'est ce qui
 * distingue une conversion d'un simple produit par un facteur.
 */
public final class Units {

    /** Exposants [longueur, masse, temps, courant, température]. */
    public record Dimension(int l, int m, int t, int i, int th) {
        public static final Dimension L = new Dimension(1, 0, 0, 0, 0);
        public static final Dimension M = new Dimension(0, 1, 0, 0, 0);
        public static final Dimension T = new Dimension(0, 0, 1, 0, 0);
        public static final Dimension I = new Dimension(0, 0, 0, 1, 0);
        public static final Dimension TH = new Dimension(0, 0, 0, 0, 1);
        public static final Dimension AREA = new Dimension(2, 0, 0, 0, 0);
        public static final Dimension VOLUME = new Dimension(3, 0, 0, 0, 0);
        public static final Dimension SPEED = new Dimension(1, 0, -1, 0, 0);
        public static final Dimension ENERGY = new Dimension(2, 1, -2, 0, 0);
        public static final Dimension POWER = new Dimension(2, 1, -3, 0, 0);
        public static final Dimension PRESSURE = new Dimension(-1, 1, -2, 0, 0);
        public static final Dimension FORCE = new Dimension(1, 1, -2, 0, 0);
    }

    /**
     * Une unité : son symbole, sa dimension, son facteur vers le SI et son
     * décalage. Le décalage n'est non nul que pour les températures — c'est
     * précisément ce qui interdit de traiter °C comme un simple facteur.
     */
    public record Unit(String symbol, Dimension dimension, double factor, double offset) {
        Unit(String symbol, Dimension dimension, double factor) {
            this(symbol, dimension, factor, 0d);
        }
    }

    /** Résultat d'une conversion : valeur nettoyée et symbole cible. */
    public record Converted(double value, String unit) {}

    private static final List<Unit> UNITS = List.of(
            new Unit("m", Dimension.L, 1),
            new Unit("km", Dimension.L, 1000),
            new Unit("cm", Dimension.L, 0.01),
            new Unit("mm", Dimension.L, 0.001),
            new Unit("in", Dimension.L, 0.0254),
            new Unit("ft", Dimension.L, 0.3048),
            new Unit("mi", Dimension.L, 1609.344),
            new Unit("kg", Dimension.M, 1),
            new Unit("g", Dimension.M, 0.001),
            new Unit("t", Dimension.M, 1000),
            new Unit("lb", Dimension.M, 0.45359237),
            new Unit("s", Dimension.T, 1),
            new Unit("min", Dimension.T, 60),
            new Unit("h", Dimension.T, 3600),
            new Unit("K", Dimension.TH, 1, 0),
            new Unit("C", Dimension.TH, 1, 273.15),
            new Unit("F", Dimension.TH, 5d / 9d, 255.37222222222223),
            new Unit("m2", Dimension.AREA, 1),
            new Unit("ha", Dimension.AREA, 10000),
            new Unit("m3", Dimension.VOLUME, 1),
            new Unit("L", Dimension.VOLUME, 0.001),
            new Unit("mL", Dimension.VOLUME, 1e-6),
            new Unit("m/s", Dimension.SPEED, 1),
            new Unit("km/h", Dimension.SPEED, 1000d / 3600d),
            new Unit("mph", Dimension.SPEED, 1609.344 / 3600d),
            new Unit("J", Dimension.ENERGY, 1),
            new Unit("kWh", Dimension.ENERGY, 3.6e6),
            new Unit("cal", Dimension.ENERGY, 4.184),
            new Unit("W", Dimension.POWER, 1),
            new Unit("kW", Dimension.POWER, 1000),
            new Unit("Pa", Dimension.PRESSURE, 1),
            new Unit("bar", Dimension.PRESSURE, 1e5),
            new Unit("atm", Dimension.PRESSURE, 101325),
            new Unit("psi", Dimension.PRESSURE, 6894.757293168361),
            new Unit("N", Dimension.FORCE, 1),
            new Unit("A", Dimension.I, 1));

    private static final Map<String, Unit> BY_SYMBOL = new LinkedHashMap<>();

    static {
        for (Unit unit : UNITS) {
            BY_SYMBOL.put(unit.symbol().toLowerCase(Locale.ROOT), unit);
        }
    }

    private static final Map<Dimension, String> LABELS = Map.ofEntries(
            Map.entry(Dimension.L, "longueur"),
            Map.entry(Dimension.M, "masse"),
            Map.entry(Dimension.T, "temps"),
            Map.entry(Dimension.I, "courant électrique"),
            Map.entry(Dimension.TH, "température"),
            Map.entry(Dimension.AREA, "surface"),
            Map.entry(Dimension.VOLUME, "volume"),
            Map.entry(Dimension.SPEED, "vitesse"),
            Map.entry(Dimension.ENERGY, "énergie"),
            Map.entry(Dimension.POWER, "puissance"),
            Map.entry(Dimension.PRESSURE, "pression"),
            Map.entry(Dimension.FORCE, "force"));

    private Units() {
    }

    /** Unité connue, ou {@link UnitException} — jamais un défaut silencieux. */
    public static Unit lookup(String name) {
        if (name == null || name.isBlank()) {
            throw new UnitException("unité manquante.");
        }
        Unit unit = BY_SYMBOL.get(name.trim().toLowerCase(Locale.ROOT));
        if (unit == null) {
            throw new UnitException("unité inconnue : " + name);
        }
        return unit;
    }

    private static double toSi(double value, Unit unit) {
        return value * unit.factor() + unit.offset();
    }

    private static double fromSi(double si, Unit unit) {
        return (si - unit.offset()) / unit.factor();
    }

    /**
     * Rabote le bruit de la virgule flottante, comme le fait le JS : sans cela
     * 1 km -> m rendrait 999.9999999999999 et la conversion paraîtrait fausse.
     */
    static double clean(double n) {
        if (!Double.isFinite(n)) {
            return n;
        }
        if (Math.abs(n) < 1e-12) {
            return 0d;
        }
        double nearest = Math.round(n);
        if (nearest != 0 && Math.abs(n - nearest) < 1e-10) {
            return nearest;
        }
        return n;
    }

    /** Convertit entre deux unités de MÊME dimension. */
    public static Converted convert(double value, String from, String to) {
        Unit source = lookup(from);
        Unit target = lookup(to);
        if (!source.dimension().equals(target.dimension())) {
            throw new UnitException(
                    "conversion impossible : " + source.symbol() + " vers " + target.symbol() + ".");
        }
        return new Converted(clean(fromSi(toSi(value, source), target)), target.symbol());
    }

    /**
     * Divise deux mesures de même dimension et refuse un dénominateur nul.
     *
     * <p>Le rapport est sans dimension, donc exprimable en nombre : c'est le seul
     * cas de division que l'agent expose, et c'est là que le refus s'applique.
     *
     * <p>LA FAILLE CORRIGÉE ICI — le refus ne portait que sur {@code bottom == 0d},
     * une égalité flottante exacte. Un dénominateur réellement non nul, mais
     * minuscule face au numérateur (ou rendu minuscule par le facteur SI de son
     * unité, voir psi ≈ 6894,76), fait déborder la division en
     * {@link Double#POSITIVE_INFINITY} ou {@link Double#NEGATIVE_INFINITY} sans
     * jamais franchir ce test. Le portage JavaScript souffre du même défaut
     * ({@code div()} dans web/js/convertisseur.js, et {@code clean()} qui laisse
     * passer explicitement toute valeur non finie) : ce n'est pas une régression
     * du portage, c'est le comportement d'origine, fidèlement reproduit.
     *
     * <p>Or c'est précisément ce que la classe {@link ZeroDivisionMeasurementException}
     * dit refuser dans son propre Javadoc : « on ne renvoie ni Infinity, ni NaN,
     * ni zéro par commodité ». Exposé à travers l'API HTTP de l'agent
     * ({@code GET /v1/agent/ratio}), Jackson sérialise un double infini en la
     * CHAÎNE JSON {@code "Infinity"} plutôt que de lever une erreur — un agent
     * appelant qui attend un nombre reçoit un texte qui y ressemble, et un calcul
     * en aval sur cette valeur produit un résultat silencieusement faux plutôt
     * qu'un échec visible.
     *
     * <p>Le refus porte donc sur le RÉSULTAT, pas seulement sur l'entrée : un
     * résultat non fini est refusé au même titre qu'un dénominateur nul, avec le
     * même code d'erreur — l'appelant n'a pas à distinguer les deux cas, dans les
     * deux cas la division n'a pas de résultat exploitable.
     */
    public static double ratio(double numerator, String numeratorUnit,
                               double denominator, String denominatorUnit) {
        Unit a = lookup(numeratorUnit);
        Unit b = lookup(denominatorUnit);
        if (!a.dimension().equals(b.dimension())) {
            throw new UnitException(
                    "rapport impossible : " + a.symbol() + " et " + b.symbol() + " n'ont pas la même dimension.");
        }
        double bottom = toSi(denominator, b);
        if (bottom == 0d) {
            throw new ZeroDivisionMeasurementException();
        }
        double result = toSi(numerator, a) / bottom;
        if (!Double.isFinite(result)) {
            throw new ZeroDivisionMeasurementException(
                    "résultat non représentable (dépassement de capacité) : le dénominateur n'est pas nul, "
                            + "mais trop petit face au numérateur pour produire un rapport fini.");
        }
        return clean(result);
    }

    /** Symboles connus, dans l'ordre de déclaration. */
    public static List<String> symbols() {
        List<String> out = new ArrayList<>(UNITS.size());
        for (Unit unit : UNITS) {
            out.add(unit.symbol());
        }
        return List.copyOf(out);
    }

    /** Décrit la dimension d'une unité, en clair. */
    public static Map<String, Object> explain(String name) {
        Unit unit = lookup(name);
        Dimension d = unit.dimension();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("unit", unit.symbol());
        out.put("dimension", LABELS.getOrDefault(d, "sans dimension"));
        out.put("exponents", List.of(d.l(), d.m(), d.t(), d.i(), d.th()));
        out.put("siFactor", unit.factor());
        out.put("siOffset", unit.offset());
        return out;
    }

    /** Unité inconnue, ou incompatible avec la conversion demandée. */
    public static class UnitException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public UnitException(String message) {
            super(message);
        }
    }
}
