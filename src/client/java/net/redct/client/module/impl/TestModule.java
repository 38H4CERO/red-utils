package net.redct.client.module.impl;

import net.redct.client.module.Category;
import net.redct.client.module.Module;

public class TestModule extends Module{
    public TestModule(){
        super("test","Test", Category.DUNGEONS);
    }

    // TODO
    @Override
    public void onEnable() {}
}
