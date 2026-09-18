const assert = require("node:assert/strict");
const test = require("node:test");
const fs = require("node:fs");
const path = require("node:path");
const TDAAH = require("./js/convertisseur.js");

const corpus = JSON.parse(fs.readFileSync(
  path.join(__dirname, "..", "agent-spring-boot", "src", "test", "resources", "numeric-parity.json"), "utf8"
));

const toNumber = value => typeof value === "string" ? Number(value) : value;
const expression = (n, d) => `${n} / ${d}`;

for (const c of corpus.cases) {
  test(c.id, () => {
    const n = toNumber(c.numerator);
    const d = toNumber(c.denominator);

    if (c.expected === "zero_division") {
      assert.throws(() => TDAAH.calculate(expression(n, d)), TDAAH.ZeroDivisionMeasurementError);
      return;
    }

    if (c.expected === "non_finite_operand") {
      assert.throws(() => TDAAH.calculate(expression(n, d)), TDAAH.UnitError);
      return;
    }

    if (c.expected === "non_finite_result") {
      assert.throws(() => TDAAH.calculate(expression(n, d)), TDAAH.ZeroDivisionMeasurementError);
      return;
    }

    const result = TDAAH.calculate(expression(n, d));
    assert.equal(result.unit, "");
    assert.ok(Number.isFinite(result.value));
    assert.equal(result.value, c.value);
  });
}
