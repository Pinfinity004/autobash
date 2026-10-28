#Requires -Version 5.1
<#
    AutoBash -> GitHub one-shot publisher.

    BEFORE RUNNING: create an EMPTY repository on GitHub.
    No README, no .gitignore, no license — an empty repo, or the first push is rejected.

    Usage:
        .\push.ps1 -RepoUrl https://github.com/<your-name>/autobash.git

    The first push opens a browser window (Git Credential Manager) to sign in to
    GitHub. No token is stored in this folder and none is needed.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RepoUrl,

    [string]$Branch  = 'main',
    [string]$Message = 'AutoBash 7.4 - import source and project docs'
)

$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

function Stop-Here([string]$Text) {
    Write-Host ''
    Write-Host "  FAILED: $Text" -ForegroundColor Red
    Write-Host ''
    exit 1
}

function Step([string]$Text) {
    Write-Host ''
    Write-Host "== $Text" -ForegroundColor Cyan
}

# --- 1. Git present? ------------------------------------------------------
Step 'Checking Git'
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Host ''
    Write-Host '  Git is not installed (or not on PATH).' -ForegroundColor Yellow
    Write-Host '  Install it once with:'
    Write-Host '      winget install --id Git.Git -e --source winget'
    Write-Host '  Then close this window, open a NEW one, and re-run this script.'
    exit 1
}
Write-Host ('   ' + (git --version))

# --- 2. Remote URL --------------------------------------------------------
Step 'Validating repository URL'
if ($RepoUrl -notmatch '^(https://|git@)') {
    Stop-Here 'That does not look like a Git URL. Use https://github.com/<user>/<repo>.git'
}
if ($RepoUrl -notmatch '\.git$') { $RepoUrl = "$RepoUrl.git" }
Write-Host "   $RepoUrl"

# --- 3. Commit identity ---------------------------------------------------
Step 'Checking commit identity'
$name  = git config --global --get user.name
$email = git config --global --get user.email
if ([string]::IsNullOrWhiteSpace($name)) {
    $name = Read-Host '   Name for commits'
    git config --global user.name $name
}
if ([string]::IsNullOrWhiteSpace($email)) {
    $email = Read-Host '   Email for commits'
    git config --global user.email $email
}
Write-Host "   $name <$email>"

# --- 4. Init --------------------------------------------------------------
Step 'Preparing local repository'
if (Test-Path (Join-Path $PSScriptRoot '.git')) {
    Write-Host '   already a git repository'
} else {
    git init -b $Branch | Out-Null
    Write-Host "   initialised on branch '$Branch'"
}

# --- 5. Stage + commit ----------------------------------------------------
Step 'Staging and committing'
git add -A
$pending = @(git status --porcelain)
if ($pending.Count -eq 0) {
    Write-Host '   nothing new to commit'
} else {
    git commit -q -m $Message
    Write-Host "   committed $($pending.Count) path(s)"
}

# --- 6. Remote ------------------------------------------------------------
Step "Wiring remote 'origin'"
if (@(git remote) -contains 'origin') {
    git remote set-url origin $RepoUrl
    Write-Host '   origin updated'
} else {
    git remote add origin $RepoUrl
    Write-Host '   origin added'
}

# --- 7. Push --------------------------------------------------------------
Step 'Pushing to GitHub'
Write-Host '   a browser window may open for sign-in' -ForegroundColor Yellow
git push -u origin $Branch
if ($LASTEXITCODE -ne 0) {
    Stop-Here @"
git push was rejected. Most likely causes:

  * The GitHub repo is NOT empty. If you let GitHub add a README, .gitignore or
    license on creation, delete the repo and create a truly empty one.
    Or run:  git pull --rebase origin $Branch  and re-run this script.
  * You signed in as an account that does not own this repo.
  * Typo in -RepoUrl.

"@
}

Write-Host ''
Write-Host '  Pushed successfully.' -ForegroundColor Green
Write-Host ('  ' + ($RepoUrl -replace '\.git$', ''))
Write-Host ''
