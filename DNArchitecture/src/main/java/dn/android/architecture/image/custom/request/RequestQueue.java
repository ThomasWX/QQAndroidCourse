package dn.android.architecture.image.custom.request;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestQueue {


    /**
     阻塞式队列  PriorityBlockingQueue 带优先级的阻塞式队列
     为何要使用阻塞式队列？
     需多线程共享
     生产效率和消费效率相差太远
     支持优先级，优先级高的先被消费，每个产品都有编号
      */
    private BlockingQueue<BitmapRequest> requestBlockingQueue = new PriorityBlockingQueue<>();

    /**
     * 转发器的数量
     */
    private int threadCount;
    /**
     * 转发器(可理解为消费者)：转发请求
     */
    private RequestDispatcher[] requestDispatchers;

    private AtomicInteger i = new AtomicInteger(0);


    public RequestQueue(int threadCount) {
        this.threadCount = threadCount;
    }

    /**
     * 添加请求对象
     * @param request
     */
    public void addRequest(BitmapRequest request){
        if (!requestBlockingQueue.contains(request)){
            // 给请求添加编号，这个方法会被多线程频繁调用，要考虑线程安全
            request.setSerialNo(i.incrementAndGet());
            requestBlockingQueue.add(request);
        }
    }

    /**
     * 开启请求
     */
    public void start(){
        // 先停止
        stop();
        // 开启所有转发器
        startDispatchers();
    }

    /**
     * 开启转发器
     */
    private void startDispatchers() {
        requestDispatchers = new RequestDispatcher[threadCount];
        for (int j = 0; j < threadCount; j++) {
            RequestDispatcher dispatcher = new RequestDispatcher(requestBlockingQueue);
            requestDispatchers[j] = dispatcher;
            dispatcher.start();
        }
    }

    /**
     * 停止请求
     */
    public void stop(){

    }

}
