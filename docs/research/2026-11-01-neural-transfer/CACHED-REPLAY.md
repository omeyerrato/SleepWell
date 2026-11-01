# One final mechanical cached replay

Parent explicitly authorized one final deterministic replay after the synthetic
decoder correction passed. `mechanical-head-replay.json` is the complete bounded
exception to the earlier retry prohibition. It pins the unchanged scientific
protocol, synthetic proof, prior failures and exact cached TRAIN NPZ/metadata.
These files and the complete workflow must be committed before execution.

The fit command requires `--cached-train path/to/train-features.npz`. The reader
checks both files' exact byte sizes and SHA-256 hashes before decoding with
`allow_pickle=False`, then checks array types/shapes and feature/label hashes.
The original fitted scalers and head procedure, target checks, convergence
requirement and strict parity threshold remain. The cache is not modified; exact
feature arrays and a source receipt are preserved in the new output directory.

Run the fixed primary NN synthetic parity against the new freeze first, then
one cached fit, then one validation phase. The existing test phase refuses to
open its data unless every unchanged validation gate passed and the exact model
hash matches. Use a new output directory; exclusive creation of phase/artifact
receipts prevents replaying a phase there. Any new error or failed validation
gate terminates this exception. No data downloads, weight updates, calibration,
threshold changes, search or additional retry are authorized.

The correction and these execution instructions establish no performance claim.
Actual results will be recorded separately after execution.
