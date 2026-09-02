(function () {
  const $ = (id) => document.getElementById(id);
  const units = window.TDAAH.unitSymbols();

  // All rendering below builds nodes and assigns textContent. Never innerHTML:
  // unit names and expressions come straight from the user, and UnitError
  // interpolates them into its message, so any string here can be hostile.
  const el = (tag, text) => {
    const node = document.createElement(tag);
    node.textContent = text;
    return node;
  };

  function fillSelect(target) {
    target.replaceChildren(
      ...units.map((u) => {
        const option = el("option", u);
        option.value = u;
        return option;
      })
    );
  }

  if ($("srcUnit")) {
    fillSelect($("srcUnit"));
    fillSelect($("dstUnit"));
    $("srcUnit").value = "cm";
    $("dstUnit").value = "m";
  }

  const history = [];
  function pushHistory(line) {
    history.unshift(line);
    const box = $("history");
    if (box) box.replaceChildren(...history.slice(0, 8).map((h) => el("li", h)));
  }

  function show(target, headline, detail, ok) {
    target.className = "result " + (ok ? "ok" : "error");
    target.replaceChildren(el("strong", headline), el("span", detail));
  }

  $("btnConvert")?.addEventListener("click", () => {
    const out = $("convertOut");
    try {
      const r = window.TDAAH.convert($("value").value, $("srcUnit").value, $("dstUnit").value);
      show(out, `${r.value} ${r.unit}`, "Conversion TDAAH", true);
      pushHistory(`${$("value").value} ${$("srcUnit").value} → ${r.value} ${r.unit}`);
    } catch (err) {
      show(out, "Erreur", err.message, false);
    }
  });

  $("btnCalc")?.addEventListener("click", () => {
    const out = $("calcOut");
    try {
      const r = window.TDAAH.calculate($("expr").value);
      show(out, `${r.value} ${r.unit}`, "Calcul dimensionnel", true);
      pushHistory(`${$("expr").value} = ${r.value} ${r.unit}`);
    } catch (err) {
      show(out, "Erreur", err.message, false);
    }
  });
})();
