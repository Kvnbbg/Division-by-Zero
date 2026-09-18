# Division-by-Zero — numeric safety matrix

| Input/result | Expected semantic state |
|---|---|
| 10 / 4 | finite |
| 1 / 0 | zero-division rejection |
| 1 / -0 | zero-division rejection |
| 0 / 0 | zero-division rejection |
| NaN / 2 | non-finite rejection |
| 2 / NaN | non-finite rejection |
| +Infinity / 2 | non-finite rejection |
| 2 / +Infinity | non-finite rejection |
| overflowing finite ratio | non-finite-result rejection |
| very small finite ratio | finite, unless a future representability rule explicitly changes |
| dimension mismatch | conversion/domain rejection |

## Regression rule

Every new numerical transform should have at least one ordinary finite case and one boundary case. If JavaScript and Java classify a case differently, update the canonical corpus first rather than patching one runtime in isolation.

## Anti-regression checklist

- [ ] zero and negative zero
- [ ] NaN
- [ ] both infinities
- [ ] overflow
- [ ] underflow
- [ ] null/missing operand semantics
- [ ] dimension mismatch
- [ ] conversion offset
- [ ] serialization of result
