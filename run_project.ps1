$ErrorActionPreference = "Stop"

# Configuration
$MavenVersion = "3.9.6"
$MavenUrl = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
$MavenDir = "apache-maven-$MavenVersion"
$MavenBin = "$PSScriptRoot\$MavenDir\bin\mvn.cmd"

# Check if Maven is already installed globally
if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
    Write-Host "Maven found globally. Running project..."
    mvn javafx:run
    exit
}

# Check if local Maven exists
if (-not (Test-Path "$PSScriptRoot\$MavenDir")) {
    Write-Host "Maven not found. Downloading Maven $MavenVersion..."
    
    $ZipFile = "$PSScriptRoot\maven.zip"
    Invoke-WebRequest -Uri $MavenUrl -OutFile $ZipFile
    
    Write-Host "Extracting Maven..."
    Expand-Archive -Path $ZipFile -DestinationPath $PSScriptRoot -Force
    Remove-Item $ZipFile
    
    Write-Host "Maven installed to $PSScriptRoot\$MavenDir"
}

# Run the project using local Maven
Write-Host "Running project with local Maven..."
& $MavenBin javafx:run
