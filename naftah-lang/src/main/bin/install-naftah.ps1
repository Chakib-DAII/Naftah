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

        $javaVersionText = $javaMatch.Groups[1].Value
        $javacVersionText = $javacMatch.Groups[1].Value

        if ($javaVersionText -match '^1\.(\d+)') {
            $javaMajor = [int]$Matches[1]
        }
        elseif ($javaVersionText -match '^(\d+)') {
            $javaMajor = [int]$Matches[1]
        }
        else {
            return $false
        }

        if ($javacVersionText -match '^1\.(\d+)') {
            $javacMajor = [int]$Matches[1]
        }
        elseif ($javacVersionText -match '^(\d+)') {
            $javacMajor = [int]$Matches[1]
        }
        else {
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

function Install-NaftahJava {
    $requiredVersion = Get-RequiredJavaVersion

    Write-Host "Naftah requires JDK $requiredVersion or newer."

    $candidateHomes = @(
        $env:JAVA_HOME,
        [Environment]::GetEnvironmentVariable("JAVA_HOME", "User"),
        [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
    ) |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
            Select-Object -Unique

    $jdkHome = $null

    foreach ($candidateHome in $candidateHomes) {
        if (
        Test-Jdk `
                -JavaHome $candidateHome `
                -RequiredVersion $requiredVersion
        ) {
            $jdkHome = $candidateHome
            break
        }
    }

    if (-not $jdkHome) {
        Write-Host "No compatible JDK found. Attempting installation..."

        $winget = Get-Command winget.exe -ErrorAction SilentlyContinue

        if (-not $winget) {
            throw "winget is unavailable. Install JDK $requiredVersion or newer manually."
        }

        $packageId = "EclipseAdoptium.Temurin.$requiredVersion.JDK"

        & $winget.Source install `
            --id $packageId `
            --exact `
            --silent `
            --accept-package-agreements `
            --accept-source-agreements

        if ($LASTEXITCODE -ne 0) {
            throw "Failed to install package $packageId."
        }

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
                        -RequiredVersion $requiredVersion
                ) {
                    $jdkHome = $directory.FullName
                    break
                }
            }

            if ($jdkHome) {
                break
            }
        }

        # Check JAVA_HOME again in case the installer updated it.
        if (-not $jdkHome) {
            $installedHome = [Environment]::GetEnvironmentVariable(
                    "JAVA_HOME",
                    "Machine"
            )

            if (
            Test-Jdk `
                    -JavaHome $installedHome `
                    -RequiredVersion $requiredVersion
            ) {
                $jdkHome = $installedHome
            }
        }
    }

    if (-not $jdkHome) {
        throw "Could not locate a compatible JDK $requiredVersion or newer."
    }

    $jdkBin = Join-Path $jdkHome "bin"

    # Update the current PowerShell process.
    $env:JAVA_HOME = $jdkHome

    if (($env:PATH -split ';') -notcontains $jdkBin) {
        $env:PATH = "$jdkBin;$env:PATH"
    }

    # Persist JAVA_HOME for the current Windows user.
    [Environment]::SetEnvironmentVariable(
            "JAVA_HOME",
            $jdkHome,
            "User"
    )

    # Add the JDK bin directory to the user's PATH.
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $pathEntries = @(
    $userPath -split ';' |
            Where-Object { $_ }
    )

    if ($pathEntries -notcontains $jdkBin) {
        $newPath = if ($userPath) {
            "$userPath;$jdkBin"
        }
        else {
            $jdkBin
        }

        [Environment]::SetEnvironmentVariable(
                "Path",
                $newPath,
                "User"
        )
    }

    Write-Host "Compatible JDK found at: $jdkHome"

    & "$jdkBin\java.exe" -version
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to execute java.exe."
    }

    & "$jdkBin\javac.exe" -version
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to execute javac.exe."
    }
}

try {
    Install-NaftahJava

    # Register the Naftah bin directory in the user's PATH.
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $pathEntries = @(
    $userPath -split ';' |
            Where-Object { $_ }
    )

    if ($pathEntries -notcontains $BinDir) {
        $newPath = if ($userPath) {
            "$userPath;$BinDir"
        }
        else {
            $BinDir
        }

        [Environment]::SetEnvironmentVariable(
                "Path",
                $newPath,
                "User"
        )
    }

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