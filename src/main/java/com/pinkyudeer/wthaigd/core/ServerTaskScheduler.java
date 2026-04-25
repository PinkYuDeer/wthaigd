package com.pinkyudeer.wthaigd.core;

import java.util.ArrayDeque;

import com.pinkyudeer.wthaigd.Wthaigd;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class ServerTaskScheduler {

    public static final ServerTaskScheduler INSTANCE = new ServerTaskScheduler();

    private final ArrayDeque<Runnable> serverTasks = new ArrayDeque<>();
    private Thread serverThread;
    private boolean started;

    private ServerTaskScheduler() {}

    public void start(Thread thread) {
        this.serverThread = thread;
        this.started = true;
    }

    public void stop() {
        synchronized (serverTasks) {
            serverTasks.clear();
        }
        this.serverThread = null;
        this.started = false;
    }

    public void schedule(Runnable task, boolean allowImmediate) {
        if (task == null) return;
        if (started && allowImmediate && Thread.currentThread() == serverThread) {
            try {
                task.run();
            } catch (Exception e) {
                Wthaigd.LOG.error("Server task failed", e);
            }
            return;
        }
        synchronized (serverTasks) {
            serverTasks.add(task);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        while (true) {
            Runnable task;
            synchronized (serverTasks) {
                task = serverTasks.poll();
            }
            if (task == null) return;
            try {
                task.run();
            } catch (Exception e) {
                Wthaigd.LOG.error("Queued server task failed", e);
            }
        }
    }
}
