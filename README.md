# androidleaks
这是一个android内存泄漏，对象存活检测工具
gradle加入：

app加入：
public class TApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidLeak.init(this);
    }
}
