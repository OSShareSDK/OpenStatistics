package cn.sharesdk.open.statistics.server;

interface AIDLService {
	void setting(String action, String value);
	void saveLog(String action, String jsonString);
	void uploadLog();
	void downloadApk(String url);
	void updateConfig();
	void updateApk();
}