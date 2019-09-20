# androidleaks
这是一个android内存泄漏，对象存活检测工具\ln
gradle加入：

implementation 'com.leak.sdk.leaklibrary:androidleak:1.0.0'

app加入：
public class TApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidLeak.init(this);
    }
}
