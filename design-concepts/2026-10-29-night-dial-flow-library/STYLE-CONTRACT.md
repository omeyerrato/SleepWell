# Night dial design and interaction contract

Selected visual reference: accepted-reference.png. Preserve its character rather than replacing it with the later monochrome studies.

- Ground near-black; tone-separated charcoal surfaces. Warm amber expresses active controls, meaningful data and selected navigation. White pill primary, charcoal pill secondary. These are intended roles, not measured raster color samples.
- Modern sans-serif typography, tabular time figures, readable labels. Large dial on Tonight and tracking; waveforms on audio; plots on reports. Do not place a decorative dial on every settings page.
- Four root tabs, with label and icon. Nested pages use Back and an actual parent title. A back gesture does not silently save a dirty editor.
- Consistent48dpminimum hit area. Switch on has thumb right; off left; selected state also differs by position/mark rather than only color. Disabled is visibly muted with a reason when relevant. No layout jump on press.
- Slider thumb has generous hit area; value is readable separately. Timer/Fade sheets preserve draft until Use. Cancel restores the previously saved value.
- Sheet: handle, title, close affordance, readable content, primary and secondary actions. No popover over another modal. On large text, sheet can expand and scroll while actions remain reachable.
- Radio selects a sound. Play previews it. Choosing a sound does not start tracking. Save stores config. Arm creates a wait. Start begins admitted recording. These verbs remain distinct.
- Sensor data graph ends at current time, with pauses/gaps explicit. A future planned interval can use a separate neutral track. Elapsed values and all duplicated facts must use one source of truth.
- Night tracking removes distracting navigation and reduces luminance. Pausing, ending and fallback remain reachable. Motion is short state feedback with a reduced-motion alternative; no perpetual decorative motion.
- Microphone, camera, activity, exact-alarm, notification, full-screen, accessibility, admin, provider and launcher flows hand off to native Android where appropriate. Custom app explanations precede those handoffs.
- Preserve free/local-first behavior. No paywalls, invented partners, accounts or hosted research/cloud service. Optional user-controlled providers have explicit connection, test, error and revoke routes.


