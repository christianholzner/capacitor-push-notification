import Foundation
import SwiftData

@available(iOS 17, *)
public class NotificationStorageIos17 : NotificationStorage {
    private var MAX_LOGS_IN_STORAGE: Int = 20
    private var container: ModelContainer!
    private var context: ModelContext!

    public init() {
        do {
            self.container = try ModelContainer(for: NotificationLogItemIos17.self)
            self.context = ModelContext(self.container)
        } catch {
            NSLog("Can't initialize the ModelContainer")
        }
    }

    public func save(log: NotificationLogItem) {
        let autoSaved = NotificationLogItemIos17(timeStamp: log.timeStamp, deviceId: log.deviceId, notificationLogId: log.notificationLogId, interventionId: log.interventionId, origin: log.origin, areNotificationsEnabled: log.areNotificationsEnabled, applicationIsActive: log.applicationIsActive)
        self.saveModel(log: autoSaved)
        self.keepMaxLogs()
    }

    public func saveModel(log: NotificationLogItemIos17) {
        do {
            self.context.insert(log)
            try self.context.save()
        } catch {
            NSLog("Can't save after inserted new NotificationLogItemIos17")
        }
    }

    func keepMaxLogs() {
        var logItems: [NotificationLogItemIos17] = self.getNotificationLogItemsIos17();
        while logItems.count > self.MAX_LOGS_IN_STORAGE {
            self.removeModel(log: logItems[0]);
            logItems =  self.getNotificationLogItemsIos17();
        }
    }

    public func remove(log: NotificationLogItem) {
        do {
            let keyId = log.timeStamp
            let descriptor = FetchDescriptor<NotificationLogItemIos17>(predicate: #Predicate { $0.timeStamp == keyId })
            let logs = try self.context.fetch(descriptor)

            if (logs.count > 0) {
                self.removeModel(log: logs[0])
            }
        } catch {
            NSLog("Can't fetch the model NotificationLogItemIos17")
        }
    }

    func removeModel(log: NotificationLogItemIos17) {
        do {
            self.context.delete(log)
            try self.context.save()
        } catch {
            NSLog("Can't save after removed a NotificationLogItemIos17")
        }
    }

    public func getNotificationLogItems() -> [NotificationLogItem] {
        return self.getNotificationLogItemsIos17().map { log in log.toLog() }
    }

    func getNotificationLogItemsIos17() -> [NotificationLogItemIos17] {
        do {
            return try self.context.fetch(FetchDescriptor<NotificationLogItemIos17>())
        } catch {
            NSLog("Can't load NotificationLogItemIos17")
            return []
        }
    }
}
