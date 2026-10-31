# Appearance lamp header: actual native route and saved appearance

Run **41193956518**, frozen source **cc4871eefa5833266f79d6f1b319d5561c1bf285** (0.1.25/version 26). Native result: **OK (1 test), 67.47 seconds**, no summary failures. This archive contains the unchanged 11 original full-window PNGs, their actual view-tree geometry, original environment/summary/native result and SHA-256 manifest. No emulator run or phone action was performed while archiving.

All 11 originals were individually opened and visually compared with board 12 Appearance; Tonight companions were checked against the established board 01 hierarchy. The automatic page and Midnight/Pre-dawn/Dawn manual pages retain the three illustrated selectors, selected ring, settings rows, sunrise lamp image, duration/intensity, full Connect light and fixed Save appearance. The new bedroom-lamp name/status header now has a genuine chevron and matching configuration destination. At 1.3 app text scale the duration and intensity labels stack above their values; all four lamp actions and Save remain visible without overlap. Art remains cropped to fill its original column.

## Measured current native geometry

1080×2316 override window, density 420; all values below are physical pixels, from the archived layout JSON. Each of the four lamp controls is 501×126 (48 dp high), and each reported visible rectangle equals its complete bounds.

| Action | Normal y range | 1.3 app text / high contrast y range |
| --- | --- | --- |
| Configure bedroom lamp | 1558–1684 | 1533–1659 |
| Sunrise duration | 1684–1810 | 1659–1785 |
| Maximum intensity | 1810–1936 | 1785–1911 |
| Connect light | 1936–2062 | 1911–2037 |
| Save appearance | 2085–2232 | 2085–2232 |

The normal Connect-to-Save gap is 23 px; the large-text gap is 48 px. Normal card copy has 4 px of noninteractive bottom padding clipped by the scroll viewport; every control remains fully visible. At large text, the whole card copy is visible. Top options may scroll away at large text. Tonight remains a scrollable body above a fixed full Start button and four-tab dock; the large frame legitimately shows body content continuing below its viewport rather than squeezing it into the footer. Four tabs reflect the user's later request for a separate Alarms tab.

## Actual action and persistence

The existing AppearanceFlowTest method opens an unsaved Dawn draft, presses the new header, verifies the resumed owner is the actual SmartLightActivity and captures its local configuration page. Back retains the Dawn/manual draft; the complete saved preferences map is unchanged by this visit. Back then cancels that Appearance draft. The same method saves each manual theme, reopens settings, recreates MainActivity, and checks actual Tonight root colors. It also saves/reopens 1.3 text, reduced motion and high contrast. The final Tonight action capture checks the Start button's entire visible touch region; tracking is not started.

The full header, duration, intensity and Connect target fit is asserted in the normal first frame; current large-frame layout diagnostics independently show all four full regions. This method exercises the new header route; it does not claim to exercise every lamp-setting route or send any lamp command.

## Remaining scope and visible limitation

The original `appearance-lamp-config.png` preserves the pre-existing squeezed Hue-title defect. **Corrected in the separately archived follow-up [41197756647](readable-bridge-41197756647/README.md)** at source b1983691: the actual title is one readable line with its subtitle below. That same existing method passed in54.815 seconds, and all11 follow-up originals were individually inspected. Original files above remain unchanged for traceability. No successful full-36-screen match is claimed.

No physical device, alarm, audio, haptic, flashlight, network light connection, credential validity or lamp restoration was exercised. The runner reports host audio disabled and no native playback outputs allowlisted. Opening this local configuration page loads saved configuration; it does not contact a lamp. Phone/hardware integration, external prerequisites, other feature gaps and broader design remain open.
