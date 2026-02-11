# Java setup for banking-project

You need a **JDK 17 or 21** to run the services. Java was not found on your system.

## Option 1: Install with winget (recommended)

Open **PowerShell** and run:

```powershell
winget install EclipseAdoptium.Temurin.17.JDK --accept-package-agreements
```

Close and reopen PowerShell (or your terminal), then:

```powershell
cd C:\Users\vishw\banking-project
.\run-account-service.ps1
```

The script will find the new JDK and start Account Service.

---

## Option 2: Manual install

1. Download **JDK 17** from https://adoptium.net/ (choose Windows x64 `.msi`).
2. Run the installer. Check **“Set JAVA_HOME variable”** and **“Add to PATH”** if offered.
3. Close and reopen PowerShell, then run:

```powershell
cd C:\Users\vishw\banking-project
.\run-account-service.ps1
```

---

## Option 3: Java is already installed elsewhere

If you installed Java in a custom folder:

**PowerShell (one session):**

```powershell
$env:JAVA_HOME = "C:\Path\To\Your\jdk-17"   # use your real path
cd C:\Users\vishw\banking-project
.\mvnw.cmd spring-boot:run -pl account-service
```

**To find your JDK:** look for a folder that contains `bin\java.exe`, for example under:

- `C:\Program Files\Java\`
- `C:\Program Files\Eclipse Adoptium\`
- `C:\Program Files\Microsoft\`

---

## Run scripts (PowerShell)

- **Account Service:**  
  `.\run-account-service.ps1`

- **Transaction Service:**  
  Set `JAVA_HOME` as above, then:  
  `.\mvnw.cmd spring-boot:run -pl transaction-service`

- **Notification Service:**  
  `.\mvnw.cmd spring-boot:run -pl notification-service`

Note: `call set-JAVA_HOME.cmd` is for **Command Prompt (cmd)**, not PowerShell. In PowerShell use `$env:JAVA_HOME = "..."` or the `.ps1` script.
