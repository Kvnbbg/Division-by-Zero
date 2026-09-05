/**
 * Tests de régression du convertisseur — miroir de UnitsTest.java.
 *
 * Le portage Java et l'original JS doivent refuser dans EXACTEMENT les mêmes
 * cas. Deux implémentations d'un même calcul qui divergent, c'est un bug qui ne
 * se voit qu'en production, du côté que personne ne regarde.
 *
 * Lancer : node --test web/js/
 */
const test = require("node:test");
const assert = require("node:assert");
const { convert, calculate, unitSymbols, UnitError, ZeroDivisionMeasurementError } = require("./convertisseur.js");

test("convertit une longueur", () => {
  assert.strictEqual(convert(1, "km", "m").value, 1000);
  assert.strictEqual(convert(1000, "m", "km").value, 1);
});

test("applique le décalage des températures, pas seulement un facteur", () => {
  assert.ok(Math.abs(convert(0, "C", "K").value - 273.15) < 1e-9);
  assert.ok(Math.abs(convert(0, "C", "F").value - 32) < 1e-9);
});

test("refuse une conversion entre dimensions différentes", () => {
  assert.throws(() => convert(1, "kg", "m"), UnitError);
});

test("refuse une unité inconnue plutôt que de deviner", () => {
  assert.throws(() => convert(1, "smoot", "m"), UnitError);
});

test("refuse la division par une mesure nulle — le geste du projet", () => {
  assert.throws(() => calculate("10 m / 0 m"), ZeroDivisionMeasurementError);
});

test("refuse un rapport qui déborde en Infinity, sans dénominateur nul", () => {
  // Le dénominateur n'est PAS nul : il est seulement trop petit face au
  // numérateur pour que la division tienne dans un double.
  assert.throws(() => calculate("1e300 m / 1e-300 m"), ZeroDivisionMeasurementError);
});

test("refuse une conversion qui déborde en Infinity", () => {
  // LE TROU FERMÉ ICI. Avant correctif, ceci rendait { value: Infinity }.
  assert.throws(() => convert(1e308, "km", "m"), ZeroDivisionMeasurementError);
});

test("refuse une conversion qui déborde par le facteur SI de la cible", () => {
  // 1e305 m3 n'a rien d'extrême ; c'est mL (facteur 1e-6) qui fait déborder.
  assert.throws(() => convert(1e305, "m3", "mL"), ZeroDivisionMeasurementError);
});

test("refuse une valeur d'entrée non finie plutôt que de la propager", () => {
  // NaN se propage sans bruit, et NaN !== NaN : même un contrôle d'égalité
  // en aval échoue à le détecter.
  assert.throws(() => convert(NaN, "m", "km"), UnitError);
  assert.throws(() => convert(Infinity, "m", "km"), UnitError);
});

test("refuse une multiplication qui déborde", () => {
  assert.throws(() => calculate("1e300 m * 1e300 m"), ZeroDivisionMeasurementError);
});

test("laisse passer les conversions extrêmes mais représentables", () => {
  // La garde ne doit pas devenir un plafond arbitraire.
  assert.ok(Math.abs(convert(1e300, "m3", "mL").value - 1e306) < 1e291);
  assert.ok(Math.abs(convert(1e-6, "mm", "km").value - 1e-12) < 1e-24);
});

test("expose les mêmes symboles que le Java, dans le même ordre", () => {
  assert.strictEqual(unitSymbols().length, 36);
  assert.strictEqual(unitSymbols()[0], "m");
});
