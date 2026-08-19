<#
.SYNOPSIS
    AAPT2 Protobuf 解码：将所有 .flat 文件解码为可读的 protobuf 文本
.DESCRIPTION
    输入: 1_compile_out/flat/ 下的所有 .flat 文件
    输出: 2_proto_text_out/ 目录下的 _proto.txt 文件
    工具: decode_flat_proto.py (Python 脚本)
.NOTES
    .flat 文件是 APC (AAPT2 Protobuf Container) 格式
    内含两个 protobuf 消息: ResFile header + PROTO_XML data
    此脚本用 Python 解析 protobuf wire format，输出可读文本
    需要先运行 1_compile.ps1 生成 .flat 文件
#>

# ==================== 配置区 ====================
# 项目根目录
$ProjectRoot = "C:\Users\win\AndroidStudioProjects\CurrentLearnLayuot"

# 输入目录：编译阶段解压出的 .flat 文件
# 依赖 1_compile.ps1 的产物
$FlatDir = "$ProjectRoot\aapt2_full_workflow\1_compile_out\flat"

# 导出目录：编号 2_ 表示第二阶段产物
# 每个 .flat 解码为一个 _proto.txt 文件
$OutDir = "$ProjectRoot\aapt2_full_workflow\2_proto_text_out"

# Python 解码脚本路径
# 这个脚本解析 protobuf wire format（varint、length-delimited 等）
# 为什么用 Python：aapt2 没有提供"解码 protobuf 为文本"的命令，只能自己写
$DecodeScript = "$ProjectRoot\aapt2_full_workflow\decode_flat_proto.py"

# 导出文件：所有 .flat 的解码结果合并到一个文件
# 为什么合并：方便整体搜索和对比
$CombinedFile = "$OutDir\all_proto_combined.txt"

# ==================== 依赖检查 ====================

# 检查 .flat 文件目录是否存在
# 如果不存在，说明编译阶段还没运行
if (-not (Test-Path $FlatDir)) {
    Write-Host "错误: 找不到 .flat 文件目录，请先运行 1_compile.ps1" -ForegroundColor Red
    exit 1
}

# 检查 Python 解码脚本是否存在
if (-not (Test-Path $DecodeScript)) {
    Write-Host "错误: 找不到 decode_flat_proto.py" -ForegroundColor Red
    exit 1
}

# 创建输出目录（如果不存在）
if (-not (Test-Path $OutDir)) { New-Item -ItemType Directory -Force -Path $OutDir | Out-Null }

# 获取所有 .flat 文件列表
$flatFiles = Get-ChildItem $FlatDir -Filter *.flat

# 打印阶段标题
Write-Host "========== AAPT2 Protobuf 解码 ==========" -ForegroundColor Cyan
Write-Host "输入: $FlatDir ($($flatFiles.Count) 个 .flat 文件)"
Write-Host "输出: $OutDir"
Write-Host ""

# ==================== 解码循环 ====================

# 初始化合并字符串和计数器
$combined = ""
$count = 0
$success = 0

# 记录解码失败的文件名
$failed = @()

# 遍历每个 .flat 文件
foreach ($f in $flatFiles) {
    $count++

    # 拼接输出文件名：layout_activity_main.xml.flat → layout_activity_main.xml_proto.txt
    $outFile = "$OutDir\$($f.BaseName)_proto.txt"

    # 调用 Python 脚本解码：
    #   第一个参数：输入文件路径（.flat 文件）
    #   第二个参数：输出文件路径（_proto.txt）
    #   2>&1：合并 stderr 到 stdout（捕获 Python 的所有输出）
    # 为什么用 Python：aapt2 没有 protobuf 文本解码命令
    # protobuf 库需要 .proto 定义文件，这里用 wire format 手动解析
    $result = & python $DecodeScript $f.FullName $outFile 2>&1

    # 检查 Python 脚本是否成功生成了输出文件
    if (Test-Path $outFile) {
        # 用 .NET ReadAllText 以 UTF-8 读取解码结果
        # 为什么用 .NET：PowerShell Get-Content 可能编码不正确
        $content = [System.IO.File]::ReadAllText($outFile, [System.Text.Encoding]::UTF8)

        # 拼接到合并字符串，带文件名分隔标记
        $combined += "===== $($f.Name) =====`n$content`n`n"
        $success++
    } else {
        # Python 解码失败，记录文件名
        $failed += $f.Name
    }

    # 每 20 个文件打印一次进度
    # 为什么：94 个文件需要几秒，让用户知道没卡住
    if ($count % 20 -eq 0) {
        Write-Host "  进度: $count / $($flatFiles.Count)" -ForegroundColor DarkGray
    }
}

# ==================== 保存合并文件 ====================
# 用 .NET WriteAllText 写入 UTF-8 合并文件
# 包含所有 94 个 .flat 的解码结果
# 展示什么：field 3 (string): "androidx.coordinatorlayout.widget.CoordinatorLayout"
# 这种 protobuf 字段结构，证明标签名在编译阶段是明文字符串（还没变成 StringPool 索引）
[System.IO.File]::WriteAllText($CombinedFile, $combined, [System.Text.Encoding]::UTF8)

# ==================== 统计输出 ====================
Write-Host ""
Write-Host "========== 解码完成 ==========" -ForegroundColor Green

# 打印成功/失败统计
Write-Host "成功: $success / $($flatFiles.Count)"

# 如果有失败的文件，逐个列出
if ($failed.Count -gt 0) {
    Write-Host "失败: $($failed.Count) 个" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host "  - $_" -ForegroundColor DarkRed }
}

# 打印合并文件大小
Write-Host "合并文件: $CombinedFile ($((Get-Item $CombinedFile).Length) bytes)"
Write-Host "单独文件: $OutDir\*_proto.txt"
Write-Host ""

# 取第一个解码文件的前 20 行预览
# 展示什么：让用户立刻看到解码效果，不用手动去翻文件
$sample = Get-ChildItem $OutDir -Filter *_proto.txt | Select-Object -First 1
if ($sample) {
    Write-Host "--- 示例: $($sample.Name) 前 20 行 ---" -ForegroundColor DarkGray
    Get-Content $sample.FullName -Encoding UTF8 -TotalCount 20 | ForEach-Object {
        Write-Host "  $_" -ForegroundColor DarkGray
    }
}
