# Candidate 2 rejected after validation

The final authorized mechanical replay completed at frozen source `a65be6e5`.
It used the exact previously extracted TRAIN NPZ, verified by its frozen SHA-256,
without repeating feature extraction. One real logistic fit converged in 17
iterations. Primary/portable probability differences were exactly zero on both
TRAIN and validation. The fitted model was saved before diagnostic assertions.

The unchanged validation gates **failed**. No respiratory TEST command was
invoked, its outcome labels remain unopened, and no threshold change, refit or
additional candidate is authorized by this exception.

| Validation measure | Result |
| --- | --- |
| Eligible windows | 88 of 100; 64 negative, 24 positive |
| Participants represented per class | 10 negative, 9 positive |
| Confusion, rows actual negative/positive | `[[63, 1], [24, 0]]` |
| Accuracy | 71.59% |
| Balanced accuracy | 49.22% |
| Sensitivity | 0.00% |
| Specificity | 98.44% |
| AUROC | 0.61263 |
| Macro F1 | 0.41722 |
| Brier | 0.19787 |
| Negative log likelihood | 0.58438 |

Coverage, support, specificity, Brier and log-loss gates passed. AUROC, balanced
accuracy, sensitivity and macro-F1 gates failed. In particular, the model detected
none of the 24 positive windows at the unchanged threshold. The majority baseline
had 72.73% accuracy; the apparent accuracy does not establish useful detection.

This candidate is unsuitable for deployment. Decoder agreement proves arithmetic
fidelity only. The endpoint is conditional source-annotated event overlap in
sampled windows, not sleep-stage validation, breathing diagnosis, full-night
coverage or AHI. Validation participants were previously inspected in candidate 1;
this is not an untouched validation claim. Source event-duration units remain a
documented conditional interpretation.

Exact receipts, full validation probabilities, calibration bins, per-participant
results and artifact hashes are in
`execution-status/neural-transfer-v1-final-cached/`. All original private model and
NPZ artifacts remain in this worktree's
`.android-local/research/neural-transfer-v1-final-cached/`. Fitted model SHA-256:
`c18caa20633ff1ad11d883ae4f5e229fbef91f7022f841f2e2feb245af0560ef`.
No model was added to Android, no phone operation ran and no data was downloaded.
