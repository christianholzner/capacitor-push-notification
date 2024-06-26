public class AcknowledgeService: NSObject {
    private var areNotificationsEnabled: Bool = false
    private var apiUrl: String = "http://localhost:5000/api/"
    private var acknowledgePath: String = "Intervention/Acknowledge"
    private var notificationStorage: NotificationStorage!

    public func initContext(_ enabled: Bool, apiAcknowledgeUrl: String? = "") {
        self.areNotificationsEnabled = enabled
        self.apiUrl = apiAcknowledgeUrl ?? self.apiUrl

        if #available(iOS 17, *) {
            self.notificationStorage = NotificationStorageIos17()
        } else {
            self.notificationStorage = NotificationStorageIos13()
        }
    }

    public func newNotification(_ data: [String: Any]) {
        if (self.notificationStorage != nil) {
            self.notificationStorage.save(log: self.generateNotificationLog(data))
            self.postNotification();
        }
    }

    func postNotification() {
        let logs = self.notificationStorage.getNotificationLogItems()
        for log in logs {
            self.sendAcknowledge(log) { success in
                NSLog("Notification acknowledge completed with : \(success)")
                if success == true {
                    self.notificationStorage.remove(log: log)
                }
            }
        }
    }

    func sendAcknowledge(_ log: NotificationLogItem, onSuccess: @escaping (_ success: Bool) -> Void) {
        let url = URL(string: self.apiUrl + self.acknowledgePath)!
        var request = URLRequest(url: url)

        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpMethod = "POST"
        request.httpBody = log.toJson()

        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if error != nil {
                onSuccess(false)
                return
            }
            if let response = response as? HTTPURLResponse {
                guard (200 ... 204) ~= response.statusCode else {
                    onSuccess(false)
                    return
                }
            }

            onSuccess(true)
        }

        task.resume()
    }

    func generateNotificationLog(_ data: [String: Any]) -> NotificationLogItem {
        return NotificationLogItem(
            timeStamp: Int(NSDate().timeIntervalSince1970) * 1000,
            deviceId: UIDevice.current.identifierForVendor!.uuidString,
            notificationLogId: data["notificationLogId", default: ""] as! String,
            interventionId: data["interventionId", default: ""] as! String,
            origin: "native ios " + UIDevice.current.systemVersion,
            areNotificationsEnabled: self.areNotificationsEnabled,
            applicationIsActive: UIApplication.shared.applicationState == .active ? true : false
        )
    }
}
