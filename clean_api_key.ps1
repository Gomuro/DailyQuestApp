# PowerShell script to clean API key from git history
# This script requires the BFG Repo-Cleaner tool: https://rtyley.github.io/bfg-repo-cleaner/

# Instructions:
# 1. Download BFG from https://repo1.maven.org/maven2/com/madgag/bfg/1.14.0/bfg-1.14.0.jar
# 2. Place the jar file in the same directory as this script
# 3. Run this script from PowerShell

# Create a text file with the API key pattern to replace
$apiKeyPattern = @"
OPENAI_API_KEY="sk-.*"
"@

$apiKeyPattern | Out-File -FilePath "api-key-pattern.txt" -Encoding utf8

# Check if BFG jar exists
if (-not (Test-Path "bfg-1.14.0.jar")) {
    Write-Host "BFG jar file not found. Downloading..."
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/madgag/bfg/1.14.0/bfg-1.14.0.jar" -OutFile "bfg-1.14.0.jar"
}

# Run BFG to replace text
Write-Host "Running BFG to clean repository..."
java -jar bfg-1.14.0.jar --replace-text api-key-pattern.txt

# Instructions for completing the cleanup
Write-Host @"

BFG has modified your repository history. To complete the cleanup:

1. Run these commands:
   git reflog expire --expire=now --all
   git gc --prune=now --aggressive

2. Push the cleaned history to your remote repository:
   git push --force

IMPORTANT: All collaborators should re-clone the repository after this operation.
"@

# Clean up the pattern file
Remove-Item -Path "api-key-pattern.txt" -Force 