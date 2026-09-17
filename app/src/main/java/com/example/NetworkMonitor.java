package com.example;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * NetworkMonitor tests real network latency, connection type, signal strength,
 * and provides legitimate gaming network troubleshooting tips.
 * Never makes misleading claims about reducing ping magically.
 */
public class NetworkMonitor {

    public interface PingCallback {
        void onPingProgress(int step, int totalSteps, long currentLatencyMs);
        void onPingComplete(PingResult result);
    }

    public static class PingResult {
        public long minLatencyMs = -1;
        public long maxLatencyMs = -1;
        public long avgLatencyMs = -1;
        public long jitterMs = 0;
        public int packetLossPercent = 0;
        public String qualityRating = "Unknown";
        public String targetHost = "8.8.8.8";
        public boolean isSuccess = false;
    }

    public static class NetworkDetails {
        public String connectionType = "Disconnected";
        public boolean isConnected = false;
        public boolean isWifi = false;
        public boolean isCellular = false;
        public int linkSpeedMbps = -1;
        public String wifiBand = "Unknown";
        public int signalPercent = -1;
        public String detailedStatus = "Offline";
    }

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public NetworkMonitor(Context context) {
        this.context = context.getApplicationContext();
    }

    public NetworkDetails getNetworkDetails() {
        NetworkDetails details = new NetworkDetails();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return details;

        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) {
            details.connectionType = "No Connection";
            details.detailedStatus = "Device is offline";
            return details;
        }

        NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
        if (caps == null) {
            details.connectionType = "Unknown";
            return details;
        }

        details.isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            details.isWifi = true;
            details.connectionType = "Wi-Fi";
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                try {
                    WifiInfo wifiInfo = wm.getConnectionInfo();
                    if (wifiInfo != null) {
                        details.linkSpeedMbps = wifiInfo.getLinkSpeed();
                        int freq = wifiInfo.getFrequency();
                        if (freq > 4900 && freq < 5900) {
                            details.wifiBand = "5 GHz High-Band";
                        } else if (freq > 2400 && freq < 2500) {
                            details.wifiBand = "2.4 GHz Standard";
                        } else if (freq > 5900) {
                            details.wifiBand = "6 GHz Wi-Fi 6E";
                        }

                        int rssi = wifiInfo.getRssi();
                        int level = WifiManager.calculateSignalLevel(rssi, 100);
                        details.signalPercent = Math.max(0, Math.min(100, level));
                    }
                } catch (Exception ignored) {
                }
            }
            details.detailedStatus = details.wifiBand + " • " +
                    (details.linkSpeedMbps > 0 ? details.linkSpeedMbps + " Mbps" : "Connected");

        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            details.isCellular = true;
            details.connectionType = "Cellular Mobile Data";
            details.detailedStatus = "Mobile Network (LTE/5G)";
        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            details.connectionType = "Ethernet LAN";
            details.detailedStatus = "Wired Connection";
        } else {
            details.connectionType = "Connected";
            details.detailedStatus = "Active Connection";
        }

        return details;
    }

    /**
     * Conducts a legitimate multi-sample network ping measurement against high-availability DNS endpoints.
     */
    public void runPingTest(final PingCallback callback) {
        executor.execute(() -> {
            String host = "8.8.8.8"; // Google Public DNS
            int port = 53;
            int attempts = 4;
            int timeoutMs = 1200;

            List<Long> latencies = new ArrayList<>();
            int failedPackets = 0;

            for (int i = 0; i < attempts; i++) {
                final int step = i + 1;
                long start = System.currentTimeMillis();
                boolean success = false;
                long latency = -1;

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), timeoutMs);
                    latency = System.currentTimeMillis() - start;
                    success = true;
                } catch (Exception e) {
                    // Try fallback Cloudflare DNS
                    try (Socket socket2 = new Socket()) {
                        long start2 = System.currentTimeMillis();
                        socket2.connect(new InetSocketAddress("1.1.1.1", 53), timeoutMs);
                        latency = System.currentTimeMillis() - start2;
                        success = true;
                    } catch (Exception e2) {
                        failedPackets++;
                    }
                }

                if (success && latency >= 0) {
                    latencies.add(latency);
                    final long currentLat = latency;
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onPingProgress(step, attempts, currentLat);
                        }
                    });
                }

                try {
                    Thread.sleep(120);
                } catch (InterruptedException ignored) {
                }
            }

            final PingResult result = new PingResult();
            result.targetHost = host;

            if (!latencies.isEmpty()) {
                result.isSuccess = true;
                long sum = 0;
                long min = Long.MAX_VALUE;
                long max = Long.MIN_VALUE;

                for (Long l : latencies) {
                    sum += l;
                    if (l < min) min = l;
                    if (l > max) max = l;
                }

                result.avgLatencyMs = sum / latencies.size();
                result.minLatencyMs = min;
                result.maxLatencyMs = max;
                result.jitterMs = max - min;
                result.packetLossPercent = (failedPackets * 100) / attempts;

                if (result.avgLatencyMs < 28) {
                    result.qualityRating = "Ultra Fast (Pro Grade)";
                } else if (result.avgLatencyMs < 55) {
                    result.qualityRating = "Good (Competitive Ready)";
                } else if (result.avgLatencyMs < 95) {
                    result.qualityRating = "Moderate (Casual Play)";
                } else {
                    result.qualityRating = "High Latency (Noticeable Delay)";
                }
            } else {
                result.isSuccess = false;
                result.qualityRating = "Connection Timed Out";
                result.packetLossPercent = 100;
            }

            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onPingComplete(result);
                }
            });
        });
    }

    public static List<String> getTroubleshootingTips() {
        List<String> tips = new ArrayList<>();
        tips.add("Connect to 5 GHz or 6 GHz Wi-Fi instead of 2.4 GHz to minimize interference.");
        tips.add("Pause automatic cloud uploads, app updates, and streaming on the same local network.");
        tips.add("Select game servers geographically closest to your physical location.");
        tips.add("Ensure your device is not behind a congested VPN routing overseas.");
        tips.add("Note: Android apps cannot alter external ISP infrastructure or physical routing distance.");
        return tips;
    }
}
