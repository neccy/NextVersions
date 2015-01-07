# AnyVersion - 高级使用教程

AnyVersion 提供了一些自定义接口，以方便扩展。

## 1. URL 与 RemoteHandler

在不使用自定义 RemoteHandler 时，检查新版本的 URL 地址是必要的。此时，AnyVersion 使用内置的 SimpleRemoteHandler 来获取新版本数据。如果没有通过`setURL`方法设置检查新版本 URL 地址，在调用 `check(NotifyStyle)` 方法时，AnyVersion 将抛出异常。

使用自定义 RemoteHandler 可以通过 `AnyVersion.getInstance().setCustomRemote(RemoteHandler)`接口。

`RemoteHandler` 与 `VersionParser` 类似，接口提供设定的 URL 地址，要求返回 URL 地址的响应数据。

> String request(String url) throws IOException;

继承`RemoteHandler`并实现`request`接口，返回 URL 的响应数据即可。

**在 setCustomRemote 之后，setURL(...)设置的 URL 不再是必要的。**

如果没有使用`setURL`设置 URL，则 `request(String url)` 传递的 URL 地址为 null。

## 2. 异步转同步 - SyncRemoteHandler 

AnyVersion 的多个自定义功能均使用接口来实现，网络请求功能也是。但开发中，我们的 App 使用的 API 接口极有可能是异步回调接口。为了解决在异步接口中使用异步接口这样的需求，AnyVersion 设计了 `SyncRemoteHandler` 类，使用它可以将异步接口转换成同步，以适应 RemoteHandler 的 `request(String)` 接口。

SyncRemoteHandler 的具体使用方法可以参考以下代码片段：

    AnyVersion.getInstance().setCustomRemote(new SyncRemoteHandler() {

        @Override
        public void request(String url, final StringBuilder resultBuf, final CountDownLatch markup) {
            ApiClient.request(new GetVersion(), new HttpCallback() {
                @Override
                public void onRequestSuccess(String jsonContent) {
                    resultBuf.append(jsonContent);
                }
                @Override
                public void onRequestFail(String message) {
                }

                @Override
                public void onRequestCompleted() {
                    markup.countDown();
                }

                @Override
                public void onRequestError(Throwable throwable) {
                }
            });
        }
    });

以上代码片段中，`ApiClient.request` 的第二个参数是一个回调接口。
SyncRemoteHandler 的 request 方法提供了三个参数。其中 `final StringBuilder resultBuf` 用于填充 `ApiClient.request` 异步请求的 URL 响应数据。而 `final CountDownLatch markup` 则用于标识 `ApiClient.request` 异步请求完成。

**注意:**

`final CountDownLatch markup` 的 `countDown()` 方法调用，对 SyncRemoteHandler 是至关重要的，而且要保证在 `ApiClient.request` 的所有回调路径上都被调用。如果这个调用缺失，将会导致 AnyVersion 的 check 没有任何响应。
