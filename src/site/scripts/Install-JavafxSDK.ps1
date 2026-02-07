# download-openjfx.ps1
# Downloads OpenJFX SDK and jmods for Windows, verifies SHA256, unpacks, and creates symlink

param(
    [Alias("v")]
    [string]$Version = "25.0.2"
)

$baseUrl = "https://download2.gluonhq.com/openjfx/${Version}"
$destDir = "$HOME\Applications"
$symlinkPath = Join-Path $destDir "javafx-sdk"

$downloads = @(
    @{
        Name      = "SDK"
        Url       = "${baseUrl}/openjfx-${Version}_windows-x64_bin-sdk.zip"
        ZipFile   = Join-Path $env:TEMP "openjfx-${Version}_windows-x64_bin-sdk.zip"
        Sha256Url = "${baseUrl}/openjfx-${Version}_windows-x64_bin-sdk.zip.sha256"
    },
    @{
        Name      = "jmods"
        Url       = "${baseUrl}/openjfx-${Version}_windows-x64_bin-jmods.zip"
        ZipFile   = Join-Path $env:TEMP "openjfx-${Version}_windows-x64_bin-jmods.zip"
        Sha256Url = "${baseUrl}/openjfx-${Version}_windows-x64_bin-jmods.zip.sha256"
    }
)

# Download all files in parallel using BITS
Write-Host "Downloading OpenJFX $Version SDK and jmods in parallel..."

$bitsJobs = @()
foreach ($dl in $downloads) {
    $bitsJobs += Start-BitsTransfer -Source $dl.Url -Destination $dl.ZipFile -Asynchronous -DisplayName $dl.Name
}

# Monitor progress until all complete - show each download with its own progress bar
do {
    $allComplete = $true
    $id = 1
    foreach ($job in $bitsJobs) {
        $job = Get-BitsTransfer -JobId $job.JobId
        if ($job.JobState -eq "Transferring" -or $job.JobState -eq "Connecting" -or $job.JobState -eq "Queued") {
            $allComplete = $false
            if ($job.BytesTotal -gt 0) {
                $pct = [math]::Round(($job.BytesTransferred / $job.BytesTotal) * 100, 1)
                $mb = [math]::Round($job.BytesTransferred / 1MB, 1)
                $totalMb = [math]::Round($job.BytesTotal / 1MB, 1)
                Write-Progress -Id $id -Activity "Downloading $($job.DisplayName)" `
                    -Status "${mb} MB / ${totalMb} MB ($pct%)" -PercentComplete $pct
            }
            else {
                Write-Progress -Id $id -Activity "Downloading $($job.DisplayName)" `
                    -Status "Connecting..."
            }
        }
        elseif ($job.JobState -eq "Transferred") {
            Write-Progress -Id $id -Activity "Downloading $($job.DisplayName)" -Completed
        }
        elseif ($job.JobState -eq "Error") {
            $err = $job.ErrorDescription
            $bitsJobs | ForEach-Object { Complete-BitsTransfer -BitsJob $_ -ErrorAction SilentlyContinue }
            throw "BITS download failed for $($job.DisplayName): $err"
        }
        $id++
    }
    if (-not $allComplete) { Start-Sleep -Milliseconds 500 }
} while (-not $allComplete)

# Complete all BITS jobs
foreach ($job in $bitsJobs) {
    Complete-BitsTransfer -BitsJob $job
}
Write-Host "Downloads complete."

# Verify checksums
$webClient = New-Object System.Net.WebClient
foreach ($dl in $downloads) {
    Write-Host "Verifying SHA256 for $($dl.Name)..."
    $expectedHash = $webClient.DownloadString($dl.Sha256Url).Trim().Split()[0]
    $actualHash = (Get-FileHash -Path $dl.ZipFile -Algorithm SHA256).Hash

    if ($actualHash -ine $expectedHash) {
        Remove-Item $dl.ZipFile -Force
        throw "$($dl.Name) SHA256 mismatch!`n  Expected: $expectedHash`n  Actual:   $actualHash"
    }
    Write-Host "$($dl.Name) SHA256 verified."
}
$webClient.Dispose()

# Ensure destination directory exists
if (-not (Test-Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir | Out-Null
}

# Extract SDK
Write-Host "Extracting SDK to $destDir..."
Expand-Archive -Path $downloads[0].ZipFile -DestinationPath $destDir -Force
Remove-Item $downloads[0].ZipFile -Force

$extractedDir = Join-Path $destDir "javafx-sdk-${Version}"
if (-not (Test-Path $extractedDir)) {
    throw "Expected extracted SDK directory not found: $extractedDir"
}

# Extract jmods into the SDK folder's jmods subfolder
$jmodsExtractDir = Join-Path $extractedDir "jmods"
Write-Host "Extracting jmods to $jmodsExtractDir..."
$jmodsTempDir = Join-Path $env:TEMP "openjfx-jmods-extract"
if (Test-Path $jmodsTempDir) { Remove-Item $jmodsTempDir -Recurse -Force }
Expand-Archive -Path $downloads[1].ZipFile -DestinationPath $jmodsTempDir -Force
Remove-Item $downloads[1].ZipFile -Force

$jmodsSourceDir = Join-Path $jmodsTempDir "javafx-jmods-${Version}"
if (-not (Test-Path $jmodsSourceDir)) {
    throw "Expected extracted jmods directory not found: $jmodsSourceDir"
}

if (-not (Test-Path $jmodsExtractDir)) {
    New-Item -ItemType Directory -Path $jmodsExtractDir | Out-Null
}
Copy-Item -Path (Join-Path $jmodsSourceDir "*") -Destination $jmodsExtractDir -Recurse -Force
Remove-Item $jmodsTempDir -Recurse -Force
Write-Host "jmods installed to $jmodsExtractDir"

# Create/recreate symbolic link using gsudo for elevation
if (Test-Path $symlinkPath) {
    Write-Host "Removing existing symlink at $symlinkPath..."
    gsudo cmd /c rmdir "$symlinkPath"
}

Write-Host "Creating symbolic link: $symlinkPath -> $extractedDir"
gsudo cmd /c mklink /D "$symlinkPath" "$extractedDir"

Write-Host "Done. OpenJFX $Version SDK with jmods installed at $symlinkPath"