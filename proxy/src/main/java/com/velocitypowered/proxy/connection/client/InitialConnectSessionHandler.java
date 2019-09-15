package com.velocitypowered.proxy.connection.client;

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.protocol.packet.PluginMessage;
import com.velocitypowered.proxy.protocol.util.PluginMessageUtil;

public class InitialConnectSessionHandler implements MinecraftSessionHandler {

  private final ConnectedPlayer player;

  InitialConnectSessionHandler(ConnectedPlayer player) {
    this.player = player;
  }

  @Override
  public boolean handle(PluginMessage packet) {
    VelocityServerConnection serverConn = player.getConnectionInFlight();
    if (serverConn != null) {
      if (player.getPhase().handle(player, packet, serverConn)) {
        return true;
      }

      if (PluginMessageUtil.isRegister(packet)) {
        player.getKnownChannels().addAll(PluginMessageUtil.getChannels(packet));
      } else if (PluginMessageUtil.isUnregister(packet)) {
        player.getKnownChannels().removeAll(PluginMessageUtil.getChannels(packet));
      }
      serverConn.ensureConnected().writeImmediately(packet.retain());
    }
    return true;
  }

  @Override
  public void disconnected() {
    // the user cancelled the login process
    player.teardown();
  }
}
