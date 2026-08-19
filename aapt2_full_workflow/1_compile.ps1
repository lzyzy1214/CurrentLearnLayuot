<#
.SYNOPSIS
    AAPT2 编译阶段：将 res/ 下所有资源编译为 .flat (protobuf) 文件
.DESCRIPTION
    输入: app/src/main/res/ 下的所有 XML 资源
    输出: 1_compile_out/ 目录下的 flat.zip + 解压后的 .flat 文件 + R.txt
    命令: aapt2 compile --dir <res> -o <flat.zip> --output-text-symbols <R.txt> -v
.NOTES
    对应 AAPT2 两阶段中的第一阶段：compile
    每个 XML 文件被独立编译为 PROTO_XML 格式的 .flat 文件
#>

# ==================== 配置区 ====================
# 项目根目录，所有路径都基于此拼接
$ProjectRoot = "C:\Users\win\AndroidStudioProjects\CurrentLearnLayuot"

# AAPT2 可执行文件完整路径。build-tools 37.0.0 对应 Android 16+
# 必须用完整路径，PowerShell 不认识 "aapt2" 命令
$Aapt2 = "D:\develop\Android\SDK\build-tools\37.0.0\aapt2.exe"

# 输入目录：res/ 包含 layout、drawable、values 等所有资源子目录
# 这是人写的 XML 文件的来源
$ResDir = "$ProjectRoot\app\src\main\res"

# 输出目录。编号 1_ 表示第一阶段产物，编译产物全部存这里
$OutDir = "$ProjectRoot\aapt2_full_workflow\1_compile_out"

# ==================== 输出文件路径 ====================
# 导出文件1: flat.zip。aapt2 compile --dir 的输出是一个 zip，内含所有 .flat 文件
# 为什么是 zip：aapt2 --dir 模式自动打包为 zip，不是单独的 .flat 文件
$OutZip = "$OutDir\flat.zip"

# 导出文件2: R.txt。资源符号表，列出所有资源名和类型
# 展示什么：编译阶段 ID 全为 0x0（未分配），证明 ID 是 link 阶段才分配的
# 格式示例: int layout activity_main 0x0
$OutRtxt = "$OutDir\R.txt"

# 解压目录。flat.zip 解压到这里，得到单独的 .flat 文件
# 为什么要解压：后续脚本(4_dump_apc, 2_proto_decode)需要逐个处理 .flat 文件
$FlatDir = "$OutDir\flat"

# 导出文件3: 编译日志。记录 aapt2 的完整 stdout/stderr 输出
# 为什么：排查编译错误时需要看详细日志
$LogFile = "$OutDir\compile_log.txt"

# ==================== 准备阶段 ====================

# 如果输出目录不存在，创建它。首次运行时目录还不存在
if (-not (Test-Path $OutDir)) { New-Item -ItemType Directory -Force -Path $OutDir | Out-Null }

# 如果解压目录已存在，先删除——避免旧 .flat 文件残留干扰本次结果
if (Test-Path $FlatDir) { Remove-Item $FlatDir -Recurse -Force }

# 重新创建空的解压目录
New-Item -ItemType Directory -Force -Path $FlatDir | Out-Null

# 打印阶段标题和输入输出路径，让用户确认路径是否正确
Write-Host "========== AAPT2 编译阶段 ==========" -ForegroundColor Cyan
Write-Host "输入: $ResDir"
Write-Host "输出: $OutDir"
Write-Host ""

# ==================== 编译阶段（核心） ====================
# 用 Start-Process 重定向 stdout/stderr 到临时文件
# 为什么不用 .NET Process 同步读取：
#   aapt2 verbose 输出走 stderr，同步 ReadToEnd stdout+stderr 会死锁
#   async 事件在 WaitForExit 期间不被 PowerShell 处理
# Start-Process -RedirectStandardOutput/Error 由 OS 重定向到文件，无死锁
$tempOut = [System.IO.Path]::GetTempFileName()
$tempErr = [System.IO.Path]::GetTempFileName()

Write-Host "正在编译..." -ForegroundColor Yellow
$proc = Start-Process -FilePath $Aapt2 `
    -ArgumentList "compile --dir `"$ResDir`" -o `"$OutZip`" --output-text-symbols `"$OutRtxt`" -v" `
    -NoNewWindow -Wait -PassThru `
    -RedirectStandardOutput $tempOut -RedirectStandardError $tempErr
$exitCode = $proc.ExitCode

$stdout = [System.IO.File]::ReadAllText($tempOut, [System.Text.Encoding]::UTF8)
$stderr = [System.IO.File]::ReadAllText($tempErr, [System.Text.Encoding]::UTF8)
Remove-Item $tempOut, $tempErr -Force

# ==================== 保存日志 ====================
$logContent = "=== AAPT2 Compile Log ===`n"
$logContent += "Command: aapt2 compile --dir `"$ResDir`" -o `"$OutZip`" --output-text-symbols `"$OutRtxt`" -v`n"
$logContent += "Exit Code: $exitCode`n`n"
$logContent += "--- stdout ---`n$stdout`n"
if ($stderr) { $logContent += "`n--- stderr ---`n$stderr`n" }
[System.IO.File]::WriteAllText($LogFile, $logContent, [System.Text.Encoding]::UTF8)

if ($exitCode -ne 0) {
    Write-Host "编译失败! Exit Code: $exitCode" -ForegroundColor Red
    Write-Host $stderr
    exit 1
}

# ==================== 解压 flat.zip ====================
# 把 flat.zip 解压到 flat/ 目录，得到单独的 .flat 文件
# 每个 XML 对应一个 .flat，如 layout_activity_main.xml.flat
# .flat 是 protobuf 二进制格式，人无法直接阅读
Write-Host "解压 flat.zip..." -ForegroundColor Yellow
Expand-Archive $OutZip -DestinationPath $FlatDir -Force

# ==================== 统计输出 ====================
# 获取所有 .flat 文件列表，本项目约 94 个
$flatFiles = Get-ChildItem $FlatDir -Filter *.flat

Write-Host ""
Write-Host "========== 编译完成 ==========" -ForegroundColor Green

# 打印三个产物的路径和大小
Write-Host "flat.zip:        $OutZip ($((Get-Item $OutZip).Length) bytes)"
Write-Host ".flat 文件数:    $($flatFiles.Count)"
Write-Host "R.txt:           $OutRtxt ($((Get-Item $OutRtxt).Length) bytes)"
Write-Host "日志:            $LogFile"
Write-Host ""

# 按资源类型分组统计
# 用文件名的第一段分组：layout_activity_main → layout
# 展示：layout 7个、drawable 10个、values 5个等
Write-Host "--- 按类型分类 ---" -ForegroundColor DarkGray
$flatFiles | Group-Object { ($_.BaseName -split '_')[0] } | Sort-Object Name | ForEach-Object {
    Write-Host "  $($_.Name): $($_.Count) 个"
}
Write-Host ""

# 预览 R.txt 前 10 行
# 展示什么：int layout activity_main 0x0 — ID 是 0x0，证明编译阶段没有分配资源 ID
# ID 在 link 阶段才分配（0x7f...）
Write-Host "--- R.txt 前 10 行 (ID 全为 0x0 = 编译阶段未分配) ---" -ForegroundColor DarkGray
$rtxtLines = Get-Content $OutRtxt -Encoding UTF8 -TotalCount 10
foreach ($line in $rtxtLines) { Write-Host "  $line" -ForegroundColor DarkGray }
