<#
.SYNOPSIS
    AAPT2 完整流程主控脚本：编译 → Protobuf解码 → 链接 → APC Dump → 链接 Dump
.DESCRIPTION
    按顺序运行全部 5 个阶段脚本：
      1. compile   - 编译 res/ → .flat
      2. proto     - .flat → protobuf 文本
      3. link      - .flat → .ap_ + R.jar
      4. dump_apc  - dump .flat APC 容器
      5. dump_link - dump .ap_ 二进制 XML + 资源表
    每个脚本独立运行，也可单独使用。
.NOTES
    用法:
      .\0_run_all.ps1          # 运行全部 5 个阶段
      .\0_run_all.ps1 1 3 5    # 只运行第 1、3、5 阶段
#>

# 定义脚本参数：接收一个整数数组，默认值 @(1,2,3,4,5) 表示运行全部5个阶段
# 允许用户选择性运行：.\0_run_all.ps1 1 3 只跑编译和链接
param([int[]]$Stages = @(1, 2, 3, 4, 5))

# 获取当前脚本所在目录
# 为什么用 Split-Path：不管你在哪个目录执行，子脚本路径都能正确解析
# 如果用相对路径，cd 到别的目录会找不到子脚本
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 用哈希表定义5个阶段的名称和脚本文件名
# 为什么用哈希表：集中管理映射关系，添加新阶段只需加一行
$Scripts = @{
    1 = @{ Name = "编译 (compile)";        File = "1_compile.ps1" }
    2 = @{ Name = "Protobuf 解码 (proto)";  File = "2_proto_decode.ps1" }
    3 = @{ Name = "链接 (link)";            File = "3_link.ps1" }
    4 = @{ Name = "APC Dump (编译产物)";    File = "4_dump_apc.ps1" }
    5 = @{ Name = "链接 Dump (二进制XML)";  File = "5_dump_link.ps1" }
}

# 打印标题和将要运行的阶段编号
# 让用户确认将要运行哪些阶段
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  AAPT2 完整流程" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "运行阶段: $($Stages -join ', ')"
Write-Host ""

# 记录开始时间，用于最后计算总耗时
$startTime = Get-Date

# 遍历用户指定的阶段编号
foreach ($stage in $Stages) {
    # 如果编号不在哈希表中（比如6），跳过
    # 为什么：容错处理，不因无效输入崩溃
    if (-not $Scripts.ContainsKey($stage)) {
        Write-Host "跳过未知阶段: $stage" -ForegroundColor Yellow
        continue
    }

    # 获取当前阶段的配置（名称和文件名）
    $script = $Scripts[$stage]

    # 拼接子脚本的完整路径
    # 如：...\aapt2_full_workflow\1_compile.ps1
    $scriptPath = "$ScriptDir\$($script.File)"

    # 打印当前阶段标题，带分隔线
    # 视觉上区分不同阶段的输出
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  阶段 $stage : $($script.Name)" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    # 检查子脚本文件是否存在
    # 如果脚本被删除或重命名，不会崩溃，而是跳过
    if (-not (Test-Path $scriptPath)) {
        Write-Host "错误: 找不到脚本 $scriptPath" -ForegroundColor Red
        continue
    }

    # 记录单阶段开始时间
    $stageStart = Get-Date

    # & 调用子脚本（实际执行阶段脚本）
    # & 是 PowerShell 的调用运算符，用于执行脚本文件
    & $scriptPath

    # 记录单阶段结束时间
    $stageEnd = Get-Date

    # 计算耗时秒数
    $duration = ($stageEnd - $stageStart).TotalSeconds

    # 打印单阶段耗时，四舍五入到1位小数
    # 知道每个阶段花多久，便于性能分析
    # 如编译可能2秒，dump可能30秒
    Write-Host ""
    Write-Host "阶段 $stage 耗时: $([math]::Round($duration, 1)) 秒" -ForegroundColor Green
}

# 计算总耗时
$endTime = Get-Date
$totalDuration = ($endTime - $startTime).TotalSeconds

# 打印完成信息和总耗时
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  全部完成！总耗时: $([math]::Round($totalDuration, 1)) 秒" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 打印最终输出目录结构树
# 让用户一眼看到所有输出在哪里、每个目录是什么
# 这是整个 AAPT2 流程的最终产物地图
Write-Host ""
Write-Host "输出目录结构:"
Write-Host "  aapt2_full_workflow\"
Write-Host "  ├── 1_compile_out\          编译产物 (.flat + R.txt)"
Write-Host "  ├── 2_proto_text_out\        Protobuf 解码文本"
Write-Host "  ├── 3_link_out\             链接产物 (linked.ap_)"
Write-Host "  ├── 4_symbols_out\           R.jar / R.txt"
Write-Host "  └── 5_dump_out\              Dump 输出"
Write-Host "      ├── all_apc\            APC dump (编译阶段)"
Write-Host "      ├── all_xmltree\         xmltree dump (链接阶段)"
Write-Host "      ├── all_xmlstrings\      xmlstrings dump (链接阶段)"
Write-Host "      ├── link_resources.txt   完整资源表 + ID"
Write-Host "      ├── link_badging.txt     Manifest 信息"
Write-Host "      └── ap_file_list.txt     .ap_ 文件列表"
