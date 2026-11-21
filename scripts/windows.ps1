
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("audio-test", "test", "run", "package", "compile")]
    [string]$Action,

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArguments = @()
)

$ErrorActionPreference = "Stop"

# Resolve a raiz do projeto a partir da localização deste script.
$projectRoot = [System.IO.Path]::GetFullPath(
    (Join-Path $PSScriptRoot "..")
)

$pomPath = Join-Path $projectRoot "pom.xml"

if (!(Test-Path -LiteralPath $pomPath -PathType Leaf)) {
    throw "pom.xml não encontrado. Verifique a estrutura do projeto."
}

# Diretório público do Windows: precisa ter caminho ASCII.
$publicRoot = $env:PUBLIC

if ([string]::IsNullOrWhiteSpace($publicRoot)) {
    throw "A variável de ambiente PUBLIC não está configurada."
}

if ($publicRoot -match '[^\x00-\x7F]') {
    throw "O caminho PUBLIC contém caracteres não ASCII."
}

# Identificadores exclusivos para evitar conflitos entre execuções.
$token = [Guid]::NewGuid().ToString("N")

$aliasPath = Join-Path $publicRoot "voice-budget-api-$token"
$tempPath = Join-Path $publicRoot "voice-budget-tmp-$token"

# Preserva as variáveis originais do terminal.
$previousTemp = $env:TEMP
$previousTmp = $env:TMP

$junctionCreated = $false
$tempCreated = $false
$locationPushed = $false
$exitCode = 1

# Define o comando Maven conforme a operação solicitada.
switch ($Action) {
    "audio-test" {
        $mavenCommand = @(
            "-Dtest=WhisperTranscriptionServiceTest,PiperSynthesisServiceTest",
            "test"
        )
    }

    "test" {
        $mavenCommand = @("test")
    }

    "run" {
        $mavenCommand = @("spring-boot:run")
    }

    "package" {
        $mavenCommand = @("package")
    }

    "compile" {
        $mavenCommand = @("-DskipTests", "compile")
    }
}

$mavenCommand += $MavenArguments

try {
    if (!(Get-Command mvn -ErrorAction SilentlyContinue)) {
        throw "Maven não foi encontrado. Instale o Maven e configure o PATH."
    }

    # Cria uma pasta temporária ASCII.
    New-Item -ItemType Directory -Path $tempPath | Out-Null
    $tempCreated = $true

    # Cria uma junction para acessar o projeto sem caracteres Unicode.
    New-Item -ItemType Junction `
        -Path $aliasPath `
        -Target $projectRoot | Out-Null

    $junctionCreated = $true

    # Direciona arquivos temporários do processo Java para o caminho ASCII.
    $env:TEMP = $tempPath
    $env:TMP = $tempPath

    # Executa o Maven pela junction.
    Push-Location -LiteralPath $aliasPath
    $locationPushed = $true

    Write-Host ""
    Write-Host "Projeto: $aliasPath" -ForegroundColor Cyan
    Write-Host "Temporários: $tempPath" -ForegroundColor Cyan
    Write-Host "Comando Maven: mvn $($mavenCommand -join ' ')" -ForegroundColor Cyan
    Write-Host ""

    & mvn @mavenCommand

    $exitCode = $LASTEXITCODE

} catch {
    Write-Host ""
    Write-Host "Falha na execução: $($_.Exception.Message)" -ForegroundColor Red
    $exitCode = 1

} finally {

    if ($locationPushed) {
        Pop-Location
    }

    # Restaura as variáveis originais do terminal.
    $env:TEMP = $previousTemp
    $env:TMP = $previousTmp

    # Remove somente a junction, nunca a pasta original do projeto.
    if ($junctionCreated) {
        & $env:ComSpec /d /c "rmdir `"$aliasPath`""

        if ($LASTEXITCODE -ne 0) {
            Write-Warning "Não foi possível remover a junction: $aliasPath"
        }
    }

    # Remove a pasta temporária depois que o Maven terminar.
    if ($tempCreated) {
        Remove-Item -LiteralPath $tempPath -Recurse -Force `
            -ErrorAction SilentlyContinue
    }
}

exit $exitCode
