# Mechanical logistic decoder correction

This is an arithmetic correction, not a new model, threshold, regularizer,
calibration procedure or successful research result. No real study data or
outcomes were read for the synthetic check.

Installed sklearn 1.9.1 source shows its binary decision function computes
`X @ coef_.T + intercept_`, retains fitted dtypes, flattens the one-column result,
then applies sigmoid in place and stacks the two class probabilities. The frozen
float32 inputs produce float32 fitted coefficients/intercept and probabilities.
The previous portable code changed this to a float64 one-dimensional matrix
product. At tolerance `1e-12`, that is a different arithmetic contract.

`scripts/phone_neural_head_synthetic.py` uses only a seeded synthetic 199×32
normal array and invented binary labels. It reproduces the old discrepancy and
checks the corrected workflow after JSON round-trip, several batch sizes and
reconstructed primary parameters. The tracked synthetic receipt records exact
source/runtime/input hashes. This establishes decoder consistency only.

The portable head now records and reuses both fitted dtypes and the exact primary
matrix shape and in-place sigmoid operation. The strict `1e-12` threshold stays
unchanged. Held-out primary reconstruction uses those same recorded dtypes.

`model-fitted.json` is created immediately after the completed fit and before
convergence/parity assertions. `head-parity.json` records a pass or failure, with
the fitted artifact hash. Exception receipts link any existing fitted/parity
artifacts. `model.json` remains available only after the original convergence and
parity requirements pass. A deliberately incorrect synthetic comparison confirms
failed parity preserves the fitted artifact and failure receipts.

No automatic retry/refit was added. A real cached-feature replay needs the
separate explicit parent authorization, committed mechanical-exception freeze and
unaltered scientific protocol. Earlier failed attempts remain archived.
