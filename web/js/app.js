(function () {
  const $ = (id) => document.getElementById(id);
  const units = window.TDAAH.unitSymbols();
  function fillSelect(el) {
    el.innerHTML = units.map((u) => `<option value="${u}">${u}</option>`).join("");
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
    if (box) box.innerHTML = history.slice(0, 8).map((h) => `<li>${h}</li>`).join("");
  }
  function show(el, text, ok) {
    el.className = "result " + (ok ? "ok" : "error");
    el.innerHTML = text;
  }
  $("btnConvert")?.addEventListener("click", () => {
    const out = $("convertOut");
    try {
      const r = window.TDAAH.convert($("value").value, $("srcUnit").value, $("dstUnit").value);
      show(out, `<strong>${r.value} ${r.unit}</strong><span>Conversion TDAAH</span>`, true);
      pushHistory(`${$("value").value} ${$("srcUnit").value} → ${r.value} ${r.unit}`);
    } catch (err) {
      show(out, `<strong>Erreur</strong><span>${err.message}</span>`, false);
    }
  });
  $("btnCalc")?.addEventListener("click", () => {
    const out = $("calcOut");
    try {
      const r = window.TDAAH.calculate($("expr").value);
      show(out, `<strong>${r.value} ${r.unit}</strong><span>Calcul dimensionnel</span>`, true);
      pushHistory(`${$("expr").value} = ${r.value} ${r.unit}`);
    } catch (err) {
      show(out, `<strong>Erreur</strong><span>${err.message}</span>`, false);
    }
  });
})();
