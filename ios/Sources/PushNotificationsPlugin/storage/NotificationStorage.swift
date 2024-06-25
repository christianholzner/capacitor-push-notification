import Foundation

protocol NotificationStorage {
    func save(log: NotificationLogItem)
    func remove(log: NotificationLogItem)
    func getNotificationLogItems() -> [NotificationLogItem]
}

public class NotificationStorageIos13 : NotificationStorage {
    private var MAX_LOGS_IN_STORAGE: Int = 10
    private var logs: [NotificationLogItem] =  []

    public func save(log: NotificationLogItem) {
        NSLog("Under iOS 17, we only keep in memory")
        self.logs.append(log)
        self.keepMaxLogs()
    }

    func keepMaxLogs() {
        var logItems: [NotificationLogItem] = self.getNotificationLogItems();
        while logItems.count > self.MAX_LOGS_IN_STORAGE {
            self.logs.remove(at: 0)
            logItems = self.getNotificationLogItems();
        }
    }

    public func remove(log: NotificationLogItem) {
        do {
            self.logs.removeAll(where: { $0.timeStamp == log.timeStamp })
        } catch {
            NSLog("Can't remove any logs")
        }
    }

    public func getNotificationLogItems() -> [NotificationLogItem] {
        return self.logs
    }

    public init() {
    }
}