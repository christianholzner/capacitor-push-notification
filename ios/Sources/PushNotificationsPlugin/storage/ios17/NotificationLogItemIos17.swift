import Foundation
import SwiftData

@available(iOS 17, *)
@Model
public class NotificationLogItemIos17 {
    @Attribute(.unique)
    public var timeStamp: Int
    public var deviceId: String
    public var notificationLogId: String
    public var interventionId: String
    public var origin: String
    public var areNotificationsEnabled: Bool = false
    public var applicationIsActive: Bool = false

    init(
        timeStamp: Int, deviceId: String, notificationLogId: String, interventionId: String,
        origin: String, areNotificationsEnabled: Bool = false, applicationIsActive: Bool = false
    ) {
        self.timeStamp = timeStamp
        self.deviceId = deviceId
        self.notificationLogId = notificationLogId
        self.interventionId = interventionId
        self.origin = origin
        self.areNotificationsEnabled = areNotificationsEnabled
        self.applicationIsActive = applicationIsActive
    }

    public func toLog() -> NotificationLogItem {
        return NotificationLogItem(timeStamp: self.timeStamp, deviceId: self.deviceId, notificationLogId: self.notificationLogId, interventionId: self.interventionId, origin: self.origin, areNotificationsEnabled: self.areNotificationsEnabled, applicationIsActive: self.applicationIsActive)
    }
}
