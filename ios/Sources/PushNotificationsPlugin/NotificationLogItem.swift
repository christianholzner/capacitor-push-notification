import Foundation

public class NotificationLogItem {
    public var timeStamp: Int
    public var deviceId: String
    public var notificationLogId: String
    public var interventionId: String
    public var origin: String
    public var areNotificationsEnabled: Bool = false
    public var applicationIsActive: Bool = false

    public init (timeStamp: Int, deviceId: String, notificationLogId: String, interventionId: String,
                origin: String, areNotificationsEnabled: Bool = false, applicationIsActive: Bool = false) {
        self.timeStamp = timeStamp
        self.deviceId = deviceId
        self.notificationLogId = notificationLogId
        self.interventionId = interventionId
        self.origin = origin
        self.areNotificationsEnabled = areNotificationsEnabled
        self.applicationIsActive = applicationIsActive
    }

    public func toJson() -> Data? {
        return try? JSONSerialization.data(withJSONObject: [
            "timeStamp": String(self.timeStamp),
            "deviceId": self.deviceId,
            "notificationLogId": self.notificationLogId,
            "interventionId": self.interventionId,
            "origin": self.origin,
            "areNotificationsEnabled": String(self.areNotificationsEnabled),
            "applicationIsActive": String(self.applicationIsActive)
        ])
    }
}
