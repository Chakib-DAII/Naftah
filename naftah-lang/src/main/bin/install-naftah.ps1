# SPDX-License-Identifier: Apache-2.0
# Copyright (c) The Naftah Project Authors

$ErrorActionPreference = "Stop"

$BinDir = $PSScriptRoot
$NaftahHome = Split-Path -Parent $BinDir
$MetadataFile = Join-Path $NaftahHome "installer\java-version.properties"

function Get-RequiredJavaVersion {
    if (-not (Test-Path -LiteralPath $MetadataFile)) {
        throw "Java version metadata not found: $MetadataFile"
    }

    $line = Get-Content -LiteralPath $MetadataFile |
            Where-Object { $_ -match '^requiredJavaVersion=\d+$' } |
            Select-Object -First 1

    if (-not $line) {
        throw "The requiredJavaVersion property is missing or invalid."
    }

    return [int](($line -split '=', 2)[1])
}

function Get-JavaMajorVersion {
    param(
        [string]$VersionText
    )

    if ($VersionText -match '^1\.(\d+)') {
        return [int]$Matches[1]
    }

    if ($VersionText -match '^(\d+)') {
        return [int]$Matches[1]
    }

    return $null
}

function Test-Jdk {
    param(
        [string]$JavaHome,
        [int]$RequiredVersion
    )

    if ([string]::IsNullOrWhiteSpace($JavaHome)) {
        return $false
    }

    $java = Join-Path $JavaHome "bin\java.exe"
    $javac = Join-Path $JavaHome "bin\javac.exe"

    if (
    -not (Test-Path -LiteralPath $java) -or
            -not (Test-Path -LiteralPath $javac)
    ) {
        return $false
    }

    try {
        # java -version and javac -version normally write to stderr.
        # Capture their output without treating it as a PowerShell error.
        $javaOutput = (& $java -version 2>&1 | Out-String)
        $javaExitCode = $LASTEXITCODE

        $javacOutput = (& $javac -version 2>&1 | Out-String)
        $javacExitCode = $LASTEXITCODE

        if ($javaExitCode -ne 0 -or $javacExitCode -ne 0) {
            return $false
        }

        # Support both modern Java versions and legacy 1.x version strings.
        $javaMatch = [regex]::Match(
                $javaOutput,
                'version "([^"]+)"'
        )

        $javacMatch = [regex]::Match(
                $javacOutput,
                'javac ([^\s]+)'
        )

        if (-not $javaMatch.Success -or -not $javacMatch.Success) {
            return $false
        }

        $javaMajor = Get-JavaMajorVersion $javaMatch.Groups[1].Value
        $javacMajor = Get-JavaMajorVersion $javacMatch.Groups[1].Value

        if ($null -eq $javaMajor -or $null -eq $javacMajor) {
            return $false
        }

        return (
        $javaMajor -ge $RequiredVersion -and
                $javacMajor -ge $RequiredVersion
        )
    }
    catch {
        return $false
    }
}

function Find-CompatibleJdk {
    param(
        [int]$RequiredVersion
    )

    $candidateHomes = @(
        $env:JAVA_HOME,
        [Environment]::GetEnvironmentVariable("JAVA_HOME", "User"),
        [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
    )

    foreach ($candidateHome in $candidateHomes) {
        if (
        -not [string]::IsNullOrWhiteSpace($candidateHome) -and
                (Test-Jdk -JavaHome $candidateHome -RequiredVersion $RequiredVersion)
        ) {
            return $candidateHome
        }
    }

    # Check common Eclipse Temurin installation directories.
    $roots = @(
        "$env:ProgramFiles\Eclipse Adoptium",
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium"
    )

    foreach ($root in $roots) {
        if (-not (Test-Path -LiteralPath $root)) {
            continue
        }

        foreach ($directory in Get-ChildItem -LiteralPath $root -Directory) {
            if (
            Test-Jdk `
                    -JavaHome $directory.FullName `
                    -RequiredVersion $RequiredVersion
            ) {
                return $directory.FullName
            }
        }
    }

    # Check the Java executable available through PATH.
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue

    if ($javaCommand) {
        $javaHome = Split-Path -Parent (
        Split-Path -Parent $javaCommand.Source
        )

        if (
        Test-Jdk `
                -JavaHome $javaHome `
                -RequiredVersion $RequiredVersion
        ) {
            return $javaHome
        }
    }

    return $null
}

function Install-JdkWithWinget {
    param(
        [int]$RequiredVersion
    )

    $winget = Get-Command winget.exe -ErrorAction SilentlyContinue

    if (-not $winget) {
        throw "winget is unavailable. Install JDK $RequiredVersion or newer manually."
    }

    $packageId = "EclipseAdoptium.Temurin.$RequiredVersion.JDK"

    Write-Host "Attempting to install $packageId..."

    & $winget.Source install `
        --id $packageId `
        --exact `
        --silent `
        --accept-package-agreements `
        --accept-source-agreements

    $installExitCode = $LASTEXITCODE

    if ($installExitCode -eq 0) {
        return
    }

    if ($installExitCode -eq 1618) {
        Write-Warning (
        "Another installation is already in progress. " +
                "Waiting briefly and checking whether the required JDK becomes available."
        )

        # Do not start another installer while Windows Installer is busy.
        # Check for an installation completed by another process.
        for ($attempt = 1; $attempt -le 6; $attempt++) {
            Start-Sleep -Seconds 10

            $installedHome = Find-CompatibleJdk `
                -RequiredVersion $RequiredVersion

            if ($installedHome) {
                Write-Host "A compatible JDK was found at: $installedHome"
                return
            }
        }

        throw (
        "Another installation is still in progress (exit code 1618). " +
                "Wait for the other installation to finish, then rerun this script. " +
                "No second installation was started."
        )
    }

    throw "Failed to install package $packageId. Exit code: $installExitCode"
}

function Set-UserPathEntry {
    param(
        [string]$Entry
    )

    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $pathEntries = @(
    $userPath -split ';' |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    )

    $alreadyPresent = $false

    foreach ($pathEntry in $pathEntries) {
        if ($pathEntry.TrimEnd('\') -ieq $Entry.TrimEnd('\')) {
            $alreadyPresent = $true
            break
        }
    }

    if (-not $alreadyPresent) {
        $newPath = if ([string]::IsNullOrWhiteSpace($userPath)) {
            $Entry
        }
        else {
            "$userPath;$Entry"
        }

        [Environment]::SetEnvironmentVariable(
                "Path",
                $newPath,
                "User"
        )
    }
}

function Install-NaftahJava {
    $requiredVersion = Get-RequiredJavaVersion

    Write-Host "Naftah requires JDK $requiredVersion or newer."

    # Check existing installations before invoking winget.
    $jdkHome = Find-CompatibleJdk -RequiredVersion $requiredVersion

    if (-not $jdkHome) {
        Write-Host "No compatible JDK found. Attempting installation..."

        Install-JdkWithWinget -RequiredVersion $requiredVersion

        # Discover the JDK after installation.
        $jdkHome = Find-CompatibleJdk -RequiredVersion $requiredVersion
    }

    if (-not $jdkHome) {
        throw "Could not locate a compatible JDK $requiredVersion or newer."
    }

    $jdkBin = Join-Path $jdkHome "bin"

    # Update the current PowerShell process.
    $env:JAVA_HOME = $jdkHome

    $currentPathEntries = @(
    $env:PATH -split ';' |
            Where-Object { $_ }
    )

    $jdkBinPresent = $false

    foreach ($pathEntry in $currentPathEntries) {
        if ($pathEntry.TrimEnd('\') -ieq $jdkBin.TrimEnd('\')) {
            $jdkBinPresent = $true
            break
        }
    }

    if (-not $jdkBinPresent) {
        $env:PATH = "$jdkBin;$env:PATH"
    }

    # Persist JAVA_HOME and PATH for the current Windows user.
    [Environment]::SetEnvironmentVariable(
            "JAVA_HOME",
            $jdkHome,
            "User"
    )

    Set-UserPathEntry -Entry $jdkBin

    Write-Host "Compatible JDK found at: $jdkHome"

    Write-Host "Java runtime version:"
    & "$jdkBin\java.exe" -version

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to execute java.exe."
    }

    Write-Host "Java compiler version:"
    & "$jdkBin\javac.exe" -version

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to execute javac.exe."
    }
}

try {
    Install-NaftahJava

    # Register the Naftah bin directory in the user's PATH.
    Set-UserPathEntry -Entry $BinDir

    Write-Host ""
    Write-Host "Naftah Java setup completed successfully."
    Write-Host "Open a new terminal to use the updated environment."
}
catch {
    [Console]::Error.WriteLine(
            "Installation failed: {0}",
            $_.Exception.Message
    )
    exit 1
}