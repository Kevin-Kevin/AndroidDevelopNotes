1. Drawables
2. String
3. Styles



### style

style的使用是设置布局文件的xml的style属性

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="CodeFont" >
        <item name="android:layout_width">fill_parent</item>
        <item name="android:layout_height">wrap_content</item>
        <item name="android:textColor">#00FF00</item>
        <item name="android:typeface">monospace</item>
    </style>
</resources>
```

```xml
<TextView
	style="@style/CodeFont"
	android:text="Hello World!"
 />
```



style的子节点是view属性值

view只能应用一个style, 其子view不会继承这个style

style可以继承其他style

```xml
<style name="SettingFragmentSwitch" parent="TextAppearance.AppCompat.Widget.Switch">
    <item name="trackColor">@color/ios_switch_close_grey</item>
</style>
```



### theme

在`AndroidManifest.xml` 文件中的 `<application>` 标记或 `<activity>` 标记应用具有 `android:theme` 属性的主题背景

可以设置应用到application还是activity

>  从 Android 5.0（API 级别 21）和 Android 支持库 v22.1 开始，您还可以在布局文件中为视图指定 `android:theme` 属性

主题是命名资源的集合



### 设置样式的多种方法

直接在布局中设置属性

将样式应用到视图

将主题背景应用到布局

以编程方式设置属性

#### 设置样式的优先级

该列表按照优先级从高到低的顺序排序：

1. 通过文本 span 将字符或段落级样式应用到 `TextView` 派生的类
2. 以编程方式应用属性
3. 将单独的属性直接应用到 View
4. 将样式应用到 View
5. 默认样式
6. 将主题背景应用到 View 集合、Activity 或整个应用
7. 应用某些特定于 View 的样式，例如为 `TextView` 设置 `TextAppearance`

