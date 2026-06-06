#!/bin/bash
# ── Task Tracker — start both servers ──────────────────────────────────────
# Run from the ITWS_final folder:  bash run.sh
# Stop both servers:               bash run.sh stop
# ---------------------------------------------------------------------------

ROOT="$(cd "$(dirname "$0")" && pwd)"

stop_all() {
  echo ""
  echo "Stopping servers…"
  kill "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null
  wait "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null
  echo "Done."
  exit 0
}

if [[ "$1" == "stop" ]]; then
  pkill -f "spring-boot:run" 2>/dev/null
  pkill -f "vite"            2>/dev/null
  echo "Servers stopped."
  exit 0
fi

echo "=================================================="
echo "  Task Tracker"
echo "  Backend  → http://localhost:8081"
echo "  Frontend → http://localhost:5173"
echo "  Press Ctrl+C to stop both"
echo "=================================================="
echo ""

# Start backend in background, log to backend.log
cd "$ROOT/backend"
mvn spring-boot:run > "$ROOT/backend.log" 2>&1 &
BACKEND_PID=$!
echo "Backend  started (PID $BACKEND_PID) — tailing backend.log"

# Wait until Spring Boot is up
echo -n "Waiting for backend"
until curl -s http://localhost:8081/api/categories > /dev/null 2>&1; do
  echo -n "."
  sleep 1
done
echo " ready!"

# Start frontend in background, log to frontend.log
cd "$ROOT/frontend"
npm run dev > "$ROOT/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo "Frontend started (PID $FRONTEND_PID) — open http://localhost:5173"
echo ""

# Open the browser automatically (macOS)
sleep 2 && open "http://localhost:5173" &

# Trap Ctrl+C to stop both
trap stop_all INT TERM

# Keep script alive, stream both logs
tail -f "$ROOT/backend.log" "$ROOT/frontend.log" &
wait "$BACKEND_PID" "$FRONTEND_PID"
