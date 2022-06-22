## Services 服务, thread

 `Service` 是一种可在后台执行长时间运行操作而不提供界面的应用组件。

服务可由其他应用组件启动，而且即使用户切换到其他应用，服务仍将在后台继续运行

服务可分为前台和后台, 状态有启动和绑定两种

>  服务在其托管进程的主线程中运行，它既**不**创建自己的线程，也**不**在单独的进程中运行（除非另行指定） 

### 在子线程中更新UI

![image-20220513190737669](Service 服务, fragment, thread.assets/image-20220513190737669.png)

> <<Android开发艺术探索>>

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

#### `启动服务`的周期

该服务在其他组件调用 `startService()` 时创建，然后无限期运行，且必须通过调用 `stopSelf()` 来自行停止运行

此外，其他组件也可通过调用 `stopService()` 来停止此服务

服务停止后，系统会将其销毁。

#### `绑定服务`的周期

该服务在其他组件（客户端）调用 `bindService()` 时创建。然后，客户端通过 `IBinder` 接口与服务进行通信。客户端可通过调用 `unbindService()` 关闭连接。多个客户端可以绑定到相同服务，而且当所有绑定全部取消后，系统即会销毁该服务。（服务不用自行停止运行。）

#### `启动加绑定`的服务的周期

即使所有客户端解绑, 还是要使用`stopService()`来停止服务

#### 服务周期的回调

![img](https://developer.android.google.cn/images/service_lifecycle.png)



一个应用进程中一个service只有一个实例

`onCreate()`只会在第一次启动服务时调用

第一次启动服务时先调用`oncreate()`然后是`onStartCommand()`

多个服务启动请求会导致多次对服务的 `onStartCommand()` 进行相应的调用。

> `onStartCommand()`的返回值说明系统如何处理被异常杀掉的进程
>
> ![image-20220519191718540](Service 服务, thread.assets/image-20220519191718540.png)

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
Intent startService = new Intent(getApplicationContext(), MyService.class);
startService(startService);

Intent stopService = new Intent(this, MyService.class);
stopService(stopService);
```

#### 停止

服务可以调用 `stopSelf()` 来自行停止运行

其他组件也可通过调用 `stopService()` 来停止此服务



### 绑定服务

通过绑定服务可以实现其他组件和服务的通信

> 只有 Activity、服务和内容提供程序可以绑定到服务 , 无法从广播接收器绑定到服务。 

如果要在组件中指挥Service去干什么 , 就需要使用绑定服务

实现绑定服务需要以下类

- Service子类
- IBinder 实现类
- ServiceConnection实现类

原理

- 在activity中 调用`bindService(intent, serviceConnection,flag)` 绑定服务

  > 第一个参数就是刚刚构建出的Intent对象
  >
  > 第二个参数是前面创建出的ServiceConnection的实例
  >
  > 第三个参数则是一个标志位 

- ServiceConnection中会获取服务的IBinder

- 调用IBinder的函数来对service的数据进行操作

> IBinder的函数在activity中调用, 那就在主线程中运行, 并不能实现说主线程直接调用函数, 然后就在服务中运行此函数
>
> 若要实现主线程通知服务运行一段代码如runnable, 需要在服务的回调函数中开启子线程, 子线程中开启looper

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

注意，从Android 9.0开始, 需修改AndroidManifest.xml，添加如下权限：

```xml
<!-- 允许前台服务 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

前台服务必须先被启动, 并且要用startForegroundService





- startForeground：把当前服务切换到前台运行

  第一个参数表示通知的编号

  第二个参数表示 Notification对象，意味着切换到前台就是展示到通知栏

- stopForeground：停止前台运行

  参数为true表示清除通知，参数为false表示不清除

```java
// 注意 : notification里面要加contentIntent
// 这里的Intent要用 PendingIntent
Intent notificationIntent = new Intent(this, ExampleActivity.class);
PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 0, notificationIntent, 0);

Notification notification =
          new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
    .setContentTitle(getText(R.string.notification_title))
    .setContentText(getText(R.string.notification_message))
    .setSmallIcon(R.drawable.icon)
    .setContentIntent(pendingIntent)
    .setTicker(getText(R.string.ticker_text))
    .build();

startForeground(ONGOING_NOTIFICATION_ID, notification);
```
