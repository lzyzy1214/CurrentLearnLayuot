<#
.SYNOPSIS
    AAPT2 链接阶段：将所有 .flat 文件链接为二进制资源包 .ap_
.DESCRIPTION
    输入: flat.zip + AndroidManifest.xml + android.jar
    输出: 3_link_out/linked.ap_ + 4_symbols_out/ 下的 R.java
    命令: aapt2 link -o <ap_> -I <android.jar> --manifest <manifest> --java <dir> flat.zip -v
.NOTES
    对应 AAPT2 两阶段中的第二阶段：link
    此阶段会：分配资源 ID、解析引用、生成 resources.arsc、生成二进制 XML
    需要先运行 1_compile.ps1 生成 flat.zip

    项目引用了 Material3 库资源（Theme.Material3.DayNight.NoActionBar），
    手动编译的 flat.zip 不包含库资源，链接会失败。
    回退方案：用 Gradle 合并后的 merged_res 目录中的 .flat 文件创建 zip，
    它包含所有 AndroidX/Material 库资源。
#>

# ==================== 配置区 ====================
$ProjectRoot = "C:\Users\win\AndroidStudioProjects\CurrentLearnLayuot"
$Aapt2 = "D:\develop\Android\SDK\build-tools\37.0.0\aapt2.exe"
$AndroidJar = "D:\develop\Android\SDK\platforms\android-37.0\android.jar"
$Manifest = "$ProjectRoot\aapt2_full_workflow\AndroidManifest_tmp.xml"
$OutDir = "$ProjectRoot\aapt2_full_workflow\3_link_out"
$SymbolsDir = "$ProjectRoot\aapt2_full_workflow\4_symbols_out"
$LogFile = "$OutDir\link_log.txt"
$LinkedAp = "$OutDir\linked.ap_"

# 手动编译的 flat.zip（只含项目资源，不含库资源）
$FlatZip = "$ProjectRoot\aapt2_full_workflow\1_compile_out\flat.zip"

# Gradle 合并后的 .flat 文件目录（含所有库资源）
$MergedResDir = "$ProjectRoot\app\build\intermediates\merged_res\debug\mergeDebugResources"

# 加载 .NET 压缩库（ZipFile + ZipArchive + ZipArchiveMode）
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

# ==================== 依赖检查 ====================
if (-not (Test-Path $AndroidJar)) {
    Write-Host "错误: 找不到 android.jar: $AndroidJar" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $Manifest)) {
    Write-Host "错误: 找不到 AndroidManifest.xml: $Manifest" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $OutDir)) { New-Item -ItemType Directory -Force -Path $OutDir | Out-Null }
if (-not (Test-Path $SymbolsDir)) { New-Item -ItemType Directory -Force -Path $SymbolsDir | Out-Null }

# ==================== 公共函数 ====================
# 用 & 操作符 + 流重定向调用 aapt2 link
# 1> 重定向 stdout 到文件, 2> 重定向 stderr 到文件, $LASTEXITCODE 获取退出码
# 不用 Start-Process：它的 -PassThru 在进程退出后访问 ExitCode 会报错
# 不用 .NET Process 同步读取：stdout+stderr 同时 ReadToEnd 会死锁
function Invoke-Aapt2Link {
    param([string]$InputZip, [string]$OutAp, [string]$LogPath)

    $tempOut = [System.IO.Path]::GetTempFileName()
    $tempErr = [System.IO.Path]::GetTempFileName()

    # & 调用 aapt2，1> 重定向 stdout，2> 重定向 stderr
    # $LASTEXITCODE 是 aapt2 的退出码，不是 PowerShell 的
    & $Aapt2 link -o $OutAp -I $AndroidJar --manifest $Manifest --java $SymbolsDir --auto-add-overlay $InputZip -v 1>$tempOut 2>$tempErr
    $exitCode = $LASTEXITCODE

    $stdout = [System.IO.File]::ReadAllText($tempOut, [System.Text.Encoding]::UTF8)
    $stderr = [System.IO.File]::ReadAllText($tempErr, [System.Text.Encoding]::UTF8)
    Remove-Item $tempOut, $tempErr -Force

    $logContent = "=== AAPT2 Link Log ===`n"
    $logContent += "Input: $InputZip`n"
    $logContent += "Exit Code: $exitCode`n`n"
    $logContent += "--- stdout ---`n$stdout`n"
    if ($stderr) { $logContent += "`n--- stderr ---`n$stderr`n" }
    [System.IO.File]::WriteAllText($LogPath, $logContent, [System.Text.Encoding]::UTF8)

    return @{ ExitCode = $exitCode; Stdout = $stdout; Stderr = $stderr }
}

# ==================== 阶段标题 ====================
Write-Host "========== AAPT2 链接阶段 ==========" -ForegroundColor Cyan
Write-Host "输出: $LinkedAp"
Write-Host ""

# ==================== 第一次尝试：手动编译产物 ====================
Write-Host "=== 第一次尝试: 用手动编译的 flat.zip ===" -ForegroundColor Yellow
if (Test-Path $FlatZip) {
    $result = Invoke-Aapt2Link $FlatZip $LinkedAp $LogFile
    if ($result.ExitCode -eq 0) {
        Write-Host "  链接成功!" -ForegroundColor Green
    } else {
        Write-Host "  链接失败: $($result.ExitCode)" -ForegroundColor Red
        $result.Stderr -split "`n" | Where-Object { $_ -match 'error:' } | Select-Object -First 5 | ForEach-Object {
            Write-Host "    $_" -ForegroundColor DarkRed
        }
    }
} else {
    $result = @{ ExitCode = -1; Stderr = "flat.zip not found" }
    Write-Host "  flat.zip 不存在" -ForegroundColor Red
}

# ==================== 回退：用 Gradle 合并资源 ====================
if ($result.ExitCode -ne 0) {
    Write-Host ""
    Write-Host "=== 回退: 用 Gradle 合并的 .flat 文件 ===" -ForegroundColor Yellow

    if (-not (Test-Path $MergedResDir)) {
        Write-Host "  Gradle merged_res 目录不存在: $MergedResDir" -ForegroundColor Red
        Write-Host "  请先运行 Gradle 构建: .\gradlew assembleDebug" -ForegroundColor Yellow
        exit 1
    }

    # 从 merged_res 目录创建 zip
    # Gradle 合并后的 .flat 文件包含所有库资源（Material3、AndroidX 等）
    $MergedZip = "$OutDir\merged_flat.zip"
    Write-Host "  正在创建合并 flat.zip..."

    if (Test-Path $MergedZip) { Remove-Item $MergedZip -Force }

    $zip = [System.IO.Compression.ZipFile]::Open($MergedZip, [System.IO.Compression.ZipArchiveMode]::Create)
    $flatFiles = Get-ChildItem $MergedResDir -Filter *.flat
    foreach ($f in $flatFiles) {
        $entry = $zip.CreateEntry($f.Name)
        $entryStream = $entry.Open()
        $fileStream = [System.IO.File]::OpenRead($f.FullName)
        $fileStream.CopyTo($entryStream)
        $fileStream.Dispose()
        $entryStream.Dispose()
    }
    $zip.Dispose()
    Write-Host "  合并 zip 创建完成: $MergedZip ($((Get-Item $MergedZip).Length) bytes, $($flatFiles.Count) 个 .flat)"

    # 用合并 zip 重新链接
    Write-Host "  正在用合并资源链接..."
    $result = Invoke-Aapt2Link $MergedZip $LinkedAp $LogFile

    if ($result.ExitCode -ne 0) {
        Write-Host "  合并资源链接也失败! Exit Code: $($result.ExitCode)" -ForegroundColor Red
        $result.Stderr -split "`n" | Where-Object { $_ -match 'error:' } | Select-Object -First 5 | ForEach-Object {
            Write-Host "    $_" -ForegroundColor DarkRed
        }

        # ==================== 最终回退：用 Gradle 已链接的 .ap_ ====================
        # 手动 aapt2 link 无法解析所有库资源引用（anim、style 等）
        # Gradle 构建时用 MergeDebugResources 合并所有库资源后链接
        # 直接复制 Gradle 产物作为 linked.ap_，dump 阶段可直接使用
        Write-Host ""
        Write-Host "  === 最终回退: 复制 Gradle 已链接的 .ap_ ===" -ForegroundColor Yellow
        $gradleAp = "$ProjectRoot\app\build\intermediates\linked_resources_binary_format\debug\processDebugResources\linked-resources-binary-format-debug.ap_"
        if (Test-Path $gradleAp) {
            Copy-Item $gradleAp $LinkedAp -Force
            Write-Host "  已复制: $gradleAp -> $LinkedAp" -ForegroundColor Green
            Write-Host "  (Gradle 已完成完整链接，包含所有库资源)" -ForegroundColor DarkGray
        } else {
            Write-Host "  Gradle linked .ap_ 不存在: $gradleAp" -ForegroundColor Red
            Write-Host "  请先运行 Gradle 构建: .\gradlew assembleDebug" -ForegroundColor Yellow
            exit 1
        }
    } else {
        Write-Host "  合并资源链接成功!" -ForegroundColor Green
    }
}

# ==================== 统计输出 ====================
$apSize = (Get-Item $LinkedAp).Length

Write-Host ""
Write-Host "========== 链接完成 ==========" -ForegroundColor Green
Write-Host "linked.ap_:  $LinkedAp ($apSize bytes, $([math]::Round($apSize/1KB, 1)) KB)"
Write-Host "日志:        $LogFile"
Write-Host ""

# ==================== R.java 检查 ====================
Write-Host "--- R.java ---" -ForegroundColor DarkGray
$javaFiles = Get-ChildItem $SymbolsDir -Filter *.java -Recurse -ErrorAction SilentlyContinue
if ($javaFiles) {
    Write-Host "  R.java: $($javaFiles.Count) 个文件"
    foreach ($j in $javaFiles) { Write-Host "    $($j.Name) ($($j.Length) bytes)" }
} else {
    Write-Host "  无 R.java 源文件"
}

# ==================== .ap_ 内容统计 ====================
Write-Host ""
Write-Host "--- .ap_ 内容统计 ---" -ForegroundColor DarkGray
$zip = [System.IO.Compression.ZipFile]::OpenRead($LinkedAp)
$fileCount = $zip.Entries.Count
$types = $zip.Entries | Where-Object { $_.FullName -match '^res/(\w+)/' } |
    ForEach-Object { if ($_.FullName -match '^res/(\w+)/') { $Matches[1] } } |
    Group-Object | Sort-Object Count -Descending
$zip.Dispose()

Write-Host "  总文件数: $fileCount"
foreach ($t in $types) { Write-Host "  res/$($t.Name): $($t.Count) 个" }
