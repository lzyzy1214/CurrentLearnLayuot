<#
.SYNOPSIS
    AAPT2 链接阶段 Dump：dump .ap_ 中的所有二进制 XML + 完整资源表 + Manifest
.DESCRIPTION
    输入: 3_link_out/linked.ap_ (链接阶段产物)
    输出: 5_dump_out/ 下的:
      - all_xmltree/           每个 XML 文件的二进制 XML 树 dump
      - all_xmlstrings/        每个 XML 文件的字符串池 dump
      - link_resources.txt     完整资源表 (resources.arsc 内容)
      - link_badging.txt        Manifest 信息
      - ap_file_list.txt        .ap_ 内全部文件列表
    命令:
      aapt2 dump xmltree --file <path> <ap_>
      aapt2 dump xmlstrings --file <path> <ap_>
      aapt2 dump resources <ap_>
      aapt2 dump badging <ap_>
.NOTES
    这是整个 AAPT2 流程中最核心的 dump:
    - xmltree 显示标签名/属性名→StringPool 索引、属性值→整数/引用
    - xmlstrings 显示 StringPool 中的所有字符串
    - resources 显示所有资源 ID 分配结果
    需要先运行 3_link.ps1 生成 linked.ap_
#>

# ==================== 配置区 ====================
# 项目根目录
$ProjectRoot = "C:\Users\win\AndroidStudioProjects\CurrentLearnLayuot"

# AAPT2 可执行文件路径
$Aapt2 = "D:\develop\Android\SDK\build-tools\37.0.0\aapt2.exe"

# 输入文件：链接阶段的最终产物 linked.ap_
# 依赖 3_link.ps1
$LinkedAp = "$ProjectRoot\aapt2_full_workflow\3_link_out\linked.ap_"

# 编译阶段的 .flat 文件目录
# 用于构建文件名映射：layout_activity_main.xml.flat → res/layout/activity_main.xml
$FlatDir = "$ProjectRoot\aapt2_full_workflow\1_compile_out\flat"

# 输出根目录：所有 dump 结果存放位置
$DumpDir = "$ProjectRoot\aapt2_full_workflow\5_dump_out"

# 导出目录1：每个 XML 的 xmltree dump
# 展示：标签名(明文)、属性名(带资源ID)、属性值(整数/引用)
$XmltreeDir = "$DumpDir\all_xmltree"

# 导出目录2：每个 XML 的 xmlstrings dump
# 展示：String #4 : layout_width / String #11 : CoordinatorLayout
# 这就是字符串池！标签名和属性名都变成了索引号
$XmlstrDir = "$DumpDir\all_xmlstrings"

# 导出文件3：完整资源表 + ID 分配结果
# 展示：int layout activity_main 0x7f0b001f（对比编译阶段的 0x0）
$ResourcesFile = "$DumpDir\link_resources.txt"

# 导出文件4：Manifest 信息（包名、权限、启动 Activity）
$BadgingFile = "$DumpDir\link_badging.txt"

# 导出文件5：.ap_ 内全部文件列表（约1000个文件）
$ApListFile = "$DumpDir\ap_file_list.txt"

# ==================== 依赖检查 ====================

# 检查 linked.ap_ 是否存在（链接阶段产物）
if (-not (Test-Path $LinkedAp)) {
    Write-Host "错误: 找不到 linked.ap_，请先运行 3_link.ps1" -ForegroundColor Red
    exit 1
}

# 创建 xmltree 和 xmlstrings 子目录
foreach ($d in @($XmltreeDir, $XmlstrDir)) {
    if (-not (Test-Path $d)) { New-Item -ItemType Directory -Force -Path $d | Out-Null }
}

# 打印阶段标题
Write-Host "========== AAPT2 链接阶段 Dump ==========" -ForegroundColor Cyan
Write-Host "输入: $LinkedAp"
Write-Host "输出: $DumpDir"
Write-Host ""

# ==================== 公共函数：用 .NET Process 调用 aapt2 ====================
# 同 4_dump_apc.ps1 的函数
# 为什么封装：要对 79 个 XML 文件逐个调用 aapt2 dump，封装避免重复代码
# 为什么用 .NET Process：PowerShell 管道会产生中文乱码(GBK)
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

# ==================== 1. 提取 .ap_ 中的项目 XML 文件列表 ====================
# 这是最关键的逻辑：从 .flat 文件名构建 .ap_ 中的路径
# 因为 aapt2 dump xmltree 需要指定 --file "res/layout/activity_main.xml"
# 但 .flat 文件名是 layout_activity_main.xml.flat，需要转换
Write-Host "=== 1. 提取项目 XML 文件列表 ===" -ForegroundColor Yellow

# 加载 .NET 压缩库（用于读取 zip 格式的 .ap_）
Add-Type -AssemblyName System.IO.Compression.FileSystem

# 以只读方式打开 linked.ap_（它本质上是一个 zip 文件）
$zip = [System.IO.Compression.ZipFile]::OpenRead($LinkedAp)

# 从 .flat 文件名构建 .ap_ 中的路径
# 转换规则：layout_activity_main.xml.flat → res/layout/activity_main.xml
# 1. 去掉 .flat → layout_activity_main.xml
# 2. 找到第一个 _ → 分割为 type=layout + name=activity_main.xml
# 3. 拼接 → res/layout/activity_main.xml
# 为什么这样：AAPT2 编译时用下划线替代路径分隔符
$projectXmlFiles = @()
if (Test-Path $FlatDir) {
    $flatFiles = Get-ChildItem $FlatDir -Filter *.flat
    foreach ($f in $flatFiles) {
        # 去掉 .xml.flat 后缀，得到 layout_activity_main
        $name = $f.BaseName -replace '\.xml$', ''
        # 找到第一个下划线的位置
        $idx = $name.IndexOf('_')
        if ($idx -gt 0) {
            # 下划线前是类型目录名：layout
            $typeDir = $name.Substring(0, $idx)
            # 下划线后是文件名：activity_main.xml
            $fileName = $name.Substring($idx + 1) + '.xml'
            # 拼接为 .ap_ 内的路径：res/layout/activity_main.xml
            $apPath = "res/$typeDir/$fileName"
            # 检查这个路径是否存在于 .ap_ 中
            $entry = $zip.Entries | Where-Object { $_.FullName -eq $apPath } | Select-Object -First 1
            if ($entry) {
                # 存在则加入列表
                $projectXmlFiles += [PSCustomObject]@{ FlatName = $f.Name; ApPath = $apPath }
            }
        }
    }
}

# 释放 zip 资源
$zip.Dispose()
Write-Host "  项目 XML 文件: $($projectXmlFiles.Count) 个"

# ==================== 2. 批量 xmltree dump ====================
# 对每个 XML 文件执行 aapt2 dump xmltree
# 命令：aapt2 dump xmltree --file "res/layout/activity_main.xml" "linked.ap_"
# 展示什么：
#   E: androidx.coordinatorlayout.widget.CoordinatorLayout (line=9)
#     A: ...:layout_width(0x010100f4)=-1
# — 标签名是明文（aapt2 dump 自动还原了索引）
# — 属性名带资源 ID（0x010100f4 = 框架属性）
# — 属性值已编码为整数（-1 = match_parent）
Write-Host ""
Write-Host "=== 2. 批量 xmltree dump ===" -ForegroundColor Yellow

# 初始化合并字符串
$xmltreeAll = ""
$count = 0

# 遍历每个项目 XML 文件
foreach ($xf in $projectXmlFiles) {
    $count++

    # 调用 aapt2 dump xmltree
    # --file 指定要 dump 的 XML 文件路径（在 .ap_ 内的路径）
    $result = Invoke-Aapt2 "dump xmltree --file `"$($xf.ApPath)`" `"$LinkedAp`""

    # 拼接到合并字符串，带文件路径标记
    $xmltreeAll += "===== $($xf.ApPath) (from $($xf.FlatName)) =====`n$result`n`n"

    # 替换扩展名：.flat → _xmltree.txt
    $outName = $xf.FlatName -replace '\.flat$', '_xmltree.txt'

    # 用 .NET WriteAllText 写入单独文件（UTF-8）
    [System.IO.File]::WriteAllText("$XmltreeDir\$outName", $result, [System.Text.Encoding]::UTF8)

    # 每 20 个文件打印进度
    if ($count % 20 -eq 0) { Write-Host "  xmltree: $count / $($projectXmlFiles.Count)" -ForegroundColor DarkGray }
}

# 保存合并文件（所有 xmltree dump 在一个文件里）
[System.IO.File]::WriteAllText("$DumpDir\all_xmltree_combined.txt", $xmltreeAll, [System.Text.Encoding]::UTF8)
Write-Host "  完成: $count 个文件" -ForegroundColor Green

# ==================== 3. 批量 xmlstrings dump ====================
# 对每个 XML 文件执行 aapt2 dump xmlstrings
# 命令：aapt2 dump xmlstrings --file "res/layout/activity_main.xml" "linked.ap_"
# 展示什么：
#   String #4 : layout_width
#   String #9 : Android 布局学习
#   String #11: ...CoordinatorLayout
# 这就是字符串池！标签名和属性名都变成了索引号
# 这是最核心的证据：对比 proto 解码（明文）和 xmlstrings（索引）
# 证明 link 阶段把字符串收进了 StringPool
Write-Host ""
Write-Host "=== 3. 批量 xmlstrings dump ===" -ForegroundColor Yellow

# 初始化合并字符串
$xmlstrAll = ""
$count = 0

# 遍历每个项目 XML 文件
foreach ($xf in $projectXmlFiles) {
    $count++

    # 调用 aapt2 dump xmlstrings
    $result = Invoke-Aapt2 "dump xmlstrings --file `"$($xf.ApPath)`" `"$LinkedAp`""

    # 拼接到合并字符串
    $xmlstrAll += "===== $($xf.ApPath) =====`n$result`n`n"

    # 替换扩展名：.flat → _xmlstrings.txt
    $outName = $xf.FlatName -replace '\.flat$', '_xmlstrings.txt'

    # 用 .NET WriteAllText 写入单独文件（UTF-8）
    [System.IO.File]::WriteAllText("$XmlstrDir\$outName", $result, [System.Text.Encoding]::UTF8)

    # 每 20 个文件打印进度
    if ($count % 20 -eq 0) { Write-Host "  xmlstrings: $count / $($projectXmlFiles.Count)" -ForegroundColor DarkGray }
}

# 保存合并文件
[System.IO.File]::WriteAllText("$DumpDir\all_xmlstrings_combined.txt", $xmlstrAll, [System.Text.Encoding]::UTF8)
Write-Host "  完成: $count 个文件" -ForegroundColor Green

# ==================== 4. resources dump（完整资源表 + ID） ====================
# 命令：aapt2 dump resources "linked.ap_"
# 展示什么：完整资源表，显示每个资源的最终 ID
#   int layout activity_main 0x7f0b001f
# 对比编译阶段 R.txt 的 0x0，证明 ID 在 link 阶段被分配
# 这个文件通常很大（1MB+），包含所有资源的 ID 映射
Write-Host ""
Write-Host "=== 4. resources dump (完整资源表 + ID) ===" -ForegroundColor Yellow

# 调用 aapt2 dump resources（不需要 --file，dump 整个资源表）
$resResult = Invoke-Aapt2 "dump resources `"$LinkedAp`""

# 用 .NET WriteAllText 写入 UTF-8 文件
[System.IO.File]::WriteAllText($ResourcesFile, $resResult, [System.Text.Encoding]::UTF8)
Write-Host "  完成: $ResourcesFile ($((Get-Item $ResourcesFile).Length) bytes)" -ForegroundColor Green

# ==================== 5. badging dump（Manifest 信息） ====================
# 命令：aapt2 dump badging "linked.ap_"
# 展示什么：Manifest 信息——包名、启动 Activity、权限、SDK 版本
#   package: name='com.example.currentlearnlayuot'
#   launchable-activity: name='com.example.currentlearnlayuot.MainActivity'
Write-Host ""
Write-Host "=== 5. badging dump (Manifest 信息) ===" -ForegroundColor Yellow

# 调用 aapt2 dump badging
$badResult = Invoke-Aapt2 "dump badging `"$LinkedAp`""

# 用 .NET WriteAllText 写入 UTF-8 文件
[System.IO.File]::WriteAllText($BadgingFile, $badResult, [System.Text.Encoding]::UTF8)
Write-Host "  完成: $BadgingFile ($((Get-Item $BadgingFile).Length) bytes)" -ForegroundColor Green

# ==================== 6. .ap_ 文件列表 ====================
# 列出 .ap_ 内所有文件，保存到 txt
# 展示什么：res/layout/activity_main.xml / resources.arsc / AndroidManifest.xml
# 证明 .ap_ 本质上是一个 zip，包含编译后的所有资源
# 本项目约 1000 个文件
Write-Host ""
Write-Host "=== 6. .ap_ 文件列表 ===" -ForegroundColor Yellow

# 以只读方式打开 .ap_（zip 格式）
$zip2 = [System.IO.Compression.ZipFile]::OpenRead($LinkedAp)

# 提取所有条目的完整路径，排序
$apList = $zip2.Entries | ForEach-Object { $_.FullName } | Sort-Object

# 释放 zip 资源
$zip2.Dispose()

# 用 .NET WriteAllLines 写入 UTF-8 文件（每行一个文件路径）
[System.IO.File]::WriteAllLines($ApListFile, $apList, [System.Text.Encoding]::UTF8)
Write-Host "  完成: $ApListFile ($($apList.Count) 个文件)" -ForegroundColor Green

# ==================== 汇总 ====================
# 打印所有 dump 结果的统计
Write-Host ""
Write-Host "========== 全部 Dump 完成 ==========" -ForegroundColor Green

# 统计各目录文件数
Write-Host "xmltree:         $((Get-ChildItem $XmltreeDir -Filter *.txt).Count) 个文件"
Write-Host "xmlstrings:      $((Get-ChildItem $XmlstrDir -Filter *.txt).Count) 个文件"
Write-Host "resources:       $((Get-Item $ResourcesFile).Length) bytes"
Write-Host "badging:         $((Get-Item $BadgingFile).Length) bytes"
Write-Host ".ap_ 文件列表:   $($apList.Count) 个"
Write-Host ""

# 预览 activity_main.xml 的 xmltree（前 15 行）
# 展示什么：让用户直接在终端看到二进制 XML 的格式
# 包括标签名、属性资源 ID、属性值编码
# 这是整个 AAPT2 流程的最终验证——人写的 XML 变成了什么
Write-Host "--- activity_main.xml xmltree 预览 (前 15 行) ---" -ForegroundColor DarkGray
$sample = Get-ChildItem $XmltreeDir -Filter *activity_main* | Select-Object -First 1
if ($sample) {
    Get-Content $sample.FullName -Encoding UTF8 -TotalCount 15 | ForEach-Object {
        Write-Host "  $_" -ForegroundColor DarkGray
    }
}
