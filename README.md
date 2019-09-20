# androidleaks
这是一个android内存泄漏，对象存活检测工具

gradle添加：

implementation 'com.leak.sdk.leaklibrary:androidleak:1.0.0'

Application添加：

public class TApplication extends Application {

    @Override
    
    public void onCreate() {
    
        super.onCreate();
        
        AndroidLeak.init(this);
    }
    
}
activity检测泄自动弹出信息


Object存活检测

Object o=new Object();
 
 需要回收Object的地方
 
 AndroidLeak.addObserver(o);
 
 如果没有回收那么弹出引用链
