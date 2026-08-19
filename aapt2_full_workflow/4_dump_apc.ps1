<#
.SYNOPSIS
    AAPT2 编译阶段 Dump：dump 所有 .flat 文件的 APC (AAPT2 Protobuf Container) 内容
.DESCRIPTION
    输入: 1_compile_out/flat/ 下的所有 .flat 文件
    输出: 5_dump_out/all_apc/ 下的 .txt 文件 + all_apc_combined.txt
    命令: aapt2 dump apc <file.flat>
.NOTES
    APC dump 显示 .flat 文件的容器元数据：
    - 依赖列表 (dependencies)
    - 来源路径 (source path)
    - 资源配置 (configuration)
    不包含 XML 内容本身，XML 内容用 2_proto_decode.ps1 解码
    需要先运行 1_compile.ps1 生成 .flat 文件
#>

# ==================== 配置区 ====================
# 项目根目录
$ProjectRoot = "C:\Users\win\AndroidStudioProjects\CurrentLearnLayuot"

# AAPT2 可执行文件路径
$Aapt2 = "D:\develop\Android\SDK\build-tools\37.0.0\aapt2.exe"

# 输入目录：编译阶段解压出的 .flat 文件
# 依赖 1_compile.ps1 的产物
$FlatDir = "$ProjectRoot\aapt2_full_workflow\1_compile_out\flat"

# 导出目录1：每个 .flat dump 为一个 .txt 文件
# 路径：5_dump_out/all_apc/
$OutDir = "$ProjectRoot\aapt2_full_workflow\5_dump_out\all_apc"

# 导出文件2：所有 APC dump 合并到一个文件
# 为什么合并：方便全局搜索，比如查所有 layout 类型文件
$CombinedFile = "$ProjectRoot\aapt2_full_workflow\5_dump_out\all_apc_combined.txt"

# ==================== 依赖检查 ====================

# 检查 .flat 文件目录是否存在
# 如果不存在，说明编译阶段还没运行
if (-not (Test-Path $FlatDir)) {
    Write-Host "错误: 找不到 .flat 文件目录，请先运行 1_compile.ps1" -ForegroundColor Red
    exit 1
}

# 创建输出目录（如果不存在）
if (-not (Test-Path $OutDir)) { New-Item -ItemType Directory -Force -Path $OutDir | Out-Null }

# 获取所有 .flat 文件列表
$flatFiles = Get-ChildItem $FlatDir -Filter *.flat

# 打印阶段标题
Write-Host "========== AAPT2 APC Dump (编译阶段产物) ==========" -ForegroundColor Cyan
Write-Host "输入: $FlatDir ($($flatFiles.Count) 个 .flat 文件)"
Write-Host "输出: $OutDir"
Write-Host ""

# ==================== 公共函数：用 .NET Process 调用 aapt2 ====================
# 这个函数是整个脚本防乱码的核心
# 为什么封装成函数：要对 94 个 .flat 文件逐个调用 aapt2，封装成函数避免重复代码
# 为什么用 .NET Process 不用 & 调用：
#   PowerShell 的 & $aapt2 dump apc "file.flat" | Out-File 会产生中文乱码（GBK 编码）
#   .NET Process 可以显式指定 UTF-8 编码
function Invoke-Aapt2 {
    param([string]$ArgStr)

    $tempOut = [System.IO.Path]::GetTempFileName()
    $tempErr = [System.IO.Path]::GetTempFileName()
    $proc = Start-Process -FilePath $Aapt2 -ArgumentList $ArgStr `
        -NoNewWindow -Wait -PassThru `
        -RedirectStandardOutput $tempOut -RedirectStandardError $tempErr
    $stdout = [System.IO.File]::ReadAllText($tempOut, [System.Text.Encoding]::UTF8)
    $stderr = [System.IO.File]::ReadAllText($tempErr, [System.Text.Encoding]::UTF8)
    Remove-Item $tempOut, $tempErr -Force
    if ($stderr -and $stderr.Length -gt 0) { return "$stdout`n$stderr" }
    return $stdout
}

# ==================== 批量 APC Dump ====================

# 初始化合并字符串和计数器
$combined = ""
$count = 0
$success = 0

# 遍历每个 .flat 文件
foreach ($f in $flatFiles) {
    $count++

    # 调用 Invoke-Aapt2 函数执行 aapt2 dump apc
    # 命令：aapt2 dump apc "文件路径.flat"
    # 展示什么：APC 容器元数据
    #   Type: xml
    #   Source: res/layout/activity_main.xml
    #   Config: (default)
    # 这是 .flat 文件的容器元数据，不是 XML 内容
    $result = Invoke-Aapt2 "dump apc `"$($f.FullName)`""

    # 替换扩展名：.flat → .txt
    $outName = $f.Name -replace '\.flat$', '.txt'

    # 用 .NET WriteAllText 写入单独文件（UTF-8 编码）
    # 为什么用 WriteAllText：PowerShell Out-File 默认 UTF-16 BOM
    [System.IO.File]::WriteAllText("$OutDir\$outName", $result, [System.Text.Encoding]::UTF8)

    # 拼接到合并字符串，带文件名分隔标记
    $combined += "===== $($f.Name) =====`n$result`n`n"
    $success++

    # 每 20 个文件打印一次进度
    if ($count % 20 -eq 0) {
        Write-Host "  进度: $count / $($flatFiles.Count)" -ForegroundColor DarkGray
    }
}

# ==================== 保存合并文件 ====================
# 用 .NET WriteAllText 写入 UTF-8 合并文件
# 包含所有 94 个 .flat 的 APC dump
# 方便全局搜索
[System.IO.File]::WriteAllText($CombinedFile, $combined, [System.Text.Encoding]::UTF8)

# ==================== 统计输出 ====================
Write-Host ""
Write-Host "========== APC Dump 完成 ==========" -ForegroundColor Green

# 打印成功统计
Write-Host "成功: $success / $($flatFiles.Count)"

# 打印输出文件路径和大小
Write-Host "单独文件: $OutDir\*.txt"
Write-Host "合并文件: $CombinedFile ($((Get-Item $CombinedFile).Length) bytes)"
Write-Host ""

# 取第一个 APC dump 文件预览
# 展示什么：让用户看到 APC dump 的格式，不用手动打开文件
$sample = Get-ChildItem $OutDir -Filter *.txt | Select-Object -First 1
if ($sample) {
    Write-Host "--- 示例: $($sample.Name) ---" -ForegroundColor DarkGray
    Get-Content $sample.FullName -Encoding UTF8 | ForEach-Object {
        Write-Host "  $_" -ForegroundColor DarkGray
    }
}
