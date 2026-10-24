# Visual search challenge

An original sleeping-moon search task is available in the alarm routine editor.
One sleeping target appears among awake decoys. Rounds use3×3,4×4 and5×5 boards;
wrong answers never advance. The exact board persists for the alarm occurrence and
unit, so recreation cannot replace it. Completion uses the same occurrence/unit
fences as the existing routine. Accessible labels identify each moon and cell.
An explicit persisted Math alternative is available. Invalid saved boards require
Math rather than silently replacing the task.

The editor includes a silent preview that shares the actual grid component and
never starts/schedules an alarm or any sound, sensor, vibration or light output.
Four pure model tests pass, including1800 generated boards and strict round trips.
Three private storage/codec tests and one native preview test are authored; native
execution and the live alarm dispatch remain unverified until recorded separately.
