## todo : Intent

intent 中带有信息, 传递给被调用的组件

可以启动并传递信息给 activity, service, boardcastReceiver

### intent 类中字段

- Component name : 要启动的组件名称, 可以使用 `setComponent()`、`setClass()`、`setClassName()`，或 `Intent` 构造函数设置组件名称

- Action : 用于指定 intent 的操作行为, 只能指定一个

- Category : 指定 intent 的操作类别, 可以指定多个

- Data : 既 Uri , 和 action 配合使用 , 指定 action 要操作的数据**路径**

  ![image-20210410150421368](https://gitee.com/kevinzhang1999/my-picture/raw/master/uPic/image-20210410150421368-1618038261669.png)

- Type ; 用于指定 Data 类型的定义

- 

- Extras

- Flags

### 显式 intent

 直接在构造函数中指定来源类和目标类

### 隐式 intent

不指定目标类, 根据动作让系统匹配拥有相同字符串定义的目标

使用隐式 Intent 时，Android 系统通过将 Intent 的内容与在设备上其他应用的[清单文件](https://developer.android.google.cn/guide/topics/manifest/manifest-intro)中声明的 **Intent 过滤器**进行比较，从而找到要启动的相应组件

如果 Intent 与 Intent 过滤器匹配，则系统将启动该组件，并向其传递 `Intent` 对象

如果多个 Intent 过滤器兼容，则系统会显示一个对话框，支持用户选取要使用的应用。



### Intent Filter 意图过滤器

 intent-filter 是在manifest中定义的 , 用于指定该组件要接收的 Intent 类型

例如 : AndroidManifest.xml 里activity 节点中就包含了 intent-filter

> 启动 `Service` 时，请始终使用显式 Intent
>
> 从 Android 5.0（API 级别 21）开始，如果使用隐式 Intent 调用 `bindService()`，系统会抛出异常
>
> 

### 向下一个 activity 传递参数

setData 只指定了



