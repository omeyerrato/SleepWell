# Sleep sound mixer controls and retained routes

Board07/left, screen19. This changes only the listening screen's access to its existing settings and the silent Timer/Fade choice presentation. It preserves the existing artwork, sounds, mixer levels, explicit Play/Pause/Stop, sources and playback behavior. The exact current native configuration flow passed with14 individually inspected originals. This archive does not claim all36 reference screens or physical audio acceptance.

## Source and scope

Base `741b3718032ab4dc1d52eb67cf2194d4c59884f9`. Source commits `94c1cb45293d3579a4446fda870ee1e253e360c3`, then `7df469d24a6de23dc273865c5c0610620313c007`:

- `SleepSoundsActivity`: a48dp header **Sound settings** action opens the existing More route. The duplicate footer row is removed, preserving Stop, the main core and all four bundled library tiles. More retains Sources & playlists, Playback & tracking and Guided wind-down.
- `SleepMixerChoiceEditor`: selecting a row updates the existing widgets in place. **Use** and **Cancel** stay outside the scroll area. Scroll and actual option/action focus, plus the unsaved selection and expected revision, survive recreation. Restoration waits for the first measured draw. Header/system Back and Cancel still discard; only Use calls the unchanged saved-mix/revision/update path. Arbitrary valid saved minutes and the existing fade clamp remain.

`source-preflight.json` pins the production/native hashes and confirms nine runtime/source-editor files plus eight critical callback fragments unchanged from the base. The production tree is `c756112b139cce51dcb735babd369fef3aca7585`, identical between both native attempts. No engine, model, artwork, global Design, manifest, version or release metadata was changed here.

The existing `SoundSettingsFlowTest#silentChoicesRecreateAndReopenWithoutPlayback` remains one method. `scope-inventory.json` lists the exact14 unique PNG literals, including the original four main states. `sleep-mixer-reference` adds only five workflow/script lines; prior mappings/defaults remain text-identical to the base after removing those additions. Both optional output flags are false, and `full_app_review=false`.

Local production/native compilation passed; the final corrected native compile passed in1min,25tasks(1executed/24up-to-date). CI uses the existing app/test APK build. No unit batch or new test method was added; no release build, publication or physical install was performed.

## Actual attempts

[First run40798291747](https://github.com/omeyerrato/SleepWell/actions/runs/40798291747), head `76b03ef7964c834943317e26c7be607ac7452bbb`, **FAILED** after6.441s. The new native description lookup called `String.contentEquals(null)` before the first required image. `attempt-1/` preserves all12 original artifact files, plus the terminal API result. Its single diagnostic PNG was individually opened: current main artwork/controls, Timer/Fade, header Stop/More, all four tiles and status fit. It is diagnostic evidence, not14-frame acceptance.

The fixture correction makes all description lookups null-safe, binds readiness to the owned laid-out Activity, waits for actual selected/focused postconditions, and uses real accessibility scroll/focus actions. The custom draft/private concurrent revision exercises nine real choices, nonzero scroll/recreation and stale-Use rejection; the fixed Cancel is used after scrolling. It does not change production or weaken target/ownership gates.

[Corrected run40800438208](https://github.com/omeyerrato/SleepWell/actions/runs/40800438208), head `7a4c6dfb656c180f5762b6771a50d935c52d5251`: **PASSED in34.228s**, status0 / `OK (1 test)`. Exact same one-method/14-original scope, flags false. Both APK builds passed. The original combined summary reports one class, one method,14 required originals and no failures. `originals/` retains all24 downloaded files byte-for-byte, including every PNG, exact method log, environment and summary. `run.json` pins the terminal API identity. The first attempt remains failed; no production change or extra native scope followed it.

All14 successful originals were individually opened against Board07/left; `inspection.json` records their hashes/dimensions and observations. The complete main core, Stop/More and allfourtiles fit. Ocean/45-minute timer/10-minute fade survive recreation and reopening. The240-minute selected/focused row stays readable; fixed Use/Cancel and custom37/7 selections fit. Sources, Playback and the recreated More menu retain readable existing destinations. Native48dp/full-visible bounds checks completed for the required main/menu/actions. No new core clipping was observed.

The native method also completed actual nonzero scroll/focus retention with the nine-choice private concurrent-revision state, stale-Use rejection, Cancel/Back discard and untouched-until-Use assertions. Those state assertions are not extra captured images, and no screenshot of the stale error is claimed. The exact service/personal-preference invariants passed through cleanup.

The static reference shows playing Rain; these configuration captures honestly show Ready/zero elapsed time and an inert disabled Stop. Existing artwork, functional child menus, headerStop/More and saved custom values intentionally extend that reference. Keyboard focus has a platform edge behind the selected panel without obscuring its text. Font/theme/device variants and current-playback behavior remain outside this result.

`SHA256SUMS` covers all archived files except itself. All36 original artifact files across the failed and passed attempts were byte-compared; the15 PNGs (14required plus one failed diagnostic) were individually opened.

## Boundaries

This is actual Android UI with UUID-private sound preferences. The method never taps Play, imports audio, starts tracking or creates an audio sink. It checks unchanged personal preferences, no service snapshot, explicit Use persistence, Cancel/Back discard, recreation, arbitrary values, stale revision rejection and the retained source/playback routes. A concurrent newer mix is injected only into private preferences; no personal data is replaced.

The real MediaPlayer/notification flow is excluded. Currently-playing update behavior, actual sound/provider/network operation, physical controls and alternate device/font/theme cases are not replayed. Existing artwork and retained extra source/menu functionality intentionally extend the static reference. This does not close broader SystemUI, model/provider, protection or36-screen acceptance gaps.
