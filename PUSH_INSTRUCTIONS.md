# Publishing this repository to GitHub

Step-by-step. Total time: about five minutes, most of it waiting for the Git installer.

---

## Step 1 — Install Git (one time only)

```powershell
winget install --id Git.Git -e --source winget
```

**Close this terminal and open a new one** — the installer changes your `PATH`, and the
old window will not see it. Verify with:

```powershell
git --version
```

If `winget` is unavailable, download Git from <https://git-scm.com/download/win> and accept
the defaults during setup.

---

## Step 2 — Create an empty repository on GitHub

1. Go to <https://github.com/new>.
2. **Repository name:** `autobash`
3. **Visibility:** Private (recommended, at least while you decide on a license).
4. **Do not** tick "Add a README file", ".gitignore", or "license". Leave them all off.
   A repo with any initial commit will reject the first push.
5. Click **Create repository**.
6. Copy the URL shown, e.g. `https://github.com/yourname/autobash.git`.

---

## Step 3 — Push

Open PowerShell and run:

```powershell
cd "C:\Users\Pinfinity\OneDrive\Documents\deepseek-harness\default-workspace\autobash"
.\push.ps1 -RepoUrl https://github.com/YOURNAME/autobash.git
```

If PowerShell blocks the script with an execution-policy error, run it this way instead:

```powershell
powershell -ExecutionPolicy Bypass -File .\push.ps1 -RepoUrl https://github.com/YOURNAME/autobash.git
```

A browser window may open for GitHub sign-in on the first push. That is Git Credential
Manager — it stores the credential in Windows Credential Manager, not in this folder.

Expect the final output:

```
  Pushed successfully.
  https://github.com/YOURNAME/autobash
```

Refresh the repository page and the files will be there.

---

## What the script actually does

It is short and readable — open `push.ps1` and check it. In order:

| # | Action |
|---|---|
| 1 | Verifies `git` is installed, tells you the install command if not |
| 2 | Normalises the repo URL (appends `.git` if missing) |
| 3 | Sets `user.name` / `user.email` globally if they are unset (prompts once) |
| 4 | `git init -b main` if this folder is not yet a repository |
| 5 | `git add -A` + one initial commit |
| 6 | Adds `origin`, or updates its URL if it exists |
| 7 | `git push -u origin main` |

No force-push, no history rewriting, no credentials stored on disk. Running it a second time
after edits just commits and pushes again.

## Prefer to do it by hand?

```powershell
cd "C:\Users\Pinfinity\OneDrive\Documents\deepseek-harness\default-workspace\autobash"
git init -b main
git add -A
git commit -m "AutoBash 7.4 - import source and project docs"
git remote add origin https://github.com/YOURNAME/autobash.git
git push -u origin main
```

---

## If something goes wrong

**`git : The term 'git' is not recognized`**
Step 1 did not complete, or you did not open a new terminal afterwards.

**`failed to push some refs` / `updates were rejected`**
The GitHub repo has a commit you do not have — almost always the README you ticked on
creation. Either delete and recreate the empty repo, or run:

```powershell
git pull --rebase origin main
.\push.ps1 -RepoUrl https://github.com/YOURNAME/autobash.git
```

**`Authentication failed`**
Run `git credential-manager github logout`, then push again and sign in with the account
that owns the repo.

**`remote origin already exists`**
The script handles this (it updates the URL), but by hand: `git remote set-url origin <url>`.

---

## After the push

- **APK** — create a Release on GitHub and attach the `.apk` there. It is deliberately
  excluded from the tree by `.gitignore`.
- **License** — `LICENSE` currently contains a placeholder. Pick one before making the repo
  public; MIT or Apache-2.0 are the usual choices for an app like this.
- **Do not commit signing keys.** `*.jks` / `*.keystore` / `keystore.properties` are already
  ignored. If you ever sign a release build, keep the keystore outside the repo.
