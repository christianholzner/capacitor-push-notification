public class AcknowledgeService: NSObject {
    private var areNotificationsEnabled: Bool = false
    private var apiUrl: String = "http://localhost:5000/api/"
    private let acknowledgePath: String = "Intervention/Acknowledge"

    public func initContext(_ enabled: Bool, apiAcknowledgeUrl: String? = "") {
        self.areNotificationsEnabled = enabled
        self.apiUrl = apiAcknowledgeUrl ?? self.apiUrl
    }

    public func newNotification(_ data: [String: Any]) {
        self.postNotificationAck(data);
    }

    func postNotificationAck(_ data: [String: Any]) {
        self.sendAcknowledge(data) { success in
            NSLog("Notification acknowledge completed with : \(success)")
        }
    }

    func sendAcknowledge(_ data: [String: Any], onSuccess: @escaping (_ success: Bool) -> Void) {
        let body = self.makeAcknowledgeObject(data)
        let url = URL(string: self.apiUrl + self.acknowledgePath)!
        var request = URLRequest(url: url)

        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpMethod = "POST"
        request.httpBody = body

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

    func makeAcknowledgeObject(_ data: [String: Any]) -> Data? {
        return try? JSONSerialization.data(withJSONObject: [
            "deviceId": UIDevice.current.identifierForVendor!.uuidString,
            "origin": "native ios " + UIDevice.current.systemVersion,
            "interventionId": data["interventionId"] ?? "",
            "notificationLogId": data["notificationLogId"] ?? "",
            "areNotificationsEnabled": self.areNotificationsEnabled,
            "applicationIsActive": UIApplication.shared.applicationState == .active ? true : false
        ])
    }
}
