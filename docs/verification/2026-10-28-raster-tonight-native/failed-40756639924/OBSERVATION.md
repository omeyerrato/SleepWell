# Failed native fixture attempt

Run 40756639924 at ef69408258e900a50274d6f2488e859647138714 is **FAILED**, method 58.396s. All 18 required originals plus arc-failure.png/XML were produced. Build passed. The failure is the final raw drawable-bounds assertion in launcherDiagnostic: ImageView width 315px versus drawable width237px. ImageView FIT_CENTER applies its actual image matrix after those intrinsic drawable bounds; comparing the two untransformed widths was invalid.

Every original and the failure PNG was individually opened. The actual color/themed native launcher previews visibly fit at 120/48dp; all 73 glyphs are legible on the three galleries, which were captured only after their decoded bitmap/intrinsic/alpha checks and before their actual-window PixelCopy tint checks. The method reached the final launcher assertion after those tint checks passed. This does not turn the failed method/job into a pass. No metrics TSV was written in this first version because that write occurred after the final launcher assertions.

Native-only fix ecdea35da8956adc120cbf96a50fc08449a374d0 maps drawable and mask bounds through ImageView.getImageMatrix, verifies the fully rendered dimensions and containment, retains raw mask containment and all full 48dp gates, and persists measured 73 glyph rows before launcher checks as well as afterward. It changes no production file or artwork. The focused rerun remains the same existing method/18 required originals with both optional output flags false.

All files copied from the artifact are byte-identical. The failed raw summary, environment, full method stack/log, guest logs,19 original PNGs and XML remain here. No failure files are relabelled as success.
