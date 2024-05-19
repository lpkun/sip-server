package top.skyyemu.sip.utils;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description 线程池
 * @Author LPkun
 * @Date 2024/5/19 19:00
 */
public class ThreadPoolUtil {
    /**
     * 核心线程数量
     */
    private static final int CORE_POOLS_IZE = Runtime.getRuntime().availableProcessors();
    /**
     * 空闲线程存活时间
     */
    private static final int KEEP_ALIVE_SECONDS = 60;
    /**
     * 自定义忽略最新的任务线程池
     */
    private static final ThreadPoolExecutor CUSTOM_IGNORE_THREAD_POOL = new ThreadPoolExecutor(
            CORE_POOLS_IZE,
            CORE_POOLS_IZE,
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            //一万个任务，满了忽略最新的
            new LinkedBlockingDeque<>(10000),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("custom_ignore_" + thread.getId());
                return thread;
            },
            //忽略最新的任务
            new ThreadPoolExecutor.DiscardPolicy()

    );

    /**
     * 自定义呼叫者运行策略程池
     */
    private static final ThreadPoolExecutor CUSTOM_CALLER_RUNS_THREAD_POOL = new ThreadPoolExecutor(
            CORE_POOLS_IZE,
            CORE_POOLS_IZE * 2,
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            //一万个任务，满了忽略最新的
            new LinkedBlockingDeque<>(10000),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("custom_caller_runs_" + thread.getId());
                return thread;
            },
            //使用调用该线程池的线程来执行任务
            new ThreadPoolExecutor.CallerRunsPolicy()

    );

    /**
     * 执行器按忽略线程池（队列满了忽略最新的任务）
     *
     * @param runnable 待执行任务
     */
    public static void executorByIgnore(Runnable runnable) {
        CUSTOM_IGNORE_THREAD_POOL.execute(runnable);
    }

    /**
     * 执行器按调用方运行（队列满了，调用方法现场执行任务）
     *
     * @param runnable 待执行任务
     */
    public static void executorByCallerRun(Runnable runnable) {
        CUSTOM_IGNORE_THREAD_POOL.execute(runnable);
    }
}
