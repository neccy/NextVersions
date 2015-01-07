# AnyVersion - 源码解析

Android 自动更新新版本的其中套路是：

1. 获取服务器的应用版本；
2. 对比当前应用的版本，判断是否为新版本；
3. 下载新版本；
4. 安装新版本；

## 1. 如何更新

每个 Android App 都有两个特殊的属性：

* `versionName` 版本名称，字符串。指应用版本的具体名称，如 `v0.0.1` 等。
* `versionCode` 版本码，整数。Android 系统依赖此参数来判定版本的新旧，`新版本.versionCode` > `旧版本.versionCode`。

用一个 App 版本对象，将 App 的 versionName，versionCode, 新版本 APK 下载 URL，以及此版本的更新说明等数据封装在一起。

    public class Version implements Parcelable {

        public final String name;
        public final String note;
        public final String URL;
        public final int code;

        public Version(String name, String note, String url, int code) {
            this.name = name;
            this.note = note;
            this.URL = url;
            this.code = code;
        }
        
    }
    
    ... parcelable code ...


## 2. 如何设计

AnyVersion 在设计接口时，秉承一个原则：默认优于配置。

### - 数据解析接口

每个 Web Service 有各自数据格式规范。但是无论远程服务器返回的数据格式是 JSON ，是 XML ，还是 HTML，这此 AnyVersion 都交由开发者自行定夺。`VersionParser` 就是完成此任务的接口。它提供远程服务器返回的数据，要求开发者将数据解析，并返回一个 Version 对象。 

    public interface VersionParser {
        /**
         * 将服务端返回的版本数据解析为版本对象
         * @param response 服务端返回的数据
         */
        Version onParse(String response);
    }
    
### - 网络访问接口

默认地，AnyVersion 提供了一个简单的 HTTP 网络访问实现类。开发者只需要设置访问 URL，即可获取远程数据。

### - 提供新版本展现方式

在 App 用户在使用过程中，我们如何提示用户此 App 需要更新呢？

当然，弹出一个对话框，强制不能按返回键取消，必须选择“现在更新”或者“立即更新”两个选项，更新效果是最直接的。但是，这样操作，不但用户会恨死你，传说中的产品狗也会骂死你。所以，我们需要提供几种更新提示展现方式。

1. 强制式：弹出对话框，强制更新
2. 温和式：推送到本地通知栏
3. 被动式：让用户主动点击按钮来触发更新

这三种方式是对 App 用户而言，而 AnyVersion 是面向开发者的。我们要将这三种展现方式转换为代码层次的处理方式。

    public enum NotifyStyle {
        Broadcast,
        Dialog,
        Callback,
        Notification
    }

用意非常清晰。当远程服务器上的 App 是新版本时，Broadcast 为通过 Android 的广播机制来处理如何处理这个新版本的 Version；Dialog，则直接弹出一个对话框，让用户选择是否更新；Callback，则是将新版本 Version 传递给一个回调接口，让 App 开发者自行处理新版本事宜；Notification，则是推送到通知栏。

## 3. AnyVersion 接口实现

AnyVersion 在实现上，严格遵循“Fast-Fail（快速崩溃）”原则，一旦认为在接口及参数的使用和逻辑不合法，立即崩溃，而输出非模棱两可的提示信息。

#### - 初始化

在应用启动时，将 AnyVersion 初始化；在需要检测应用新版本时，调用一些检测接口，即可实现检测。

Android OS 的资源都跟 Context 有关，AnyVersion 很多操作也非常依赖 Context。为了使得 API 参数列表尽量简单，我们不需要每个 Call API 都传递 Context 参数。为了达到这个目标，AnyVersion 单例对象不能持有一个短命的 Context对象。因此，Application 无疑是最佳的 Context。

在初始化时，要求库调用者实现和传递必要的参数，并判断其有效性。

    public class AnyVersion {

        private static final AnyVersion ANY_VERSION = new AnyVersion();

        private AnyVersion() {}

        public static AnyVersion getInstance(){
            return ANY_VERSION;
        }

        private Future<?> workingTask;
        private Version currentVersion;
        private Application context;
        private String URL;
        private Callback callback;
        private Parser parser;
        private ExecutorService threads = Executors.newSingleThreadExecutor();
        private Handler mainHandler;

        public static void init(final Application context, String url, Parser parser){
            if (ANY_VERSION.context != null){
                throw new IllegalStateException("Duplicate init !");
            }
            if (context == null) {
                throw new NullPointerException("Application Context CANNOT be null !");
            }
            if (parser == null) {
                throw new NullPointerException("Parser CANNOT be null !");
            }
            if (TextUtils.isEmpty(url)){
                throw new NullPointerException("URL CANNOT be null or empty !");
            }
            ANY_VERSION.context = context;
            ANY_VERSION.URL = url;
            ANY_VERSION.parser = parser;
            ANY_VERSION.mainHandler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    Version version = (Version) msg.obj;
                    new VersionDialog(context, version).show();
                }
            };
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
                ANY_VERSION.currentVersion = new Version(pi.versionName, null, null, pi.versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                ANY_VERSION.currentVersion = new Version(null, null, null, 0);
            }
        }

    }
    
创建 Dialog 时，Andoird OS 要求创建在 MainUIThread 中，使用 Activity.runOnMainThread(...) 和 Handler 可以解决此问题。但 AnyVersion 不适合与 Activity 交互，使用 Handler 和 Looper.getMainLooper 组合，解决在 Non-MainUI Thread 中要求 MainUI Thread 的问题是非常不错的选择。
    
通过 Callback 来处理 App 新版本不是必须的，在库被使用时，更多的是弹出窗口。使用额外 Setter 来传递即可。

    public void setCallback(Callback callback){
        if (callback == null){
            throw new NullPointerException("Callback CANNOT be null !");
        }
        this.callback = callback;
    }
    
在应用销毁时，我们要释放占用的资源，尤其是线程池。

	public void cancelCheck(){
	    if (workingTask != null && !workingTask.isDone()){
	        workingTask.cancel(true);
	    }
	}

	public void destroy(){
	    cancelCheck();
	    threads.shutdown();
	}

## 4. 如何下载

Android OS 已经内置下载管理器，使用它我们可以非常方便的完成 Apk 文件的下载功能。

## 5. 如何安装

前面这么多，都是为了这一步。

内置的下载管理器，在下载完成时会发送一个广播，通过判断广播的 Intent 的下载 ID，可以得知我们的 APK是否下载完成。当然也有其它详细的信息，比如下载进度、下载失败等信息。这些我们都不管，只要成功的。

    private static final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (!Downloads.KEEPS.contains(reference)) return;
            // 下载完成，自动安装
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(reference);
            DownloadManager download = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor cursor = download.query(query);
            if (cursor.moveToFirst()) {
                int fileNameIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                String fileName = cursor.getString(fileNameIdx);
                // 得到 APK 文件下载保存的路径
            }
            cursor.close();
        }
    };

安装 APK 这一步很简单，根据下载管理器，我们得到已下载的 APK 的文件路径，直接调用系统安装程序即可。

核心代码如下：

    Intent install = new Intent(Intent.ACTION_VIEW);
    install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(install);

