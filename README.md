# AnyVersion - Android App 版本自动更新库

Android App 版本更新升级库。

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
	    compile 'com.github.yoojia:next-versions:2.0-alpha'
	}

### Step 2 - Android 权限

AnyVersion 需要的权限非常少，在 App 模块的 `AndroidManifest.xml` 文件中添加以下内容：

	<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    
**注意：不要嵌套在 application 中，在它的外层。 **

### Step 3 - 设置更新源

NextVersion 内置了多种更新源的实现。

- 1. RemoteSource 部署在服务器上返回指定**格式**JSON数据的URL更新源。

> 这是最常用的场景，通常的更新APK方式都是通过网络读取并下载安装。

- 2. DirectorySource 指定本地文件夹路径（如U盘目录），扫描目录下的APK文件，读取最新版本的APK文件。

> 这是少有但也属于正常需求的场景。我厂经常使用这种方式给客户更新APK。

- 3. 自定义更新源。这个按你的需求来实现Source接口即可。

```java

```

### Step 6 - 触发更新

```java

```

## 高级使用方式

### Source - 更新源

NextVersions 内置了多种更新源的实现。

- 1. RemoteJSONSource 部署在服务器上返回指定**格式**JSON数据的URL更新源。

> 这是最常用的场景，通常的更新APK方式都是通过网络读取并下载安装。

- 2. LocalFileSource 指定本地文件夹路径（如U盘目录），扫描目录下的APK文件，读取最新版本的APK文件。

> 这是少有但也属于正常需求的场景。我厂经常使用这种方式给客户更新APK。

- 3. 自定义更新源。这个按你的需求来实现Source接口即可。


## Copyright

	Copyright 2014-2016 Yoojia Chen (yoojia.chen@gmail.com)
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
