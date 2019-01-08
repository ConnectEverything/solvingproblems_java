
# Instructor Notes for Solving Problems with NATS

These are the notes for instructor setup for the _Solving Problems with NATS_ course. These are included
in the solutions folder in case students want to repeat the setup.

## Running the main cluster

1. Open 2 terminals.
2. In the first terminal, run `gnatsd -p 4222 -cluster nats://localhost:5222 -routes nats://localhost:5223`.
3. In the second terminal, run `gnatsd -p 4223 -cluster nats://localhost:5223 -routes nats://localhost:5222`.