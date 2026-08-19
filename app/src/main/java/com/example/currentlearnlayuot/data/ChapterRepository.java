package com.example.currentlearnlayuot.data;

import com.example.currentlearnlayuot.R;

import java.util.Arrays;
import java.util.List;

/**
 * 章节数据仓库，提供全部 11 章的学习内容和示例布局
 */
public class ChapterRepository {

    public static List<Chapter> getChapters() {
        return Arrays.asList(
                new Chapter(0,
                        "布局系统概述",
                        "View Tree、XML 布局、measure/layout/draw 渲染流水线",
                        "⭐ 入门",
                        Arrays.asList(
                                new ExampleItem("View Tree 层级",
                                        "展示典型 View 树结构，理解父 View 与子 View 的层级关系",
                                        R.layout.example_00_view_tree),
                                new ExampleItem("自定义绘制",
                                        "演示自定义 View 的 onDraw 绘制流程",
                                        R.layout.example_00_custom_draw)
                        )),
                new Chapter(1,
                        "LinearLayout",
                        "orientation、layout_weight、gravity、visibility",
                        "⭐ 入门",
                        Arrays.asList(
                                new ExampleItem("垂直列表",
                                        "orientation=vertical，子 View 从上到下排列",
                                        R.layout.example_01_vertical),
                                new ExampleItem("水平列表",
                                        "orientation=horizontal，子 View 从左到右排列",
                                        R.layout.example_01_horizontal),
                                new ExampleItem("weight 分配",
                                        "layout_weight 按比例分配剩余空间",
                                        R.layout.example_01_weight),
                                new ExampleItem("gravity 对齐",
                                        "演示多种 gravity 对齐方式",
                                        R.layout.example_01_gravity)
                        )),
                new Chapter(2,
                        "RelativeLayout",
                        "相对父容器与相对兄弟 View 定位",
                        "⭐⭐ 中等",
                        Arrays.asList(
                                new ExampleItem("基础相对定位",
                                        "alignParentTop/Bottom/Start/End 等相对父容器定位",
                                        R.layout.example_02_relative_basic),
                                new ExampleItem("居中与对齐",
                                        "centerInParent、toStartOf、toEndOf 等组合定位",
                                        R.layout.example_02_relative_center)
                        )),
                new Chapter(3,
                        "FrameLayout",
                        "层叠布局、layout_gravity、加载蒙版、帧动画",
                        "⭐ 入门",
                        Arrays.asList(
                                new ExampleItem("多层覆盖",
                                        "子 View 默认从左上角堆叠，配合 gravity 定位",
                                        R.layout.example_03_frame_overlay),
                                new ExampleItem("加载蒙版",
                                        "模拟网络请求时的加载遮罩效果",
                                        R.layout.example_03_frame_loading),
                                new ExampleItem("帧动画",
                                        "使用 AnimationDrawable 实现帧动画",
                                        R.layout.example_03_frame_animation)
                        )),
                new Chapter(4,
                        "ConstraintLayout",
                        "约束布局、Chain、Guideline、Barrier、Group、Placeholder",
                        "⭐⭐⭐ 复杂",
                        Arrays.asList(
                                new ExampleItem("基础约束",
                                        "通过约束确定 View 的位置和大小",
                                        R.layout.example_04_constraint_basic),
                                new ExampleItem("Chain 链",
                                        "spread / spread_inside / packed 三种链式分布",
                                        R.layout.example_04_constraint_chain),
                                new ExampleItem("Guideline 辅助线",
                                        "使用百分比辅助线实现百分比布局",
                                        R.layout.example_04_constraint_guideline),
                                new ExampleItem("Barrier 屏障",
                                        "根据多个 View 动态生成屏障",
                                        R.layout.example_04_constraint_barrier)
                        )),
                new Chapter(5,
                        "ScrollView",
                        "垂直/水平滚动、fillViewport、NestedScrollView、嵌套滑动",
                        "⭐⭐ 进阶",
                        Arrays.asList(
                                new ExampleItem("垂直滚动",
                                        "ScrollView 包裹长内容实现垂直滚动",
                                        R.layout.example_05_scrollview),
                                new ExampleItem("水平滚动",
                                        "HorizontalScrollView 实现横向滚动",
                                        R.layout.example_05_horizontal_scroll),
                                new ExampleItem("嵌套滚动",
                                        "NestedScrollView 与 RecyclerView 嵌套",
                                        R.layout.example_05_nested_scroll)
                        )),
                new Chapter(6,
                        "RecyclerView",
                        "Adapter、ViewHolder、LayoutManager、DiffUtil、ItemTouchHelper",
                        "⭐⭐⭐ 高级",
                        Arrays.asList(
                                new ExampleItem("线性列表",
                                        "LinearLayoutManager 垂直列表",
                                        R.layout.example_06_recycler_linear),
                                new ExampleItem("网格列表",
                                        "GridLayoutManager 网格布局",
                                        R.layout.example_06_recycler_grid),
                                new ExampleItem("瀑布流",
                                        "StaggeredGridLayoutManager 瀑布流效果",
                                        R.layout.example_06_recycler_staggered)
                        )),
                new Chapter(7,
                        "布局优化技巧",
                        "include、merge、ViewStub、扁平化、RecyclerView",
                        "⭐⭐ 进阶",
                        Arrays.asList(
                                new ExampleItem("include / merge",
                                        "复用布局并减少层级",
                                        R.layout.example_07_include_merge),
                                new ExampleItem("ViewStub 延迟加载",
                                        "按需加载不常用的错误提示布局",
                                        R.layout.example_07_viewstub),
                                new ExampleItem("扁平化对比",
                                        "对比嵌套 LinearLayout 与单层 ConstraintLayout",
                                        R.layout.example_07_flatten)
                        )),
                new Chapter(8,
                        "屏幕适配",
                        "dp/sp、资源限定符、ConstraintLayout 百分比、深色模式",
                        "⭐⭐ 进阶",
                        Arrays.asList(
                                new ExampleItem("dp 与 sp",
                                        "正确使用 dp 和 sp 单位",
                                        R.layout.example_08_dp_sp),
                                new ExampleItem("百分比布局",
                                        "使用 Guideline 实现百分比布局",
                                        R.layout.example_08_percent),
                                new ExampleItem("深色模式",
                                        "values-night 资源自动适配深色模式",
                                        R.layout.example_08_night)
                        )),
                new Chapter(9,
                        "Jetpack Compose",
                        "声明式 UI、Column/Row/Box、LazyColumn、State、Modifier、动画（XML 等价实现）",
                        "⭐⭐⭐ 高级",
                        Arrays.asList(
                                new ExampleItem("Column 垂直排列",
                                        "XML 等价：垂直 LinearLayout",
                                        R.layout.example_09_column),
                                new ExampleItem("Row 水平排列",
                                        "XML 等价：水平 LinearLayout",
                                        R.layout.example_09_row),
                                new ExampleItem("Box 堆叠",
                                        "XML 等价：FrameLayout",
                                        R.layout.example_09_box),
                                new ExampleItem("LazyColumn 列表",
                                        "XML 等价：RecyclerView",
                                        R.layout.example_09_recycler),
                                new ExampleItem("State 状态",
                                        "XML 等价：点击按钮计数",
                                        R.layout.example_09_state),
                                new ExampleItem("动画",
                                        "XML 等价：属性动画缩放与变色",
                                        R.layout.example_09_animation)
                        )),
                new Chapter(10,
                        "章节总结",
                        "六大布局对比、性能对比、面试高频问题",
                        "⭐ 入门",
                        Arrays.asList(
                                new ExampleItem("布局对比表",
                                        "LinearLayout / RelativeLayout / FrameLayout / ConstraintLayout / TableLayout / Compose",
                                        R.layout.example_10_compare_table),
                                new ExampleItem("性能等级",
                                        "布局嵌套性能对比可视化",
                                        R.layout.example_10_performance),
                                new ExampleItem("面试 Q&A",
                                        "常见 Android 布局面试问题",
                                        R.layout.example_10_qa)
                        ))
        );
    }
}
