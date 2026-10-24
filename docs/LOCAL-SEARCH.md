# Local search

Settings → Search SleepWell searches actual saved alarms, effective sleep notes,
tags/quantities/dates and real settings destinations. All words must match;
case/accent/punctuation normalization supports Unicode. Exact/prefix titles rank
first. Queries are limited to160 characters and results to100. No network index,
write, invented sample results or automatic alarm/tracking action is involved.

Data reads and matching run off the UI thread with request-generation fences.
Draft queries survive recreation and data reloads on return. Alarm selection
re-reads its current saved row before opening the existing editor. Record selection
opens its actual report and settings selection opens the corresponding screen.
Empty/no-result/loading/error/retry states are explicit.

Four pure search tests pass. Private storage and native query/recreate/navigation
tests are authored; combined Android build and native execution remain pending.
