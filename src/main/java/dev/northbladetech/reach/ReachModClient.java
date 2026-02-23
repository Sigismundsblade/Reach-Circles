package dev.northbladetech.reach;

import dev.northbladetech.reach.client.ReachRenderer;
import dev.northbladetech.reach.config.ReachConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class ReachModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ReachConfig.get();
        WorldRenderEvents.AFTER_ENTITIES.register(ReachRenderer::render);
    }
}
