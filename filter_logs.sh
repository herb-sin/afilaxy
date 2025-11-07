#!/bin/bash

# Filtrar apenas logs do app Afilaxy
echo "=== Logs do Afilaxy ==="
adb logcat | grep "com.afilaxy"

# Ou filtrar por tag específica
# adb logcat -s "HelperRepository,RequestEme,com.afilaxy"

# Ou filtrar por PID (substitua 5673 pelo PID atual)
# adb logcat --pid=5673