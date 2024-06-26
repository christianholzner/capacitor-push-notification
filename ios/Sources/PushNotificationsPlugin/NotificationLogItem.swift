import Foundation

public class NotificationLogItem {
    public var timeStamp: Int
    public var deviceId: String
    public var notificationLogId: String
    public var interventionId: String
    public var origin: String
    public var areNotificationsEnabled: Bool? = nil
    public var applicationIsActive: Bool = false

    public init (timeStamp: Int, deviceId: String, notificationLogId: String, interventionId: String,
                origin: String, areNotificationsEnabled: Bool? = nil, applicationIsActive: Bool = false) {
        self.timeStamp = timeStamp
        self.deviceId = deviceId
        self.notificationLogId = notificationLogId
        self.interventionId = interventionId
        self.origin = origin
        self.areNotificationsEnabled = areNotificationsEnabled
        self.applicationIsActive = applicationIsActive
    }

    public func toJson() -> Data? {
        var log = [
            "timeStamp": String(self.timeStamp),
            "deviceId": self.deviceId,
            "notificationLogId": self.notificationLogId,
            "interventionId": self.interventionId,
            "origin": self.origin,
            "applicationIsActive": String(self.applicationIsActive)
        ]

        if (self.areNotificationsEnabled != nil) {
            log["areNotificationsEnabled"] = String(self.areNotificationsEnabled!)
        }

        return try? JSONSerialization.data(withJSONObject: log)
    }
}
