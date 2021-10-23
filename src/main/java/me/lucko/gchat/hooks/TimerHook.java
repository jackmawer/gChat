package me.lucko.gchat.hooks;

import me.lucko.gchat.tab.GChatTabList;

import java.util.TimerTask;

public class TimerHook extends TimerTask {

    @Override
    public void run() {
        if (GChatTabList.instance != null) {
            GChatTabList.instance.update();
        }
    }
}
