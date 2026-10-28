# Best Practices and Security Alignment Update: callback ownership

Security alignment area: private alarm occurrence and synchronous Math input ownership. Priority: medium correctness hardening. A retained callback from an old Activity could publish stale progress or selection in the same occurrence; this is not a finding of external component exposure.

The production change is limited to AlarmActivity, MathAnswerOwner and WakeMathCard. No manifest, dependency, exported component, Intent routing, scheduling or Service output path changes. See [the exact unified source diff](production-fix.diff).

Math submission now checks the main thread, resumed live focused Activity, attached visible card rooted in its own window, current question identity, local run/unit and durable ringing/run/unit/cache before any selection, build, correct/incorrect/empty feedback or IME callback mutation. Guard and mutation share the existing Scheduler lock. The central advance lock additionally checks durable expected-unit equality. Its authenticated camera/QR/NFC and other result paths gain no global focus/resume requirement.

Production and instrumentation compile passed. CI 40633181797 passed the exact existing method in 51.773s. The shared real guard/card executes in an actual Practice Activity with private preferences and real lifecycle transitions. AlarmActivity admission, its central advance/dismiss method, authenticated child-result integrations, AlarmService and hardware outputs do not execute in this journey. See README for the precise boundary.
