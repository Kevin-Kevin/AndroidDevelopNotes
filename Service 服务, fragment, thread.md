## Services 服务, fragment, thread

 `Service` 是一种可在后台执行长时间运行操作而不提供界面的应用组件。

服务可由其他应用组件启动，而且即使用户切换到其他应用，服务仍将在后台继续运行

服务可分为前台和后台, 状态有启动和绑定两种

>  服务在其托管进程的主线程中运行，它既**不**创建自己的线程，也**不**在单独的进程中运行（除非另行指定） 

### 在子线程中更新UI

可以使用以下函数更新

- `Activity.runOnUiThread(Runnable)`

- `View.post(Runnable)`

- `View.postDelayed(Runnable, long)`

- `handler.post()`

- `handler.sendEmptyMessage()`

- ~~使用AsyncTask子类, 被弃用~~

  > **This class was deprecated in API level 30.**
  > Use the standard `java.util.concurrent` or [Kotlin concurrency utilities](https://developer.android.google.cn/topic/libraries/architecture/coroutines) instead.

这几种方式的原理都是通过handler消息机制来更新ui

#### handler 消息机制

![image-20210820185729520](https://gitee.com/kevinzhang1999/my-picture/raw/master/uPic/image-20210820185729520-1629457049881.png)

每个线程各自的 looper 从各自的 messageQueue 中取出 message

一个 handler 和一个 looper 绑定, 可以通过 handler 发送 message 给 looper 所属的线程, 并且handler 的`run()`负责处理发送给本线程的 message

ui 主线程自带一个 looper, 其余线程必须自己创建并启动 looper

```java
// 一个Looper线程实现的典型例子
// 使用分离prepare()和loop()创建初始Handler与Looper进行通信 
class LooperThread extends Thread {
      public Handler mHandler;

      public void run() {
          Looper.prepare();

          mHandler = new Handler(Looper.myLooper()) {
              public void handleMessage(Message msg) {
                  // process incoming messages here
              }
          };

          Looper.loop();
      }
  }
```

### 服务概述

Service是Android中实现程序后台运行的解决方案 

Service的运行不依赖于任何用户界面，即使程序被切换到后台, Service仍然能够保持正常运行 

服务依赖于创建服务时所在的应用进程

服务默认运行在主线程当中

 需要在Service的内部手动创建子线程 , 否则就有可能出现主线程被阻塞的情况 



按生命周期分为 **启动服务** 和 **绑定服务**

启动服务按运行状态分为 **前台服务** 和 **后台服务**

服务可以同时为**启动和绑定**两种状态



### 服务的生命周期

#### 启动服务的周期

该服务在其他组件调用 `startService()` 时创建，然后无限期运行，且必须通过调用 `stopSelf()` 来自行停止运行

此外，其他组件也可通过调用 `stopService()` 来停止此服务

服务停止后，系统会将其销毁。

#### 绑定服务的周期

该服务在其他组件（客户端）调用 `bindService()` 时创建。然后，客户端通过 `IBinder` 接口与服务进行通信。客户端可通过调用 `unbindService()` 关闭连接。多个客户端可以绑定到相同服务，而且当所有绑定全部取消后，系统即会销毁该服务。（服务*不必*自行停止运行。）

#### 启动加绑定的服务的周期

即使所有客户端解绑, 还是要使用`stopService()`来停止服务

#### 服务周期的回调

![img](https://developer.android.google.cn/images/service_lifecycle.png)



一个应用进程中一个service只有一个实例

`onCreate()`只会在第一次启动服务时调用

第一次启动服务时先调用`oncreate()`然后是`onStartCommand()`

多个服务启动请求会导致多次对服务的 `onStartCommand()` 进行相应的调用。

如要停止服务，只需一个服务停止请求（使用 `stopSelf()` 或 `stopService()`）即可, 此时服务中的`onDestroy()`会被执行

### 服务的创建和使用

服务需要在manifest中注册, 并在其中修改service的属性

> Exported属性表示是否将这个Service暴露给外部其他程序访问
>
> Enabled属性表示是否启用这个Service 

如果使用Android Studio的新建service功能会自动在manifest里面注册

#### 创建

1. 创建 `Service` 的子类（或使用它的一个现有子类), 并override一些method, 在方法中实现逻辑

   ```java
   public class MyService extends Service {
     @Nullable
     @Override
     public IBinder onBind(Intent intent) {
       return null;
     }
     @Override
     public void onCreate() {
       super.onCreate();
     }
   
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
       return super.onStartCommand(intent, flags, startId);
     }
   
     @Override
     public void onDestroy() {
       super.onDestroy();
     }
   
   
   }
   
   ```

   

2. 在manifest中注册,即添加 `<service>` 元素作为 `<application> `元素的子元素

   ```xml
   <manifest ... >
     ...
     <application ... >
         <service android:name=".ExampleService" />
         ...
     </application>
   </manifest>
   ```

#### 启动

```java
Intent startService = new Intent(this, MyService.class);
startService(startService);

Intent stopService = new Intent(this, MyService.class);
startService(stopService);
```

#### 停止

服务可以调用 `stopSelf()` 来自行停止运行

其他组件也可通过调用 `stopService()` 来停止此服务



### 绑定服务

如果要在Activity中指挥Service去干什么 , 就需要使用绑定服务

实现绑定服务需要以下类

- Service子类
- IBinder 实现类
- ServiceConnection实现类

原理

- 在activity中 调用`bindService(intent, sericeConnection,flag)` 绑定服务

  > 第一个参数就是刚刚构建出的Intent对象
  >
  > 第二个参数是前面创建出的ServiceConnection的实例
  >
  > 第三个参数则是一个标志位 

- sericeConnection中会获取服务的IBinder

#### 服务绑定步骤

1. service子类必须实现`onBinder()`, 此函数返回一个IBinder实现类
2. 客户端调用 `bindService(intent, sericeConnection,flag)` , 传入ServiceConnection实现类
3. Android 系统创建客户端与服务之间的连接时，会对 `ServiceConnection` 调用 `onServiceConnected()` , `onServiceConnected()` 方法包含一个 `IBinder` 参数，客户端需要保存此 IBinder 用于和服务通信
4. 只有在第一个客户端绑定服务时，系统才会调用服务的 `onBind()` 方法来生成 `IBinder`
5. 当最后一个客户端取消与服务的绑定时`unbindService(connection)`，系统会销毁该服务（除非还通过 `startService()` 启动了该服务）

> service的onBinder()返回的IBinder最终在serviceConnection的onServiceConnected()的传入参数获得

#### 服务绑定实现代码

service

activity

```

```



#### todo : IBinder实现类  

自定义实现类以后再细学, 因为不需要跨进程, 现在先用扩展Binder类

- [扩展 Binder 类](https://developer.android.google.cn/guide/components/bound-services#Binder)

  如果服务是供您的自有应用专用，并且在与客户端相同的进程中运行（常见情况），您应当通过扩展 `Binder` 类并从 `onBind()` 返回该类的实例来创建接口。客户端收到 `Binder` 后，可利用它直接访问 `Binder` 实现或 `Service` 中提供的公共方法。

  如果服务只是您自有应用的后台工作器，应优先采用这种方式。您不使用这种方式创建接口的唯一一种情况是：其他应用或不同进程占用了您的服务。

- [使用 Messenger](https://developer.android.google.cn/guide/components/bound-services#Messenger)

  如需让接口跨不同进程工作，您可以使用 `Messenger` 为服务创建接口。采用这种方式时，服务会定义一个 `Handler`，用于响应不同类型的 `Message` 对象。此 `Handler` 是 `Messenger` 的基础，后者随后可与客户端分享一个 `IBinder`，以便客户端能利用 `Message` 对象向服务发送命令。此外，客户端还可定义一个自有 `Messenger`，以便服务回传消息。

  ​	这是执行进程间通信 (IPC) 最为简单的方式，因为 `Messenger` 会在单个线程中创建包含所有请求的队列，这样您就不必对服务进行线程安全设计。

- [使用 AIDL](https://developer.android.google.cn/guide/components/aidl)

  Android 接口定义语言 (AIDL) 会将对象分解成原语，操作系统可通过识别这些原语并将其编组到各进程中来执行 IPC。以前采用 `Messenger` 的方式实际上是以 AIDL 作为其底层结构。如上所述，`Messenger` 会在单个线程中创建包含所有客户端请求的队列，以便服务一次接收一个请求。不过，如果您想让服务同时处理多个请求，可以直接使用 AIDL。在此情况下，您的服务必须达到线程安全的要求，并且能够进行多线程处理。

  如需直接使用 AIDL，您必须创建用于定义编程接口的 `.aidl` 文件。Android SDK 工具会利用该文件生成实现接口和处理 IPC 的抽象类，您随后可在服务内对该类进行扩展。

#### 使用binder与service进行通信

例如在 IBinder中提供getService()获取当前服务实例, 然后在活动中通过服务实例来使用服务中的方法

也可以直接调用IBinder中的public方法

### 

### 后台服务

服务默认就是处于后台状态

### 前台服务

使用前台服务可以防止服务被回收, 会在通知栏显示一条类似通知的信息



### Fragment

[`Fragment`](https://developer.android.com/reference/androidx/fragment/app/Fragment?hl=zh-cn) 表示应用界面中可重复使用的一部分。Fragment 定义和管理自己的布局，具有自己的生命周期，并且可以处理自己的输入事件。Fragment 不能独立存在，而是必须由 Activity 或另一个 Fragment 托管

#### fragment的创建

##### 设置依赖

在project的build.gradle中添加google maven 存储库

在module的build.gradle中添加以下依赖项

```groovy
dependencies {
    def fragment_version = "1.3.6"
    // Java language implementation
    implementation "androidx.fragment:fragment:$fragment_version"
    // Kotlin
    implementation "androidx.fragment:fragment-ktx:$fragment_version"
}
```

##### 创建 fragment 类

要创建自定义布局的fragment，请将fragment的布局资源提供给基本构造函数

```java
class ExampleFragment extends Fragment {
    public ExampleFragment() {
        super(R.layout.example_fragment);
    }
}
```

##### 把 fragment 添加到 activity

fragment必须嵌入到 AndroidX  FragmentActivity 才能提供ui显示, 而AppCompatActivity 是FragmentActivity的子类, 可以放入AppCompatActivity

#### 添加fragment

##### 通过 XML 添加

要以声明方式将片段添加到活动布局的 XML，请使用 `FragmentContainerView`元素。

```xml
<!-- res/layout/example_activity.xml -->
<androidx.fragment.app.FragmentContainerView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/fragment_container_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:name="com.example.ExampleFragment" />
```

##### 通过代码动态添加

如果通过代码动态添加就不要在xml中设置android:name属性

```
    if(savedInstanceState == null){
      getSupportFragmentManager().beginTransaction()
              .setReorderingAllowed(true)
              .add(R.id.main_my_fragment_code,MyFragment.class,null)
              .commit();
    }
```

#### google提供的方便使用的fragment的子类

Fragment 库提供了方便使用的基于fragment的类：

- [`DialogFragment`](https://developer.android.com/reference/androidx/fragment/app/DialogFragment)

  显示浮动对话框。使用此类创建对话框是在[`Activity`](https://developer.android.com/reference/android/app/Activity)类中使用对话框辅助方法的一个很好的替代方法 ，因为片段会自动处理`Dialog`. 有关 更多详细信息，请参阅[显示对话框`DialogFragment`](https://developer.android.com/guide/fragments/dialogs)。

- [`PreferenceFragmentCompat`](https://developer.android.com/reference/androidx/preference/PreferenceFragmentCompat)

  将[`Preference`](https://developer.android.com/reference/androidx/preference/Preference)对象层次结构显示 为列表。您可以使用`PreferenceFragmentCompat`来 [创建设置屏幕](https://developer.android.com/guide/topics/ui/settings)为您的应用程序。

#### fragment管理器

[`FragmentManager`](https://developer.android.com/reference/androidx/fragment/app/FragmentManager?hl=zh-cn) 类负责对应用的 Fragment 执行一些操作，如添加、移除或替换它们，以及将它们添加到返回堆栈

##### 访问fragmentManager

1. 在 Activity 中访问

   每个 [`FragmentActivity`](https://developer.android.com/reference/androidx/fragment/app/FragmentActivity?hl=zh-cn) 及其子类（如 [`AppCompatActivity`](https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity?hl=zh-cn)）都可以通过 [`getSupportFragmentManager()`](https://developer.android.com/reference/androidx/fragment/app/FragmentActivity?hl=zh-cn#getSupportFragmentManager()) 方法访问 `FragmentManager`。

2. 在 Fragment 中访问

   Fragment 也能够托管一个或多个子 Fragment。在 Fragment 内，您可以通过 [`getChildFragmentManager()`](https://developer.android.com/reference/androidx/fragment/app/Fragment?hl=zh-cn#getChildFragmentManager()) 获取对管理 Fragment 子级的 `FragmentManager` 的引用。如果您需要访问其宿主 `FragmentManager`，可以使用 [`getParentFragmentManager()`](https://developer.android.com/reference/androidx/fragment/app/Fragment?hl=zh-cn#getParentFragmentManager())。

#### 保存fragment的状态

由于fragment是由activity管理的, 每次activity变化时会影响到fragment, fragment经常重新创建













### ViewPager2

TODO : 使用过程中或报错

使用 ViewPager2 可以在fragment之间滑动

#### 添加AndroidX依赖项

```groovy
dependencies {
    implementation "androidx.viewpager2:viewpager2:1.0.0"
}
```

#### 创建fragemnt的布局xml文件

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <TextView
        android:id="@+id/fragment2_text_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/view_pager_background_grey"
        android:text="text view"
        android:gravity="center"
        android:layout_marginRight="10dp"
        android:layout_marginLeft="10dp"
        />
</androidx.constraintlayout.widget
```

#### 创建fragment子类java文件

```java
public class MyFragmentForViewPager2 extends Fragment {
  public TextView textView;
  String tag;
  public MyFragmentForViewPager2(String tag) {
    super(R.layout.my_fragment_layout);
    this.tag = tag;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View fragmentView = inflater.inflate(R.layout.my_fragment_for_view_pager2_layout, container, false);
    textView = fragmentView.findViewById(R.id.fragment2_text_view);
    textView.setText(tag);
    return fragmentView;
  }
}
```

#### 创建 FragmentStateAdapter 子类java文件

给ViewPager2使用

```java
public class MyViewPager2Adapter extends FragmentStateAdapter {
  List<Fragment> fragmentList = new ArrayList<>();

  public MyViewPager2Adapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
    for (int i = 0; i < 3; i++) {
      fragmentList.add(new MyFragmentForViewPager2(String.valueOf(i)));
    }
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return fragmentList.get(position);
  }

  @Override
  public int getItemCount() {
    return fragmentList.size();
  }
}

```

#### 在activity_main.xml布局中添加ViewPager2布局文件

```xml
    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/main_view_pager2"
        android:layout_width="match_parent"
        android:layout_height="180dp"
        android:layout_marginTop="10dp"
        app:layout_constraintTop_toBottomOf="@+id/main_view_pager"

        />
```



#### 在Acitivty中设置ViewPager2的适配器

这里写在一个method中

```java
  private void initViewPager2(){
    MyViewPager2Adapter myViewPager2Adapter = new MyViewPager2Adapter(this);
    ViewPager2 viewPager2 = findViewById(R.id.main_view_pager2);
    viewPager2.setAdapter(myViewPager2Adapter);
  }
```

#### TabLayout和Viewpager2的配合



#### todo : 自定义ViewPager2切换fragment时的动画

等学了动画再回来细看

