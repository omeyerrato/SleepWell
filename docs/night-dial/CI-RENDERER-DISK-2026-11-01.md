# Disposable renderer disk failure

Run41266940619 attempt1 failed before Android boot, without running any native method. Its actual emulator.log reports3553.90MB free and7372.80MB needed for the userdata partition. The failed artifact remains in the original workspace cache; this is not an application result.

The workflow now checks free space after building and before creating/booting the emulator. On GitHub-hosted Linux only, it may remove the fixed unused .NET and CodeQL toolchain directories to reserve space. It never removes the checkout, Android SDK, Gradle caches, phone/user data or anything on a local/self-hosted computer. It refuses to proceed below9GiB free rather than claiming a skipped UI success. The runner kind guard uses GitHub’s documented RUNNER_ENVIRONMENT variable: https://docs.github.com/en/actions/reference/workflows-and-actions/variables. No app source, APK, data schema, native method or assertion changes.
