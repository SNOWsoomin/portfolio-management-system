$ErrorActionPreference = "Stop"

$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$logFile = Join-Path $root "verify-scope.log"
$serverProcess = $null

function Assert-True($condition, $message) {
    if (-not $condition) {
        throw "VERIFY FAILED: $message"
    }
    Write-Host "[OK] $message"
}

function Api($method, $path, $token = $null, $body = $null) {
    $headers = @{}
    if ($token) {
        $headers.Authorization = "Bearer $token"
    }
    $params = @{
        Method = $method
        Uri = "http://localhost:8080$path"
        Headers = $headers
        TimeoutSec = 10
    }
    if ($null -ne $body) {
        $params.ContentType = "application/json; charset=utf-8"
        $params.Body = ($body | ConvertTo-Json -Depth 10)
    }
    return Invoke-RestMethod @params
}

function Expect-Blocked($path, $token = $null) {
    try {
        $headers = @{}
        if ($token) {
            $headers.Authorization = "Bearer $token"
        }
        Invoke-WebRequest -UseBasicParsing -Uri "http://localhost:8080$path" -Headers $headers -TimeoutSec 5 | Out-Null
        return $false
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        return ($status -eq 401 -or $status -eq 403)
    }
}

try {
    Push-Location $root
    & .\gradlew.bat clean build | Out-Host
    Pop-Location
    Assert-True $true "Gradle build passed"

    Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
        Where-Object { $_.OwningProcess -ne 0 } |
        ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }

    $javaPrefix = ""
    if ($env:JAVA_HOME) {
        $javaPrefix = "`$env:PATH='$($env:JAVA_HOME)\bin;' + `$env:PATH; "
    }
    $cmd = "$javaPrefix Set-Location '$root'; .\gradlew.bat bootRun *> '$logFile'"
    $serverProcess = Start-Process -FilePath powershell -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", $cmd) -WindowStyle Hidden -PassThru

    $ready = $false
    for ($i = 0; $i -lt 90; $i++) {
        try {
            Api "GET" "/api/health" | Out-Null
            $ready = $true
            break
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    if (-not $ready) {
        Get-Content $logFile -Tail 120
        throw "server did not start"
    }
    Assert-True $true "Server started"

    $health = Api "GET" "/api/health"
    Assert-True ($health.data -eq "ok") "Health endpoint works"

    $scope = Api "GET" "/api/dev/scope"
    Assert-True ($scope.data.moduleName -eq "backend-johyeonmin-scope") "Scope endpoint works"

    Assert-True (Expect-Blocked "/api/users/me") "JWT is required for protected endpoints"

    $userLogin = Api "POST" "/api/auth/login" $null @{ email = "user@test.com"; password = "user1234" }
    $userToken = $userLogin.data.accessToken
    Assert-True ($userToken) "USER login returns JWT"

    $adminLogin = Api "POST" "/api/auth/login" $null @{ email = "admin@test.com"; password = "admin1234" }
    $adminToken = $adminLogin.data.accessToken
    Assert-True ($adminToken) "ADMIN login returns JWT"

    $email = "scope-check-" + (Get-Date -Format "yyyyMMddHHmmss") + "@test.com"
    Api "POST" "/api/auth/signup" $null @{ email = $email; password = "test1234"; name = "Scope Check" } | Out-Null
    $signupLogin = Api "POST" "/api/auth/login" $null @{ email = $email; password = "test1234" }
    Assert-True ($signupLogin.data.accessToken) "Signup + login flow works"

    $me = Api "GET" "/api/users/me" $userToken
    Assert-True ($me.data.email -eq "user@test.com") "Current user endpoint works"

    $skills = Api "GET" "/api/skills" $userToken
    Assert-True ($skills.data.Count -ge 10) "Skill master data is loaded"

    $userSkills = Api "GET" "/api/users/me/skills" $userToken
    Assert-True ($userSkills.data.Count -ge 4) "User skill seed data is loaded"

    $dockerSkill = $skills.data | Where-Object { $_.name -eq "Docker" } | Select-Object -First 1
    Api "POST" "/api/users/me/skills" $userToken @{ skillId = $dockerSkill.id; level = "ADVANCED" } | Out-Null
    $updatedSkills = Api "GET" "/api/users/me/skills" $userToken
    Assert-True (@($updatedSkills.data | Where-Object { $_.name -eq "Docker" }).Count -gt 0) "User skill add/update works"

    Assert-True (Expect-Blocked "/api/admin/users" $userToken) "USER cannot access admin API"
    $adminUsers = Api "GET" "/api/admin/users" $adminToken
    Assert-True (@($adminUsers.data | Where-Object { $_.email -eq "admin@test.com" }).Count -gt 0) "ADMIN can access admin API"

    Write-Host ""
    Write-Host "ALL SCOPE VERIFICATIONS PASSED"
} finally {
    if ($serverProcess -and -not $serverProcess.HasExited) {
        Stop-Process -Id $serverProcess.Id -Force -ErrorAction SilentlyContinue
    }
    Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue |
        Where-Object { $_.OwningProcess -ne 0 } |
        ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
}
