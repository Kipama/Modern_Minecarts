[CmdletBinding()]
param(
    [ValidateSet('Compile', 'GameTest', 'Client')]
    [string]$Mode = 'Compile',

    [string[]]$Branches = @(
        'fabric/1.20.1',
        'fabric/1.21.1',
        'fabric/1.21.11',
        'fabric/26.1',
        'fabric/26.2',
        'forge/1.19.2',
        'forge/1.19.3',
        'forge/1.19.4',
        'forge/1.20.1',
        'forge/1.20.2',
        'neoforge/1.20.1',
        'neoforge/1.21',
        'neoforge/1.21.1',
        'neoforge/1.21.3-1.21.4',
        'neoforge/1.21.11',
        'neoforge/26.1',
        'neoforge/26.2'
    ),

    [switch]$KeepWorktrees,
    [switch]$ContinueOnFailure,

    [string]$Java17Home = $(if ($env:MODERNMINECARTS_JAVA17_HOME) { $env:MODERNMINECARTS_JAVA17_HOME } else { 'C:\Program Files\Eclipse Adoptium\jdk-17.0.7.7-hotspot' }),
    [string]$Java21Home = $(if ($env:MODERNMINECARTS_JAVA21_HOME) { $env:MODERNMINECARTS_JAVA21_HOME } else { 'C:\Users\Kilian Mayrhofer\.jdks\ms-21.0.11' }),
    [string]$Java25Home = $(if ($env:MODERNMINECARTS_JAVA25_HOME) { $env:MODERNMINECARTS_JAVA25_HOME } else { 'C:\Users\Kilian Mayrhofer\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2' }),
    [string]$GradleUserHome = $(if ($env:MODERNMINECARTS_GRADLE_USER_HOME) { $env:MODERNMINECARTS_GRADLE_USER_HOME } else { 'C:\mmqa-gradle' }),
    [string]$WorktreeRoot = $(if ($env:MODERNMINECARTS_WORKTREE_ROOT) { $env:MODERNMINECARTS_WORKTREE_ROOT } else { 'C:\mmqa-worktrees' })
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repositoryRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$worktreeRoot = [System.IO.Path]::GetFullPath($WorktreeRoot)
$gradleCacheRoot = [System.IO.Path]::GetFullPath($GradleUserHome)
$resultRoot = Join-Path $PSScriptRoot 'results'
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$resultDirectory = Join-Path $resultRoot "$Mode-$timestamp"

function Assert-WorktreePath([string]$Path) {
    $root = [System.IO.Path]::GetFullPath($worktreeRoot).TrimEnd([System.IO.Path]::DirectorySeparatorChar) + [System.IO.Path]::DirectorySeparatorChar
    $candidate = [System.IO.Path]::GetFullPath($Path)
    if (-not $candidate.StartsWith($root, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to modify a worktree outside '$worktreeRoot': $candidate"
    }
}

function Get-JavaHome([string]$Branch) {
    if ($Branch -match '(^|/)26\.') {
        return $Java25Home
    }
    if ($Branch -match '(^|/)(1\.21|1\.21\.1|1\.21\.3|1\.21\.11)') {
        return $Java21Home
    }
    return $Java17Home
}

function Get-WorktreeName([string]$Branch) {
    return $Branch -replace '[^A-Za-z0-9._-]', '-'
}

function Get-JavaMajor([string]$JavaHome) {
    if ($JavaHome -eq $Java17Home) { return '17' }
    if ($JavaHome -eq $Java21Home) { return '21' }
    if ($JavaHome -eq $Java25Home) { return '25' }
    throw "No isolated Gradle cache mapping exists for JDK '$JavaHome'."
}

function Remove-QAWorktree([string]$Worktree) {
    Assert-WorktreePath $Worktree
    $marker = Join-Path $Worktree '.modernminecarts-qa-worktree'
    if (-not (Test-Path -LiteralPath $marker)) {
        throw "Refusing to remove '$Worktree' because it was not created by this QA runner."
    }

    $trackedChanges = & git -C $Worktree status --porcelain --untracked-files=no
    if ($LASTEXITCODE -ne 0) {
        throw "Could not inspect QA worktree '$Worktree'."
    }
    if ($trackedChanges) {
        throw "Refusing to remove QA worktree '$Worktree' because it has tracked changes."
    }

    & git -C $repositoryRoot worktree remove --force $Worktree
    if ($LASTEXITCODE -ne 0) {
        throw "Could not remove QA worktree '$Worktree'."
    }
}

if ($Mode -eq 'Client' -and $Branches.Count -ne 1) {
    throw 'Client mode is interactive. Select exactly one branch with -Branches.'
}

New-Item -ItemType Directory -Force -Path $worktreeRoot, $gradleCacheRoot, $resultDirectory | Out-Null
$results = [System.Collections.Generic.List[object]]::new()

foreach ($branch in $Branches) {
    $branchRef = "refs/heads/$branch"
    & git -C $repositoryRoot show-ref --verify --quiet $branchRef
    if ($LASTEXITCODE -ne 0) {
        throw "Unknown local branch '$branch'."
    }

    $worktree = Join-Path $worktreeRoot (Get-WorktreeName $branch)
    Assert-WorktreePath $worktree
    $logPath = Join-Path $resultDirectory ((Get-WorktreeName $branch) + '.log')
    $started = Get-Date
    $success = $false
    $message = ''

    try {
        if (Test-Path -LiteralPath $worktree) {
            Remove-QAWorktree $worktree
        }

        & git -C $repositoryRoot worktree add --detach $worktree $branchRef
        if ($LASTEXITCODE -ne 0) {
            throw "Could not create QA worktree for '$branch'."
        }
        New-Item -ItemType File -Path (Join-Path $worktree '.modernminecarts-qa-worktree') | Out-Null

        $javaHome = Get-JavaHome $branch
        if (-not (Test-Path -LiteralPath (Join-Path $javaHome 'bin\java.exe'))) {
            throw "Required JDK was not found for '$branch': $javaHome"
        }

        $previousJavaHome = $env:JAVA_HOME
        $previousPath = $env:Path
        $previousGradleUserHome = $env:GRADLE_USER_HOME
        $env:JAVA_HOME = $javaHome
        $env:Path = "$(Join-Path $javaHome 'bin');$previousPath"
        $env:GRADLE_USER_HOME = Join-Path $gradleCacheRoot ("java" + (Get-JavaMajor $javaHome))
        try {
            $gradleArguments = switch ($Mode) {
                # Native file watching repeatedly scans a fresh detached
                # worktree on Windows before the Gradle task graph is ready.
                # Disable it for all isolated QA runs; it provides no value
                # for these one-shot invocations and prevents the scan loop.
                'Compile'  { @('compileJava', '--no-daemon', '--no-watch-fs') }
                'GameTest' { @('runGameTestServer', '--no-daemon', '--no-watch-fs') }
                'Client'   { @('runClient', '--no-daemon', '--no-watch-fs') }
            }

            "[$(Get-Date -Format 's')] $branch ($Mode, JAVA_HOME=$javaHome, GRADLE_USER_HOME=$env:GRADLE_USER_HOME)" | Tee-Object -FilePath $logPath
            Push-Location $worktree
            try {
                & .\gradlew.bat @gradleArguments 2>&1 | Tee-Object -FilePath $logPath -Append
                if ($LASTEXITCODE -ne 0) {
                    throw "Gradle exited with code $LASTEXITCODE. See $logPath"
                }
            }
            finally {
                Pop-Location
            }
        }
        finally {
            $env:JAVA_HOME = $previousJavaHome
            $env:Path = $previousPath
            $env:GRADLE_USER_HOME = $previousGradleUserHome
        }

        $success = $true
        $message = 'Passed'
    }
    catch {
        $message = $_.Exception.Message
        Write-Warning "${branch}: $message"
    }
    finally {
        $results.Add([pscustomobject]@{
            Branch = $branch
            Mode = $Mode
            Passed = $success
            DurationSeconds = [math]::Round(((Get-Date) - $started).TotalSeconds, 1)
            Log = $logPath
            Message = $message
        })

        if (-not $KeepWorktrees -and (Test-Path -LiteralPath $worktree)) {
            try {
                Remove-QAWorktree $worktree
            }
            catch {
                Write-Warning "Could not remove QA worktree '$worktree'. It was kept for inspection."
            }
        }
    }

    if (-not $success -and -not $ContinueOnFailure) {
        break
    }
}

$summaryPath = Join-Path $resultDirectory 'summary.json'
$results | ConvertTo-Json -Depth 3 | Set-Content -LiteralPath $summaryPath -Encoding utf8
$results | Format-Table -AutoSize
Write-Output "Results: $summaryPath"

if ($results.Passed -contains $false) {
    exit 1
}
