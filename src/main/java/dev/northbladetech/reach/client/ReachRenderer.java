package dev.northbladetech.reach.client;

import dev.northbladetech.reach.config.ReachConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReachRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Reach");
    private static long lastErrorLogAt;

    private ReachRenderer() {
    }

    public static void render(WorldRenderContext context) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = context.world();
            ClientPlayerEntity localPlayer = client.player;
            if (world == null || localPlayer == null) {
                return;
            }

            MatrixStack matrices = context.matrixStack();
            if (matrices == null) {
                return;
            }

            ReachConfig config = ReachConfig.get();
            double localReach = getReach(localPlayer);
            float tickDelta = context.tickCounter() == null ? 1.0f : context.tickCounter().getTickProgress(true);
            Vec3d localPos = localPlayer.getLerpedPos(tickDelta);
            var players = world.getPlayers();
            int playerCount = players.size();
            Vec3d[] positions = new Vec3d[playerCount];
            double[] reachValues = new double[playerCount];
            double[] distanceSqValues = new double[playerCount];
            boolean[] isLocalValues = new boolean[playerCount];

            for (int i = 0; i < playerCount; i++) {
                PlayerEntity player = players.get(i);
                double reach = getReach(player);
                if (reach <= 0.0) {
                    continue;
                }
                Vec3d pos = player.getLerpedPos(tickDelta);
                positions[i] = pos;
                reachValues[i] = reach;
                boolean isLocal = player == localPlayer;
                isLocalValues[i] = isLocal;
                if (!isLocal) {
                    distanceSqValues[i] = pos.squaredDistanceTo(localPos);
                }
            }

            boolean anyOtherWithinLocal = false;
            boolean anyLocalWithinOther = false;
            boolean anyMutual = false;
            double localReachSq = localReach * localReach;
            for (int i = 0; i < playerCount; i++) {
                if (isLocalValues[i] || positions[i] == null) {
                    continue;
                }
                double otherReach = reachValues[i];
                double distanceSq = distanceSqValues[i];
                boolean otherWithinLocal = distanceSq <= localReachSq;
                boolean localWithinOther = distanceSq <= otherReach * otherReach;
                if (otherWithinLocal) {
                    anyOtherWithinLocal = true;
                }
                if (localWithinOther) {
                    anyLocalWithinOther = true;
                }
                if (otherWithinLocal && localWithinOther) {
                    anyMutual = true;
                }
            }

            int cLocalDefault = ReachConfig.parseColor(config.colors.localDefault, 0xFFFF0000);
            int cLocalAnyInLocal = ReachConfig.parseColor(config.colors.localAnyInLocal, 0xFF00FF00);
            int cLocalAnyInOther = ReachConfig.parseColor(config.colors.localAnyInOther, 0xFF00B7FF);
            int cLocalMutual = ReachConfig.parseColor(config.colors.localMutual, 0xFF00FF7F);
            int cOtherDefault = ReachConfig.parseColor(config.colors.otherDefault, 0xFFFF0000);
            int cOtherWithinLocal = ReachConfig.parseColor(config.colors.otherWithinLocal, 0xFF00FF00);
            int cOtherLocalWithinOther = ReachConfig.parseColor(config.colors.otherLocalWithinOther, 0xFF00B7FF);
            int cOtherMutual = ReachConfig.parseColor(config.colors.otherMutual, 0xFF00FF7F);

            Vec3d cameraPos = context.camera().getPos();
            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f positionMatrix = entry.getPositionMatrix();

            int localColor;
            if (anyMutual) {
                localColor = cLocalMutual;
            } else if (anyOtherWithinLocal) {
                localColor = cLocalAnyInLocal;
            } else if (anyLocalWithinOther) {
                localColor = cLocalAnyInOther;
            } else {
                localColor = cLocalDefault;
            }

            int segments = Math.max(3, config.segments);
            double[] cosValues = new double[segments + 1];
            double[] sinValues = new double[segments + 1];
            for (int i = 0; i <= segments; i++) {
                double angle = (Math.PI * 2.0 * i) / segments;
                cosValues[i] = Math.cos(angle);
                sinValues[i] = Math.sin(angle);
            }

            VertexConsumer vertexConsumer = context.consumers().getBuffer(RenderLayer.getDebugQuads());
            for (int i = 0; i < playerCount; i++) {
                Vec3d playerPos = positions[i];
                if (playerPos == null) {
                    continue;
                }

                double reach = reachValues[i];
                boolean isLocal = isLocalValues[i];
                double distanceSq = isLocal ? 0.0 : distanceSqValues[i];
                boolean otherWithinLocal = !isLocal && distanceSq <= localReachSq;
                boolean localWithinOther = isLocal || distanceSq <= reach * reach;

                int color;
                if (isLocal) {
                    color = localColor;
                } else if (otherWithinLocal && localWithinOther) {
                    color = cOtherMutual;
                } else if (otherWithinLocal) {
                    color = cOtherWithinLocal;
                } else if (localWithinOther) {
                    color = cOtherLocalWithinOther;
                } else {
                    color = cOtherDefault;
                }

                float red = ((color >> 16) & 0xFF) / 255.0f;
                float green = ((color >> 8) & 0xFF) / 255.0f;
                float blue = (color & 0xFF) / 255.0f;
                float alpha = (((color >> 24) & 0xFF) / 255.0f) * config.alpha;

                double centerX = playerPos.x;
                double centerY = playerPos.y + 0.03;
                double centerZ = playerPos.z;

                double thickness = Math.max(0.01, config.lineWidth * 0.01);
                double innerRadius = Math.max(0.01, reach - thickness * 0.5);
                double outerRadius = reach + thickness * 0.5;
                double y = centerY;

                for (int s = 0; s < segments; s++) {
                    double cos1 = cosValues[s];
                    double sin1 = sinValues[s];
                    double cos2 = cosValues[s + 1];
                    double sin2 = sinValues[s + 1];

                    float xOuter1 = (float) (centerX + cos1 * outerRadius);
                    float zOuter1 = (float) (centerZ + sin1 * outerRadius);
                    float xInner1 = (float) (centerX + cos1 * innerRadius);
                    float zInner1 = (float) (centerZ + sin1 * innerRadius);
                    float xOuter2 = (float) (centerX + cos2 * outerRadius);
                    float zOuter2 = (float) (centerZ + sin2 * outerRadius);
                    float xInner2 = (float) (centerX + cos2 * innerRadius);
                    float zInner2 = (float) (centerZ + sin2 * innerRadius);

                    vertexConsumer.vertex(positionMatrix, xOuter1, (float) y, zOuter1).color(red, green, blue, alpha);
                    vertexConsumer.vertex(positionMatrix, xInner1, (float) y, zInner1).color(red, green, blue, alpha);
                    vertexConsumer.vertex(positionMatrix, xInner2, (float) y, zInner2).color(red, green, blue, alpha);
                    vertexConsumer.vertex(positionMatrix, xOuter2, (float) y, zOuter2).color(red, green, blue, alpha);
                    vertexConsumer.vertex(positionMatrix, xOuter2, (float) y, zOuter2).color(red, green, blue, alpha);
                    vertexConsumer.vertex(positionMatrix, xInner2, (float) y, zInner2).color(red, green, blue, alpha);
                    vertexConsumer.vertex(positionMatrix, xInner1, (float) y, zInner1).color(red, green, blue, alpha);
                    vertexConsumer.vertex(positionMatrix, xOuter1, (float) y, zOuter1).color(red, green, blue, alpha);
                }
            }

            matrices.pop();
        } catch (Throwable t) {
            long now = System.currentTimeMillis();
            if (now - lastErrorLogAt > 5000L) {
                lastErrorLogAt = now;
                LOGGER.error("Reach render error", t);
            }
        }
    }

    private static double getReach(PlayerEntity player) {
        return player.getAttributeValue(EntityAttributes.ENTITY_INTERACTION_RANGE);
    }
}
