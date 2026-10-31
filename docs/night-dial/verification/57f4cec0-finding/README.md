# Intermediate observation and visual findings — 57f4cec0

Run 41078614533, job 124141428306, artifact 12987974374. Both new alarm journeys passed, along with four other native methods and all 80 focused unit methods. The overall run failed in the existing dial test: it reused an accessibility object while calculating its drag path, and the object was replaced before any pointer injection. The follow-up reads one fresh rectangle before preparing coordinates, with bounded observation reacquisition; it never replays input or drops domain assertions.

Manual inspection also found inactive weekday letters rendered dark-on-dark and a repeat error still visible after the field had been corrected to 3. These historical frames intentionally preserve those defects. d3a59ff1 passed all seven methods and 64 frames after the observation/readability fixes; the final 37de8841 review additionally verifies that corrected repeat input clears the error without rebuilding the input field.
