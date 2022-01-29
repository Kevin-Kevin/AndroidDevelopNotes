

## Android 显示基础

### 屏幕显示

详情见 << Androidstudio 开发实战 从零基础到app上线>>



#### android 描述大小的单位

- px 像素
- dp 和设备无关的显示单位, aka dip
- sp 用于设置字体大小

在 xml 布局文件中, 除文字外都使用 dp 来保证不同尺寸的手机显示相同的大小

在 java 代码中, 只能使用 px , 为保证大小统一, 可以使用 `Utils.dip2px()` 把 dp 变成相应的 px

#### 颜色

##### 颜色的表示

- 八位十六进制数 : FFEEDDCC , 从前往后每两位代表一个值, 不透明度, 红色浓度, 绿色, 蓝色
- 六位十六进制数 : 在 xml 中默认 FF (不透明) , java 代码中默认 00 (全透明)



##### 使用

- Color 中有定义好的 12 种颜色, 类型是 int
  - java代码中使用, 如 Color.red

- 直接用十六进制的颜色编码赋值给 view
  - 在xml布局文件中设置颜色需要在色值前面加“#”，如android:textColor="#000000"
  - 在代码中设置颜色可以直接填八位的十六进制数值（如setTextColor(0xff00ff00);
  - 也可以通过Color.rgb(int red, int green, int blue)和Color.argb(int alpha, int red, int green, int blue)这两种方法指定颜色。

- 在 colors.xml 中定义后使用
  - 如果要在布局文件中使用XML颜色常量，可引用“@color/常量名”
  - 如果要在代码中使用XML颜色常量，可通过这行代码获取：getResources().getColor(R.color.常量名)。

在java代码中一般不要用六位编码，因为六位编码在代码中默认透明

#### 屏幕分辨率



### 视图 View 类

View 是所有控件和布局的祖先

### ViewGroup 类, 布局视图类

本质上是容器, 在其中可以放置其他 view 

所有的布局视图类都是它派生来的

