<#
.SYNOPSIS
    Downloads and installs Eclipse Temurin JDK.
.DESCRIPTION
    Fetches Eclipse Temurin JDK images from the Adoptium API and unpacks them to a local folder.
    Creates a symbolic link to the installed JDK (requires elevated privileges via gsudo).
.PARAMETER Version
    The JDK version to install (default: 21)
.EXAMPLE
    .\Install-TemurinJDK.ps1 -v 21
#>

param(
    [Alias('v')]
    [string]$Version = '21'
)

$JDK_VERSION = $Version
$SUDO_COMMAND = 'gsudo'
$INSTALL_DIR = "$env:USERPROFILE\Applications"

Write-Host "The script might prompt for elevated privileges. This is because"
Write-Host "gsudo is used to gain permissions to create symbolic links"
Write-Host

Write-Host "JDK requested is $JDK_VERSION"

$JDK_URI = "https://api.adoptium.net/v3/binary/latest/$JDK_VERSION/ga/windows/x64/jdk/hotspot/normal/eclipse/"
$JDK_STRING = "project=jdk"
$RELEASE_INFO = "https://api.adoptium.net/v3/assets/feature_releases/$JDK_VERSION/ga/"
$RELEASE_STRING = "architecture=x64&heap_size=normal&image_type=jdk&os=windows&page=0&page_size=10&project=jdk&sort_method=DEFAULT&sort_order=DESC&vendor=eclipse"

# Create temp files
$TMPFILE = [System.IO.Path]::GetTempFileName() + ".zip"
$TMPJSON = [System.IO.Path]::GetTempFileName()

try {
    # Download JDK
    Write-Host "Downloading ${JDK_URI}?${JDK_STRING}..."
    $ProgressPreference = 'SilentlyContinue'  # Speeds up download significantly
    Invoke-WebRequest -Uri "${JDK_URI}?${JDK_STRING}" -OutFile $TMPFILE -UseBasicParsing
    $ProgressPreference = 'Continue'

    # Query versions
    $jsonResponse = Invoke-WebRequest -Uri "${RELEASE_INFO}?${RELEASE_STRING}" -UseBasicParsing
    $jsonResponse.Content | Out-File -FilePath $TMPJSON -Encoding UTF8

    # Parse JSON and get checksums
    $releaseData = $jsonResponse.Content | ConvertFrom-Json
    $EXPECTED_SHA256 = $releaseData[0].binaries[0].package.checksum

    # Calculate file SHA256
    $fileHash = Get-FileHash -Path $TMPFILE -Algorithm SHA256
    $REAL_SHA256 = $fileHash.Hash.ToLower()

    Write-Host "Expected SHA256 checksum: $EXPECTED_SHA256"
    Write-Host "File SHA256 checksum:     $REAL_SHA256"

    if ($EXPECTED_SHA256 -ne $REAL_SHA256) {
        Write-Host "Cleaning up..."
        Remove-Item -Path $TMPFILE -ErrorAction SilentlyContinue
        Write-Host "Checksum mismatch (using URI below)! Exiting."
        Write-Host $RELEASE_INFO
        exit 1
    }

    # Get release name
    $RELEASE_NAME = $releaseData[0].release_name
    Write-Host $RELEASE_NAME
    Write-Host "Unpacking $RELEASE_NAME"

    # Ensure install directory exists
    if (-not (Test-Path $INSTALL_DIR)) {
        New-Item -ItemType Directory -Path $INSTALL_DIR -Force | Out-Null
    }

    # Unpack to install directory
    Set-Location $INSTALL_DIR

    # Remove existing release directory if present
    if (Test-Path $RELEASE_NAME) {
        Remove-Item -Path $RELEASE_NAME -Recurse -Force
    }

    # Extract the zip file
    Expand-Archive -Path $TMPFILE -DestinationPath $INSTALL_DIR -Force

    # Recreate symlink
    $symlinkPath = "jdk-$JDK_VERSION.jdk"
    
    if (Test-Path $symlinkPath) {
        Remove-Item -Path $symlinkPath -Recurse -Force
    }

    # Use gsudo to create symbolic link (requires elevated privileges)
    & $SUDO_COMMAND New-Item -ItemType SymbolicLink -Path $symlinkPath -Target $RELEASE_NAME

    Write-Host "Installation complete: $INSTALL_DIR\$symlinkPath -> $RELEASE_NAME"
}
finally {
    # Return to home directory
    Set-Location $env:USERPROFILE
    
    # Cleanup temp files
    if (Test-Path $TMPFILE) {
        Remove-Item -Path $TMPFILE -ErrorAction SilentlyContinue
    }
    if (Test-Path $TMPJSON) {
        Remove-Item -Path $TMPJSON -ErrorAction SilentlyContinue
    }
}
