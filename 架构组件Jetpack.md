## android 架构组件Jetpack

Android 架构组件是一组库，可帮助您设计稳健、可测试且易维护的应用

### viewbinding

> 可以不用重复写findviewbyid()了, 只要布局文件里的view有id就可> 以在绑定类中直接操作这个view

模块中启用视图绑定重新编译之后，系统会为该模块中的每个 XML 布局文件生成一个绑定类

绑定类的实例包含对在相应布局中具有 ID 的所有视图的直接引用

生成类的目录在 *模块根目录/build/generated/data_binding_base_class_source_out*下

#### 按模块启用viewbinding

视图绑定功能可按模块启用。要在某个模块中启用视图绑定，请将 `viewBinding` 元素添加到其 `build.gradle` 文件中，如下例所示：

```groovy
android {
    ...
    buildFeatures {
        viewBinding true
    }
}
```

如果您希望在生成绑定类时忽略某个布局文件，请将 `tools:viewBindingIgnore="true"` 属性添加到相应布局文件的根视图中：

```xml
<LinearLayout
            ...
            tools:viewBindingIgnore="true" >
        ...
    </LinearLayout>
    
```

#### 使用viewbinding

为某个模块启用视图绑定功能后，系统会为该模块中包含的每个 XML 布局文件生成一个绑定类

每个绑定类均包含对根视图以及具有 ID 的所有视图的引用

系统会通过以下方式生成绑定类的名称：将 XML 文件的名称转换为驼峰式大小写，并在末尾添加“Binding”一词。

> 例如，假设某个布局文件的名称为 `result_profile.xml`：
>
> 所生成的绑定类的名称就为 `ResultProfileBinding`
>
> 若其中的`View` 没有 ID，绑定类中不存在对它的引用。

每个绑定类还包含一个 `getRoot()` 方法，用于为相应布局文件的根视图提供直接引用。在此示例中，`ResultProfileBinding` 类中的 `getRoot()` 方法会返回 `LinearLayout` 根视图。

以下几个部分介绍了生成的绑定类在 Activity 和 Fragment 中的使用。

##### 在 Activity 中使用

如需设置绑定类的实例以供 Activity 使用，请在 Activity 的 [`onCreate()`](https://developer.android.google.cn/reference/kotlin/android/app/Activity#oncreate) 方法中执行以下步骤：

1. 调用生成的绑定类中包含的静态 `inflate()` 方法。此操作会创建该绑定类的实例以供 Activity 使用。
2. 通过调用 `getRoot()` 方法或使用 [Kotlin 属性语法](https://kotlinlang.org/docs/reference/properties.html#declaring-properties)获取对根视图的引用。
3. 将根视图传递到 [`setContentView()`](https://developer.android.google.cn/reference/kotlin/android/app/Activity#setcontentview_1)，使其成为屏幕上的活动视图。

```java
    private ResultProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ResultProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }
    
```

您现在即可使用该绑定类的实例来引用任何视图：

```java
    binding.getName().setText(viewModel.getName());
    binding.button.setOnClickListener(new View.OnClickListener() {
        viewModel.userClicked()
    });
    
```

##### 在Fragment中使用

```java
private ResultProfileBinding binding;

@Override
public View onCreateView (LayoutInflater inflater,
                          ViewGroup container,
                          Bundle savedInstanceState) {
    binding = ResultProfileBinding.inflate(inflater, container, false);
    View view = binding.getRoot();
    return view;
}

@Override
public void onDestroyView() {
    super.onDestroyView();
    binding = null;
}
```

```java
binding.getName().setText(viewModel.getName());
binding.button.setOnClickListener(new View.OnClickListener() {
    viewModel.userClicked()
});
```

