# Verifies that a Room schema migration preserves existing user data, end to end,
# on a connected emulator or device. No manual tapping required.
#
#   1. installs the current debug APK and clears its data
#   2. lets it create and seed a fresh database
#   3. rewinds that file to the previous schema version, with progress planted
#   4. relaunches the app so Room runs the real migration
#   5. pulls the result and asserts nothing was lost
#
# Usage:  powershell -File run_migration_check.ps1 [-SkipBuild]
#
# Three Windows/Android gotchas are worked around below; see the comments:
#   - `adb exec-out ... > file` corrupts binaries under PowerShell redirection
#   - `2>&1` on a native exe trips $ErrorActionPreference = Stop even on success
#   - Room writes in WAL mode and seeds asynchronously, so file existence proves
#     nothing; the runner polls the database contents instead

param([switch]$SkipBuild)

$ErrorActionPreference = "Stop"
$pkg  = "com.yoshisgarden.readit"
$adb  = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$root = (Resolve-Path "$PSScriptRoot\..\..").Path
$work = Join-Path $env:TEMP "readit-migration-check"

if (-not (Test-Path $adb)) { throw "adb not found at $adb" }

# The daemon can restart mid-run and report "device offline", which silently skips
# install/launch steps and makes the whole check bogus. Block until it is usable.
& $adb start-server | Out-Null
& $adb wait-for-device
for ($i = 0; $i -lt 60; $i++) {
    if ("$(& $adb shell 'getprop sys.boot_completed')".Trim() -eq "1") { break }
    Start-Sleep -Seconds 2
}
if ("$(& $adb shell 'getprop sys.boot_completed')".Trim() -ne "1") { throw "device never came online" }

# Pulls readit.db plus its -wal, so the local copy holds the committed data.
# adb reports transfer progress on stderr, so never redirect it here (see header).
function Get-AppDb($localBase) {
    Remove-Item "$localBase*" -Force -ErrorAction SilentlyContinue
    foreach ($suffix in @("", "-wal")) {
        & $adb shell "run-as $pkg cat databases/readit.db$suffix > /data/local/tmp/pull.bin"
        & $adb pull /data/local/tmp/pull.bin "$localBase$suffix" | Out-Null
    }
}

# Launches the app and waits until the database actually reaches [$wantVersion]
# with [$wantPhrases] rows. Polling the file's contents is the only reliable
# signal: the UI appears long before Room has finished opening and seeding.
function Start-App($what, $wantVersion, $wantPhrases) {
    & $adb logcat -c
    # `monkey` reuses an existing task and can return START_DELIVERED_TO_TOP, which
    # never recreates the Activity — the app then looks launched but never opens the
    # database. `-S` forces a stop first so every run is a genuine cold start.
    & $adb shell "am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS" | Out-Null
    & $adb shell "am start -S -W -n $pkg/.MainActivity" | Out-Null
    for ($i = 0; $i -lt 45; $i++) {
        Start-Sleep -Seconds 2
        $crash = & $adb logcat -d | Select-String "FATAL EXCEPTION|IllegalStateException"
        if ($crash) {
            $crash | Select-Object -First 8 | ForEach-Object { Write-Host $_ -ForegroundColor Red }
            throw "app crashed during $what"
        }
        Get-AppDb "$work\probe.db"
        $state = (py "$PSScriptRoot\probe_db.py" "$work\probe.db").Trim()
        if ($state -eq "version=$wantVersion phrases=$wantPhrases") {
            Write-Host "  $what ready ($state)"
            return
        }
    }
    throw "$what never reached version=$wantVersion phrases=$wantPhrases (last: $state)"
}

New-Item -ItemType Directory -Force -Path $work | Out-Null
Get-ChildItem $work -Filter "*.db*" -ErrorAction SilentlyContinue | Remove-Item -Force

if (-not $SkipBuild) {
    Write-Host "`n== building debug APK ==" -ForegroundColor Cyan
    $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
    & "$root\gradlew.bat" :app:assembleDebug --console=plain | Select-String "BUILD"
}

Write-Host "`n== installing + seeding ==" -ForegroundColor Cyan
& $adb install -r "$root\app\build\outputs\apk\debug\ReadIT-debug.apk" | Select-Object -Last 1
& $adb shell "pm clear $pkg" | Out-Null
# Grant POST_NOTIFICATIONS up front. Otherwise the runtime-permission dialog sits
# on top after `pm clear`, and `am start` reports "intent has been delivered to
# currently running top-most instance" — the launch goes to GrantPermissionsActivity,
# MainActivity never resumes, and the database is never opened.
& $adb shell "pm grant $pkg android.permission.POST_NOTIFICATIONS" | Out-Null
$phrases = 990   # bump alongside the seed JSON
Start-App "seeding" 2 $phrases
& $adb shell "am force-stop $pkg"
Start-Sleep -Seconds 3

Write-Host "`n== rewinding to the previous schema ==" -ForegroundColor Cyan
Get-AppDb "$work\readit.db"
py "$PSScriptRoot\make_v1_db.py" "$work\readit.db"
if ($LASTEXITCODE -ne 0) { throw "could not rewind the database" }
Remove-Item "$work\readit.db-wal" -ErrorAction SilentlyContinue  # folded in by the checkpoint

& $adb push "$work\readit.db" /data/local/tmp/push.db | Out-Null
& $adb shell "run-as $pkg cp /data/local/tmp/push.db databases/readit.db"
& $adb shell "run-as $pkg rm -f databases/readit.db-wal databases/readit.db-shm"

Write-Host "`n== running the migration ==" -ForegroundColor Cyan
Start-App "migration" 2 $phrases
& $adb shell "am force-stop $pkg"
Start-Sleep -Seconds 3

Write-Host "`n== verifying ==" -ForegroundColor Cyan
Get-AppDb "$work\after.db"
py "$PSScriptRoot\verify_migration.py" "$work\after.db"
$result = $LASTEXITCODE

& $adb shell "rm -f /data/local/tmp/pull.bin /data/local/tmp/push.db"
exit $result
