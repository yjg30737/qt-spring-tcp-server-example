// main.qml
import QtQuick
import QtQuick.Controls
import QtQuick.Controls.Material

ApplicationWindow {
    visible: true
    width: 400
    height: 300
    title: "Qt Client"
    property string statusText: "Not connected"
    
    // Material Design
    Material.theme: Material.Light
    Material.accent: Material.Purple
    Material.primary: Material.Indigo

    Column {
        anchors.centerIn: parent
        spacing: 16
        width: parent.width * 0.8

        TextField {
            id: hostInput
            placeholderText: "Server address"
            text: "127.0.0.1"
            width: parent.width
            Material.accent: Material.Purple
        }

        Button {
            width: parent.width
            text: client.connected ? "Disconnect" : "Connect"
            highlighted: true
            Material.background: client.connected ? Material.Indigo : Material.Purple
            onClicked: client.connected ? client.disconnectFromServer() : client.connectToServer(hostInput.text)
        }

        TextField {
            id: input
            placeholderText: "Message to send"
            width: parent.width
            Material.accent: Material.Purple
            enabled: client.connected
        }
        
        Button {
            text: "Send to Server"
            width: parent.width
            highlighted: true
            Material.background: Material.Purple
            enabled: client.connected && input.text.length > 0
            onClicked: client.sendMessage(input.text)
        }
        
        Label {
            id: status
            text: statusText
            width: parent.width
            horizontalAlignment: Text.AlignHCenter
            wrapMode: Text.WordWrap
            font.pixelSize: 14
        }

        Label {
            id: received
            text: "Waiting..."
            width: parent.width
            horizontalAlignment: Text.AlignHCenter
            wrapMode: Text.WordWrap
            font.pixelSize: 14
        }
    }

    Connections {
        target: client
        function onMessageReceived(msg) {
            received.text = "From Kotlin: " + msg
        }
        function onStatusMessage(msg) {
            statusText = msg
        }
    }

    Component.onCompleted: client.connectToServer(hostInput.text)
}
