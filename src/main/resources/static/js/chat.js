'use strict';

var stompClient = null;
var username = null;
var roomId = null;

function connect() {
    username = document.querySelector('#username').value;
    roomId = document.querySelector('#roomId').value;

    if (username && roomId) {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        // tat log debug cua stomp
        stompClient.debug = null;

        stompClient.connect({}, onConnected, onError);
    }
}

function onConnected() {
    // subscribe vao room
    stompClient.subscribe('/topic/room.' + roomId, onMessageReceived);

    // gui thong bao user join
    stompClient.send('/app/chat.addUser/' + roomId, {},
        JSON.stringify({
            sender: username,
            type: 'JOIN'
        })
    );

    console.log('Da ket noi thanh cong!');
}

function onError(error) {
    console.error('Khong the ket noi WebSocket: ', error);
    var messageArea = document.querySelector('#messageArea');
    var errorElement = document.createElement('div');
    errorElement.className = 'message event';
    errorElement.textContent = 'Khong the ket noi den server. Vui long thu lai!';
    messageArea.appendChild(errorElement);
}

function sendMessage(event) {
    event.preventDefault();
    var messageInput = document.querySelector('#message');
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT',
            roomId: roomId
        };

        stompClient.send('/app/chat.sendMessage/' + roomId, {},
            JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageArea = document.querySelector('#messageArea');
    var messageElement = document.createElement('div');

    if (message.type === 'JOIN') {
        messageElement.className = 'message event';
        messageElement.textContent = message.sender + ' da tham gia phong chat!';
    } else if (message.type === 'LEAVE') {
        messageElement.className = 'message event';
        messageElement.textContent = message.sender + ' da roi phong chat.';
    } else {
        // tin nhan binh thuong
        var isSent = message.sender === username;
        messageElement.className = 'message ' + (isSent ? 'sent' : 'received');

        if (!isSent) {
            var senderName = document.createElement('div');
            senderName.className = 'sender-name';
            senderName.textContent = message.sender;
            messageElement.appendChild(senderName);
        }

        var textElement = document.createElement('div');
        textElement.className = 'message-text';
        textElement.textContent = message.content;
        messageElement.appendChild(textElement);

        var timeElement = document.createElement('div');
        timeElement.className = 'message-time';
        var now = new Date();
        timeElement.textContent = now.getHours().toString().padStart(2, '0') + ':' +
                                  now.getMinutes().toString().padStart(2, '0');
        messageElement.appendChild(timeElement);
    }

    messageArea.appendChild(messageElement);

    // tu dong scroll xuong tin nhan moi nhat
    messageArea.scrollTop = messageArea.scrollHeight;
}

// khoi tao khi trang load xong
document.addEventListener('DOMContentLoaded', function() {
    var messageForm = document.querySelector('#messageForm');
    messageForm.addEventListener('submit', sendMessage, true);
    connect();
});
