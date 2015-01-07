# AnyVersion - Android App 版本自动更新库

Android App 版本更新提示库 - An Android APP new version update library.

* [AnyVersion 源码分析](ANALYSE.md)
* [AnyVersion 高级使用教程](USAGE.md)

## 1. 基本使用教程

### Step 1 - 依赖

AnyVersion 最新版本已推送到 Maven Central 和 JCenter 两个服务中。请查看项目根`build.gradle`文件配置信息中是否存在 **jcenter** 或者 **mavenCentral** 选项。如果没有，请添加。

	allprojects {
    	repositories {
    	    jcenter()
    	    // OR: mavenCentral()
    	}
	}

然后，在 Android Studio 项目使用 AnyVersion 库的模块的 `build.gradle` 中添加如下依赖项：

	dependencies {
		... others ...
	    compile 'com.github.yoojia:anyversion:1.0@aar'
	}

### Step 2 - Android 权限

AnyVersion 需要的权限非常少，在 App 模块的 `AndroidManifest.xml` 文件中添加以下内容：

	<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    
**注意：不要嵌套在 application 中，在它的外层。 **

### Step 3 - 初始化

AnyVersion 被设计成一个单例。因此，每个 App 中只允许存在一个 AnyVersion 实例。建议在自定义 Application 类的 `onCreate(...)` 方法中初始化 AnyVersion 单例。

例如：

	public class AnyVersionApplication extends Application {

	    @Override
	    public void onCreate() {
	        super.onCreate();
	        AnyVersion.init(this, new VersionParser() {
	            @Override
	            public Version onParse(String response) {
	                final JSONTokener tokener = new JSONTokener(response);
	                try {
	                    JSONObject json = (JSONObject) tokener.nextValue();
	                    return new Version(
	                            json.getString("name"),
	                            json.getString("note"),
	                            json.getString("url"),
	                            json.getInt("code")
	                    );
	                } catch (JSONException e) {
	                    e.printStackTrace();
	                }
	                return null;
	            }
	        });
	    }
	}

这样可以保证 AnyVersion 只初始化一次。如果初始化多次，AnyVersion 只使用首次初始化的 `VersionParser` 接口。其它的初始化操作将会被忽略。

#### - 设置检查新版本 API 的 URL

AnyVersion 允许改变检查新版本 API 的 URL 地址。在初始化 AnyVersion 单例后，需要设置检查新版本的 URL 地址。

	AnyVersion version = AnyVersion.getInstance();
	version.setURL("http://192.168.1.2:8082/android/release.json");

AnyVersion 内置了 HTTP 请求，可以获取此 URL 的响应数据，交由 `VersionParser` 接口解析。

#### - VersionParser 版本数据解析接口

由于每个项目需求不同，服务端返回的数据格式也不同。因此，需要实现 `VersionParser` 接口来将不同的服务端响应数据解析成 `Vesion` 对象。

	new Parser() { 
		@Override public Version onParse(String response) {
			return 
			new Version(versionName, versionNote, downloadURL, versionCode);
	    }
	}
	
接口的实现类，需要将 `onParse(response)` 回调方法中的的 response 字符串解析，获取其中的 `versionName`,`versionNote`, `versionCode`,`downloadURL` 解析，并创建和返回 Version 对象。

### Step 4 - 检查 APP 新版本

AnyVersion 提供了三种处理新版本的方式。 这三种方式基本满足正常的应用需求。

* 弹出窗口 - **NotifyStyle.Dialog**
* 回调接口 - **NotifyStyle.Callback**
* 应用广播 - **NotifyStyle.Broadcast**

通过`anyVerson.check(NotifyStyle)`接口来触发检查新版本。

	AnyVersion version = AnyVersion.getInstance();
	version.check(NotifyStyle);


#### - 弹出窗口 Dialog

AnyVersion 的弹出窗口提醒新版本方式，需要`android.permission.SYSTEM_ALERT_WINDOW` 权限：

> <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

当远程服务器的版本(versionCode)大于当前应用的版本数时，AnyVersion 将弹出一个对话框让用户选择是否升级。

#### - 回调接口 Callback

使用 Callback 可以自定义处理新版本的方式。在 `check(NotifyStyle)` 前，需要设置 Callback 接口的实现。

	AnyVersion version = AnyVersion.getInstance();
    version.setCallback(new Callback() {
        @Override
        public void onVersion(Version version) {
            Log.d("AnyVersion","New Version: \n" + version);
        }
    });
    
**注意：仅当 NotifyStule.Callback 时，此 Callback 接口才会被回调。**

#### - 应用广播 Broadcast

使用 Broadcast 来处理新版本，这种方式很不寻常，不过仍然存在。

AnyVersion 限制了接收广播的 Receiver 类型必须为 `VersionReceiver` 子类，此举是为少 AnyVersion 的复杂度。

	class NewVersionReceiver extends VersionReceiver{
        @Override
        protected void onVersion(Version newVersion) {
            System.out.println(">> Broadcast === \n" + newVersion);
        }
    }
    
Android 的广播机制，建议用户在 Activity.onStart() 中注册 Receiver，在 Activity.onStop() 中移除。AnyVersion 提供了注册和反注册的方法。

	@Override
    protected void onStart() {
        super.onStart();
        AnyVersion.registerReceiver(this, newVersionReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AnyVersion.unregisterReceiver(this, newVersionReceiver);
    }

### Step 5 - 自动安装 App

当 App 用户同意更新，并且应用下载完成后，AnyVersion 会检查下载文件是否为 APK 文件。如果是 APK 文件，则自动调用系统的安装程序，完全自动更新 App 任务。

## 2. TODO

1. 根据网络类型，优化弹出窗口的选项类型
2. App 资源增量更新功能
3. 通知栏提醒新版本方式

## 3. 案例

* 停车** 

## 版权声明

如果你的App中使用了 AnyVersion 库，你可以通过电子邮件通知我 [Yoojia.Chen@gmail.com](mailto:yoojiachen@gmail.com) ，如下格式:

> 主题: 使用 AnyVersion 通知
> 
> 内容: 我在 (_YOUR-APP-NAME_) 中使用了 AnyVersion-{_lib-version_}。我[同意|不同意]在GitHub AnyVersion 的`案例`中显示我的应用信息。



#### Copyright

	Copyright 2014-2015 Yoojia Chen
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
