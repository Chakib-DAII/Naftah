# SPDX-License-Identifier: Apache-2.0
# Copyright (c) The Naftah Project Authors

$ErrorActionPreference = "Stop"

$BinDir = $PSScriptRoot
$NaftahHome = Split-Path -Parent $BinDir
$MetadataFile = Join-Path $NaftahHome "installer\java-version.properties"

function Get-RequiredJavaVersion {
    if (-not (Test-Path $MetadataFile)) {
        throw "ملف معلومات إصدار Java غير موجود: $MetadataFile"
    }

    $line = Get-Content $MetadataFile |
            Where-Object { $_ -match '^requiredJavaVersion=\d+$' } |
            Select-Object -First 1

    if (-not $line) {
        throw "خاصية requiredJavaVersion مفقودة أو غير صالحة."
    }

    return [int](($line -split '=', 2)[1])
}

function Test-Jdk {
    param(
        [string]$JavaHome,
        [int]$RequiredVersion
    )

    if (-not $JavaHome) {
        return $false
    }

    $java = Join-Path $JavaHome "bin\java.exe"
    $javac = Join-Path $JavaHome "bin\javac.exe"

    if (-not (Test-Path $java) -or -not (Test-Path $javac)) {
        return $false
    }

    $javaOutput = (& $java -version 2>&1 | Out-String)
    $javacOutput = (& $javac -version 2>&1 | Out-String)

    return (
    $javaOutput -match "version `"$RequiredVersion(?:\.|`")" -and
            $javacOutput -match "javac $RequiredVersion(?:\.|$)"
    )
}

function Install-NaftahJava {
    $requiredVersion = Get-RequiredJavaVersion

    Write-Host "يتطلب نفطه تثبيت JDK $requiredVersion."

    $candidateHomes = @(
        $env:JAVA_HOME,
        [Environment]::GetEnvironmentVariable("JAVA_HOME", "User"),
        [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
    ) | Where-Object { $_ } | Select-Object -Unique

    $jdkHome = $null

    foreach ($candidateHome in $candidateHomes) {
        if (Test-Jdk $candidateHome $requiredVersion) {
            $jdkHome = $candidateHome
            break
        }
    }

    if (-not $jdkHome) {
        Write-Host "لم يتم العثور على إصدار JDK المطلوب. جارٍ محاولة التثبيت..."

        $winget = Get-Command winget.exe -ErrorAction SilentlyContinue

        if (-not $winget) {
            throw "تعذر العثور على winget. يرجى تثبيت JDK $requiredVersion يدويًا."
        }

        $packageId = "EclipseAdoptium.Temurin.$requiredVersion.JDK"

        & $winget.Source install `
            --id $packageId `
            --exact `
            --silent `
            --accept-package-agreements `
            --accept-source-agreements

        if ($LASTEXITCODE -ne 0) {
            throw "فشل تثبيت الحزمة $packageId."
        }

        $roots = @(
            "$env:ProgramFiles\Eclipse Adoptium",
            "$env:LOCALAPPDATA\Programs\Eclipse Adoptium"
        )

        foreach ($root in $roots) {
            if (Test-Path $root) {
                foreach ($directory in Get-ChildItem $root -Directory) {
                    if (Test-Jdk $directory.FullName $requiredVersion) {
                        $jdkHome = $directory.FullName
                        break
                    }
                }
            }

            if ($jdkHome) {
                break
            }
        }
    }

    if (-not $jdkHome) {
        throw "تعذر العثور على تثبيت صالح لـ JDK $requiredVersion."
    }

    $jdkBin = Join-Path $jdkHome "bin"

    # Update the current PowerShell session.
    $env:JAVA_HOME = $jdkHome
    $env:PATH = "$jdkBin;$env:PATH"

    # Persist JAVA_HOME for the current Windows user.
    [Environment]::SetEnvironmentVariable(
            "JAVA_HOME",
            $jdkHome,
            "User"
    )

    # Add the JDK bin directory to the user's PATH if necessary.
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $pathEntries = @(
    $userPath -split ";" |
            Where-Object { $_ }
    )

    if ($pathEntries -notcontains $jdkBin) {
        $newPath = if ($userPath) {
            "$userPath;$jdkBin"
        } else {
            $jdkBin
        }

        [Environment]::SetEnvironmentVariable(
                "Path",
                $newPath,
                "User"
        )
    }

    Write-Host "تم العثور على JDK في: $jdkHome"
    Write-Host "إصدار بيئة تشغيل Java:"
    & "$jdkBin\java.exe" -version

    Write-Host "إصدار مترجم Java:"
    & "$jdkBin\javac.exe" -version
}

try {
    Install-NaftahJava

    # Register the Naftah bin directory in the user's PATH.
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $pathEntries = @(
    $userPath -split ";" |
            Where-Object { $_ }
    )

    if ($pathEntries -notcontains $BinDir) {
        $newPath = if ($userPath) {
            "$userPath;$BinDir"
        } else {
            $BinDir
        }

        [Environment]::SetEnvironmentVariable(
                "Path",
                $newPath,
                "User"
        )
    }

    Write-Host ""
    Write-Host "اكتمل إعداد Java وتثبيت نفطه بنجاح."
    Write-Host "افتح نافذة طرفية جديدة لاستخدام متغيرات البيئة المحدّثة."
}
catch {
    Write-Error "فشل التثبيت: $($_.Exception.Message)"
    exit 1
}
